package kafka;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.json.JSONArray;
import org.json.JSONObject;
import util.LoggedClientCompatibleException;

import javax.management.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Utility to interact with Kafka and Zookeeper
 */
public class KafkaUtil
{
    private static final Logger LOGGER = Logger.getLogger(KafkaUtil.class.getName());
    private static KafkaUtil instance;
    private String kafkaURL;
    private String zookeeperURL;

    /**
     * Private constructor for singleton pattern
     */
    private KafkaUtil()
    {
        getKafkaAndZookeeperLocation();
    }

    /**
     * @return Instance of KafkaUtil
     */
    public static KafkaUtil getInstance()
    {
        if (instance == null)
        {
            instance = new KafkaUtil();
        }
        return instance;
    }

    /**
     * @return true if a connection to zookeeper could be established
     */
    public boolean zookeeperAvailable()
    {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zookeeperURL)
                .retryPolicy(new RetryNTimes(0, 0))
                .build();

        try (client)
        {
            client.start();
            client.blockUntilConnected(3, TimeUnit.SECONDS);
            boolean available = client.getZookeeperClient().isConnected();
            client.close();
            return available;
        }
        catch (InterruptedException ignored)
        {
            client.close();
            LOGGER.info("Zookeeper was not available");
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * @return true if a connection to kafka could be established
     */
    public boolean kafkaAvailable()
    {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(kafkaURL)
                .retryPolicy(new RetryNTimes(0, 0))
                .build();

        try (client)
        {
            client.start();
            client.blockUntilConnected(3, TimeUnit.SECONDS);
            if (!client.getZookeeperClient().isConnected())
            {
                return false;
            }

            ZooKeeper zookeeper = new ZooKeeper(zookeeperURL, 3000, watchedEvent -> {});
            boolean available = zookeeper.getChildren("/brokers/ids", false).size() > 0;
            ;
            zookeeper.close();
            client.close();
            return available;
        }
        catch (InterruptedException | IOException | KeeperException ignored)
        {
            client.close();
            LOGGER.info("Kafka was not available");
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Creates a topic
     *
     * @param alias Name for this topic
     * @param partitions Number of partitions for this topic
     * @param replications Number of replicas for this topic
     */
    public void createTopic(String alias, int partitions, short replications)
    {
        AdminClient adminClient = createAdminClient();
        NewTopic topic = new NewTopic(alias, partitions, replications);
        adminClient.createTopics(Collections.singletonList(topic));
        adminClient.close();
    }

    /**
     * Deletes a given topic
     *
     * @param alias alias of the topic to delete
     */
    public void deleteTopic(String alias)
    {
        createAdminClient().deleteTopics(Collections.singletonList(alias));
    }

    /**
     * @return topics in form of JSONArray(JSONObject o1, ..., JSONObject on), JSONObject has the form {name: ...,
     *         partitions: ..., replications: ...}
     * @throws LoggedClientCompatibleException if an exception occurred meanwhile
     */
    public JSONArray listTopics() throws LoggedClientCompatibleException
    {
        try
        {
            return listTopicsHelper();
        }
        catch (ExecutionException | InterruptedException e)
        {
            throw new LoggedClientCompatibleException(e, LOGGER);
        }
    }

    /**
     * Creates a Connection as Admin Client
     *
     * @return created admin client
     */
    private AdminClient createAdminClient()
    {
        Map<String, Object> clientConfig = new HashMap<>()
        {
            {
                put("bootstrap.servers", kafkaURL);
                put("client.id", UUID.randomUUID().toString());
                put("connections.max.idle.ms", 10000);
                put("request.timeout.ms", 3000);
                put("retries", 0);
            }
        };

        return KafkaAdminClient.create(clientConfig);
    }

    /**
     * Helper method that safes the hustle for multiple try catches
     *
     * @return topics in form of JSONArray(JSONObject o1, ..., JSONObject on), JSONObject has the form {name: ..., *
     *         partitions: ..., repliactions: ...}
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private JSONArray listTopicsHelper() throws ExecutionException, InterruptedException
    {
        AdminClient adminClient = createAdminClient();

        JSONArray topics = new JSONArray();

        List<String> topicNames = new ArrayList<>(adminClient.listTopics().names().get());
        Map<String, TopicDescription> topicDescriptions = adminClient.describeTopics(topicNames).all()
                .get();

        topicNames.forEach(name -> {
            TopicDescription description = topicDescriptions.get(name);
            int partitions = description.partitions().size();
            int replicas = description.partitions().get(0).replicas().size();

            topics.put(new JSONObject().put("name", name).put("partitions", partitions).put("replications", replicas));
        });

        return topics;
    }

    private void getKafkaAndZookeeperLocation()
    {
        try
        {
            String pathToConfigFile = System.getProperty("jboss.server.config.dir")
                    .concat("/greenlake/apps/kafka-configuration/kafka-configuration.json");

            File configFile = new File(pathToConfigFile);

            if (configFile.exists())
            {
                String fileContent = Files.readString(Paths.get(pathToConfigFile));

                JSONObject fileContentAsJSON = new JSONObject(fileContent);

                if (fileContentAsJSON.has("kafkaURL") && fileContentAsJSON.has("zookeeperURL"))
                {
                    kafkaURL = fileContentAsJSON.getString("kafkaURL");
                    zookeeperURL = fileContentAsJSON.getString("zookeeperURL");
                    return;
                }
                else
                {
                    LOGGER.warning("The kafka-configuration.json file is invalid. Using defaults.");
                }
            }
        }
        catch (Exception e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();

            LOGGER.warning("An exception occured while trying to read the kafka-configuration.json. Using defaults. \n "
                                   .concat(exceptionAsString));
        }

        kafkaURL = "0.0.0.0:9092";
        TestingServer zkTestServer = null;
        try {
            zkTestServer = new TestingServer(31313);
        } catch (Exception e) {
            e.printStackTrace();
        }
        zookeeperURL = zkTestServer.getConnectString();
    }
}
