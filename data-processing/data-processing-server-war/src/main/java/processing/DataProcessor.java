package processing;

import com.google.common.collect.EvictingQueue;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DataProcessor implements Runnable
{
    private Logger logger;
    private UUID uuid;
    private String title;

    private SourceDestination source;
    private SourceDestination destination;
    private String topicOrDataIn;
    private String topicOrDataOut;
    private String javascript;
    private int minDataSetSize;
    private int maxDataSetSize;
    private String consumerGroup;
    private Queue<String> collectedData = EvictingQueue.create(1);
    private boolean forgetting = false;

    private String bootstrapServers = "localhost:9092";
    private Consumer<Long, String> kafkaConsumer;
    private Producer<Long, String> kafkaProducer;
    private FileSystem inputFileSystem;
    private FileSystem outputFileSystem;
    private long readerPos;
    private long timeout = 1000;

    public enum SourceDestination
    {
        KAFKA("Kafka"), HADOOP("Hadoop");

        String displayString;

        SourceDestination(String displayString)
        {
            this.displayString = displayString;
        }

        @Override
        public String toString()
        {
            return displayString;
        }

        public static SourceDestination fromString(String displayString)
        {
            switch (displayString)
            {
                case "Kafka":
                    return KAFKA;
                case "Hadoop":
                    return HADOOP;
                default:
                    throw new IllegalArgumentException("Unknown SourceDestination Type");
            }
        }
    }

    public DataProcessor(SourceDestination source, SourceDestination destination, String javascript,
                         int minDataSetSize, int maxDataSetSize, String consumerGroup, String topicOrDataIn,
                         String topicOrDataOut, boolean forgetting, long timeout, String title)
    {
        this.uuid = UUID.randomUUID();
        logger = Logger.getLogger(this.getClass().getName().concat("@").concat(this.uuid.toString()));
        logger.setUseParentHandlers(false);

        try
        {
            String pathToLog = String
                    .format("%s/jobs/job-%s.log", System.getProperty("jboss.server.log.dir"), this.uuid.toString());
            Path pathToLogAsPath = Paths.get(pathToLog);

            if (!Files.exists(pathToLogAsPath))
            {
                Files.createDirectories(pathToLogAsPath.getParent());
                Files.createFile(pathToLogAsPath);
                logger.info("Created log file ".concat(pathToLogAsPath.toString()));
            }

            logger.addHandler(new LogHandler(pathToLog));
        }
        catch (IOException e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.severe(
                    String.format("Could not add file handler for job %s:\n%s", this.uuid.toString(), sw.toString()));
        }
        update(source, destination, javascript, minDataSetSize, maxDataSetSize, consumerGroup, topicOrDataIn,
               topicOrDataOut, forgetting, timeout, title);
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void update(SourceDestination source, SourceDestination destination, String javascript,
                       int minDataSetSize, int maxDataSetSize, String consumerGroup, String topicOrDataIn,
                       String topicOrDataOut, boolean forgetting, long timeout, String title)
    {
        this.title = title;
        this.source = source;
        this.destination = destination;
        this.javascript = javascript;
        this.minDataSetSize = forgetting ? 1 : minDataSetSize;
        this.maxDataSetSize = forgetting ? 1 : maxDataSetSize;
        this.consumerGroup = consumerGroup;
        this.topicOrDataIn = topicOrDataIn;
        this.topicOrDataOut = topicOrDataOut;
        this.forgetting = forgetting;
        this.timeout = timeout;

        Queue<String> newCollectedData = EvictingQueue.create(maxDataSetSize);
        newCollectedData.addAll(collectedData);
        collectedData = newCollectedData;

        logger.info(">> Setting config to ".concat(getAsJSON().toString()));

        if (kafkaConsumer != null)
        {
            synchronized (kafkaConsumer)
            {
                kafkaConsumer.unsubscribe();
                kafkaConsumer.close();
            }
        }

        if ("".equals(topicOrDataIn) || topicOrDataIn == null)
        {
            return;
        }

        if (SourceDestination.KAFKA.equals(source))
        {
            kafkaConsumer = constructKafkaConsumer();
        }
        else if (SourceDestination.HADOOP.equals(source))
        {
            org.apache.hadoop.fs.Path path = new org.apache.hadoop.fs.Path(topicOrDataIn);
            try
            {
                inputFileSystem = constructHadoopFileSystem(path);
            }
            catch (IOException e)
            {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.severe(
                        String.format("Could not read data from hadoop file %s:\n%s", topicOrDataIn, sw.toString()));
            }
            readerPos = 0;
        }

        if (SourceDestination.KAFKA.equals(destination))
        {
            if (kafkaProducer != null)
            {
                kafkaProducer.close();
            }

            kafkaProducer = constructKafkaProducer();
        }
        else if (SourceDestination.HADOOP.equals(destination))
        {
            org.apache.hadoop.fs.Path path = new org.apache.hadoop.fs.Path(topicOrDataOut);
            try
            {
                outputFileSystem = constructHadoopFileSystem(path);

                if (!outputFileSystem.exists(path))
                {
                    outputFileSystem.create(path);
                }
            }
            catch (IOException e)
            {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.severe(
                        String.format("Can not create output for file %s in hadoop:\n%s", topicOrDataIn,
                                      sw.toString()));
            }
        }
    }

    @Override
    public void run()
    {
        try
        {
            getData();
            logger.info("Operating on data:\n\t".concat(String.join(" and ", collectedData)));

            if (collectedData.size() < minDataSetSize)
            {
                logger.info("---- EOEC ----\n");
                return;
            }

            Context context = Context.create();
            Value function = context.eval("js",
                                          String.format("(function(data){%s\nreturn results;})", javascript));
            Value execute = function.execute(collectedData);
            JSONArray executionResult = new JSONArray(execute.toString());

            String executionResultAsString = executionResult.toString();
            if (executionResult.length() == 1)
            {
                executionResultAsString = executionResultAsString.substring(1, executionResultAsString.length() - 1);
            }

            saveData(executionResultAsString);
        }
        catch (Exception e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.severe(sw.toString());
        }
        logger.info("---- EOEC ----\n");
    }

    public JSONObject getAsJSON()
    {
        JSONObject dataProcessorAsJSON = new JSONObject();
        dataProcessorAsJSON.put("title", title);
        dataProcessorAsJSON.put("id", uuid.toString());
        dataProcessorAsJSON.put("source", source.toString());
        dataProcessorAsJSON.put("destination", destination.toString());
        dataProcessorAsJSON.put("javascript", javascript);
        dataProcessorAsJSON.put("minDataSetSize", minDataSetSize);
        dataProcessorAsJSON.put("maxDataSetSize", maxDataSetSize);
        dataProcessorAsJSON.put("topicOrDataIn", topicOrDataIn);
        dataProcessorAsJSON.put("topicOrDataOut", topicOrDataOut);
        dataProcessorAsJSON.put("forgetting", forgetting);
        dataProcessorAsJSON.put("timeout", timeout);

        if (SourceDestination.KAFKA.equals(source))
        {
            dataProcessorAsJSON.put("consumerGroup", consumerGroup);
        }

        return dataProcessorAsJSON;
    }

    public UUID getId()
    {
        return uuid;
    }

    public void purge()
    {
        if (kafkaConsumer != null)
        {
            synchronized (kafkaConsumer)
            {
                kafkaConsumer.unsubscribe();
                kafkaConsumer.close();
            }
        }

        if (kafkaProducer != null)
        {
            this.kafkaProducer.close();
        }
    }

    private void getData() throws IOException
    {
        logger.info("Forgetting previous data sets: ".concat(String.valueOf(forgetting)));
        if (forgetting)
        {
            collectedData.clear();
        }

        List<String> dataSets = new ArrayList<>();

        if ("".equals(topicOrDataIn) || topicOrDataIn == null)
        {
            logger.warning("No input topic or file defined yet.");
            return;
        }

        if (SourceDestination.KAFKA.equals(source))
        {
            synchronized (kafkaConsumer)
            {
                kafkaConsumer.poll(Duration.of(timeout, ChronoUnit.MILLIS)).forEach(record -> {
                    dataSets.add(record.value());
                });
            }
        }
        else if (SourceDestination.HADOOP.equals(source))
        {
            FSDataInputStream fileStream;
            org.apache.hadoop.fs.Path path = new org.apache.hadoop.fs.Path(topicOrDataIn);

            if (inputFileSystem.exists(path))
            {
                fileStream = inputFileSystem.open(path);
            }
            else
            {
                logger.warning(String.format("File %s does not exist in hadoop", path));
                return;
            }

            byte[] readBytes = new byte[]{};
            fileStream.readFully(readerPos, readBytes);
            readerPos = fileStream.getPos();

            String readString = new String(readBytes);
            String[] splitReadString = readString.split("\n");
            dataSets.addAll(Arrays.asList(splitReadString));
        }
        else
        {
            logger.severe("Unknown data input");
            return;
        }

        if (dataSets.size() < minDataSetSize)
        {
            logger.info("Minimal data set size not reached (yet?)");
        }

        collectedData.addAll(dataSets);
    }

    private void saveData(String data)
    {
        if (topicOrDataOut == null || "".equals(topicOrDataOut))
        {
            logger.warning("No output topic or file defined yet.");
            return;
        }

        if (SourceDestination.KAFKA.equals(destination))
        {
            kafkaProducer.beginTransaction();

            final ProducerRecord<Long, String> record =
                    new ProducerRecord<>(topicOrDataOut, System.currentTimeMillis(), data);

            kafkaProducer.send(record);
            kafkaProducer.flush();
            kafkaProducer.commitTransaction();
        }
        else if (SourceDestination.HADOOP.equals(destination))
        {
            org.apache.hadoop.fs.Path path = new org.apache.hadoop.fs.Path(topicOrDataOut);
            try (FSDataOutputStream fileStream = outputFileSystem.create(path))
            {
                logger.info(String.format("Trying to write '%s' to hadoop", data));
                fileStream.writeChars(data.concat("\n"));
                fileStream.flush();
                logger.info("Wrote to hadoop");
            }
            catch (IOException e)
            {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.severe(
                        String.format("Can not write to output for file %s in hadoop:\n%s", topicOrDataIn,
                                      sw.toString()));
            }
        }
        else
        {
            logger.severe("Unknown data output");
        }
    }

    private Consumer<Long, String> constructKafkaConsumer()
    {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        final Consumer<Long, String> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Collections.singletonList(topicOrDataIn));
        return consumer;
    }

    private Producer<Long, String> constructKafkaProducer()
    {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "Producer@".concat(this.uuid.toString()));

        KafkaProducer<Long, String> producer = new KafkaProducer<>(props);
        producer.initTransactions();
        return producer;
    }

    private FileSystem constructHadoopFileSystem(org.apache.hadoop.fs.Path path) throws IOException
    {
        String pathToConfigFile = System.getProperty("jboss.server.config.dir")
                .concat("/greenlake/greenlake-properties.json");

        JSONObject configFileJSON = new JSONObject(Files.readString(Paths.get(pathToConfigFile)));
        String pathToHadoop = configFileJSON.getString("pathToHadoop");

        Configuration conf = new Configuration();
        conf.addResource(new org.apache.hadoop.fs.Path(pathToHadoop.concat("/etc/hadoop/core-site.xml")));
        conf.addResource(new org.apache.hadoop.fs.Path(pathToHadoop.concat("/etc/hadoop/hdfs-site.xml")));

        return path.getFileSystem(conf);
    }

    private static class LogHandler extends FileHandler
    {
        public LogHandler(String pathToFile) throws IOException, SecurityException
        {
            super(pathToFile);
            setFormatter(new SimpleFormatter());
        }
    }
}
