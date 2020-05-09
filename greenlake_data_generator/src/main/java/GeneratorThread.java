import GreenhouseDataModels.*;

import javafx.util.Pair;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

public class GeneratorThread extends Thread {
    final Logger logger = LoggerFactory.getLogger(GeneratorThread.class);

    private int greenhouseId;
    private int greenhouseType;
    private boolean runThread;

    private Producer<String, String> producer;
    private CalendarConverter converter;

    private GeneratorMonth january;
    private GeneratorMonth february;
    private GeneratorMonth march;
    private GeneratorMonth april;
    private GeneratorMonth may;
    private GeneratorMonth june;
    private GeneratorMonth july;
    private GeneratorMonth august;
    private GeneratorMonth september;
    private GeneratorMonth october;
    private GeneratorMonth november;
    private GeneratorMonth december;

    public GeneratorThread(int id, int alternative) {
        greenhouseId = id;
        greenhouseType = alternative;
        runThread = true;
        initialize();
    }

    public void run() {
        System.out.println("Generator-Thread started with Greenhouse-ID " + greenhouseId + " and Type " + greenhouseType);
        GreenhouseData greenhouseData = null;
        GreenhouseData lastEntry = null;
        int secondInterval = 300;
        switch (greenhouseType) {
            case 1:
                greenhouseData = new StandardGreenhouseData(greenhouseId);
                lastEntry = new StandardGreenhouseData(greenhouseType);
                break;
            case 2:
                greenhouseData = new AlternativeOneGreenhouseData(greenhouseId);
                lastEntry = new AlternativeOneGreenhouseData(greenhouseType);
                break;
        }

        Calendar calendar = Calendar.getInstance();
        System.out.println("Rufe letzten Eintrag für gegebene ID ab falls vorhanden");

        try {
            Properties props = new Properties();
            props.put(BOOTSTRAP_SERVERS_CONFIG, "192.168.2.133:9092");
            props.put(GROUP_ID_CONFIG, "test-consumer-group");
            props.put(ENABLE_AUTO_COMMIT_CONFIG, "true");
            props.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
            props.put(SESSION_TIMEOUT_MS_CONFIG, "30000");
            props.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList("generator-test"));
            consumer.poll(Duration.ofSeconds(10));
            consumer.assignment();
            AtomicLong maxTimestamp = new AtomicLong();
            AtomicReference<ConsumerRecord<String, String>> latestRecord = new AtomicReference<>();
            // get the last offsets for each partition
            consumer.endOffsets(consumer.assignment()).forEach((topicPartition, offset) -> {
                // seek to the last offset of each partition
                consumer.seek(topicPartition, (offset == 0) ? offset : offset - 1);
                // poll to get the last record in each partition
                consumer.poll(Duration.ofSeconds(10)).forEach(record -> {
                    // the latest record in the 'topic' is the one with the highest timestamp
                    if (record.timestamp() > maxTimestamp.get() && record.value().contains("\"greenhouseID\":" + greenhouseId)) {
                        maxTimestamp.set(record.timestamp());
                        latestRecord.set(record);
                    }
                });
            });

            JSONObject jsonRecord = new JSONObject(latestRecord.get().value());
            calendar = converter.ConvertToCalendar(jsonRecord.getJSONObject("timestamp"));
            logger.info("Latest entry received from Kafka");

            switch(greenhouseType){
                case 1:
                    lastEntry.setTime(calendar);
                    lastEntry.setTempSensValue1(jsonRecord.getFloat("temperatureOutside"));
                    lastEntry.setTempSensValue2(jsonRecord.getFloat("temperatureInside"));
                    lastEntry.setHumiditySensValue1(jsonRecord.getFloat("humidityOutside"));
                    lastEntry.setHumiditySensValue2(jsonRecord.getFloat("humidityInside"));
                    lastEntry.setBrightnessSensValue(jsonRecord.getFloat("brightness"));
                    lastEntry.setMoistureSensValue1(jsonRecord.getInt("moisturePlant1"));
                    ((StandardGreenhouseData) lastEntry).setMoistureSensValue2(jsonRecord.getInt("moisturePlant2"));
                    ((StandardGreenhouseData) lastEntry).setMoistureSensValue3(jsonRecord.getInt("moisturePlant3"));
                    ((StandardGreenhouseData) lastEntry).setMoistureSensValue4(jsonRecord.getInt("moisturePlant4"));
                    break;
                case 2:
                    lastEntry.setTime(calendar);
                    lastEntry.setTempSensValue1(jsonRecord.getFloat("temperatureOutside"));
                    lastEntry.setTempSensValue2(jsonRecord.getFloat("temperatureInside"));
                    lastEntry.setHumiditySensValue1(jsonRecord.getFloat("humidityOutside"));
                    lastEntry.setHumiditySensValue2(jsonRecord.getFloat("humidityInside"));
                    lastEntry.setBrightnessSensValue(jsonRecord.getFloat("brightness"));
                    lastEntry.setMoistureSensValue1(jsonRecord.getInt("moisturePlant1"));
                    ((AlternativeOneGreenhouseData) lastEntry).setMoistureSensValue2(jsonRecord.getInt("moisturePlant2"));
                    break;
            }
        }
        catch (Exception e) {
            logger.error("Couldn't receive data from Kafka, proceeding with standard values.\n" +
                    "Error: " + e.getMessage());
            calendar.set(Calendar.AM_PM, 0);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            switch(greenhouseType) {
                case 1:
                    lastEntry.setTime(calendar);
                    lastEntry.setTempSensValue1(8);
                    lastEntry.setTempSensValue2(18);
                    lastEntry.setHumiditySensValue1(52);
                    lastEntry.setHumiditySensValue2(78);
                    lastEntry.setBrightnessSensValue(0);
                    lastEntry.setMoistureSensValue1(70);
                    ((StandardGreenhouseData) lastEntry).setMoistureSensValue2(70);
                    ((StandardGreenhouseData) lastEntry).setMoistureSensValue3(70);
                    ((StandardGreenhouseData) lastEntry).setMoistureSensValue4(70);
                    break;
                case 2:
                    lastEntry.setTime(calendar);
                    lastEntry.setTempSensValue1(8);
                    lastEntry.setTempSensValue2(18);
                    lastEntry.setHumiditySensValue1(52);
                    lastEntry.setHumiditySensValue2(78);
                    lastEntry.setBrightnessSensValue(0);
                    lastEntry.setMoistureSensValue1(70);
                    ((AlternativeOneGreenhouseData) lastEntry).setMoistureSensValue2(70);
                    break;
            }
        }

        int currentMonthNumber;
        int oldMonthNumber = 0;
        int monthRainDays = 0;
        GeneratorMonth currentMonth = null;
        List<GreenhouseData> list = null;
        Pair<List<GreenhouseData>, Integer> result = null;

        while (runThread) {
            if (result != null) {
                list = result.getKey();
                lastEntry = list.get(list.size() - 1);
                calendar = lastEntry.getTime();
                calendar.add(Calendar.DAY_OF_WEEK, 1);
                calendar.set(Calendar.AM_PM, 0);
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                lastEntry.setTime(calendar);
            }
            currentMonthNumber = lastEntry.getTime().get(Calendar.MONTH);
            if (oldMonthNumber != currentMonthNumber) {
                switch (currentMonthNumber) {
                    case 0:
                        currentMonth = january;
                        break;
                    case 1:
                        currentMonth = february;
                        break;
                    case 2:
                        currentMonth = march;
                        break;
                    case 3:
                        currentMonth = april;
                        break;
                    case 4:
                        currentMonth = may;
                        break;
                    case 5:
                        currentMonth = june;
                        break;
                    case 6:
                        currentMonth = july;
                        break;
                    case 7:
                        currentMonth = august;
                        break;
                    case 8:
                        currentMonth = september;
                        break;
                    case 9:
                        currentMonth = october;
                        break;
                    case 10:
                        currentMonth = november;
                        break;
                    case 11:
                        currentMonth = december;
                        break;
                }
                oldMonthNumber = currentMonthNumber;
                monthRainDays = 0;
            }
            result = greenhouseData.generateNewDay(secondInterval, monthRainDays, currentMonth, lastEntry);
            monthRainDays = result.getValue();

            List<GreenhouseData> entries = result.getKey();
            for (GreenhouseData entry : entries) {
                String key = String.valueOf(greenhouseId);
                String jsonString = "";
                switch (greenhouseType) {
                    case 1:
                        StandardGreenhouseData standardEntry = (StandardGreenhouseData) entry;
                        jsonString = new JSONObject()
                                .put("greenhouseID", 1)
                                .put("timestamp", converter.ConvertToJson(standardEntry.getTime()))
                                .put("temperatureOutside", standardEntry.getTempSensValue1())
                                .put("temperatureInside", standardEntry.getTempSensValue2())
                                .put("humidityOutside", standardEntry.getHumiditySensValue1())
                                .put("humidityInside", standardEntry.getHumiditySensValue2())
                                .put("brightness", standardEntry.getBrightnessSensValue())
                                .put("moisturePlant1", standardEntry.getMoistureSensValue1())
                                .put("moisturePlant2", standardEntry.getMoistureSensValue2())
                                .put("moisturePlant3", standardEntry.getMoistureSensValue3())
                                .put("moisturePlant4", standardEntry.getMoistureSensValue4())
                                .toString();
                        break;
                    case 2:
                        AlternativeOneGreenhouseData altOneEntry = (AlternativeOneGreenhouseData) entry;
                        jsonString = new JSONObject()
                                .put("greenhouseID", 2)
                                .put("timestamp", altOneEntry.getTime())
                                .put("temperatureOutside", altOneEntry.getTempSensValue1())
                                .put("temperatureInside", altOneEntry.getTempSensValue2())
                                .put("humidityOutside", altOneEntry.getHumiditySensValue1())
                                .put("humidityInside", altOneEntry.getHumiditySensValue2())
                                .put("brightness", altOneEntry.getBrightnessSensValue())
                                .put("moisturePlant1", altOneEntry.getMoistureSensValue1())
                                .put("moisturePlant2", altOneEntry.getMoistureSensValue2())
                                .toString();
                        break;
                }

                try {
                    if (!runThread && entries.indexOf(entry) == entries.size()-1) {
                        System.out.println("Warte auf Abschluss der Übertragung");
                        producer.send(new ProducerRecord<>("generator-test", key, jsonString)).get();
                    } else {
                        producer.send(new ProducerRecord<>("generator-test", key, jsonString));
                    }
                    logger.info("New entry send to Kafka: " + jsonString);
                }
                catch (Exception e) {
                    logger.error("Sending entry via Kafka failed: Entry: " + jsonString + " | error: " + e.getMessage());
                }
            }
        }

        producer.close();
    }

    public void stopExecution() {
        runThread = false;
        logger.info("Stopped execution - stop after finishing generation of current day");
    }

    private void initialize() {
        january = new GeneratorMonth(Season.WINTER, -2, -15, 4, 15, LocalTime.of(7,45), LocalTime.of(16, 45), (float) 1.5, 12, 85);
        february = new GeneratorMonth(Season.WINTER, -2, -15, 6, 17, LocalTime.of(7,20), LocalTime.of(17, 20), (float) 2.8, 9, 82);
        march = new GeneratorMonth(Season.SPRING, 1, -5, 10, 20, LocalTime.of(6, 15), LocalTime.of(18, 15), (float) 4.2, 10, 79);
        april = new GeneratorMonth(Season.SPRING, 3, -5, 14, 25, LocalTime.of(6, 10), LocalTime.of(19, 40), (float) 6.3, 10, 74);
        may = new GeneratorMonth(Season.SPRING, 7, 0, 19, 25, LocalTime.of(5,20), LocalTime.of(20, 20), (float) 6.9, 12, 71);
        june = new GeneratorMonth(Season.SUMMER, 11, 5, 22, 35, LocalTime.of(5,00), LocalTime.of(21, 00), (float) 7.4, 11, 72);
        july = new GeneratorMonth(Season.SUMMER, 13, 5, 24, 35, LocalTime.of(5,20), LocalTime.of(20, 50), (float) 7.2, 11, 71);
        august = new GeneratorMonth(Season.SUMMER, 12, 5, 25, 35, LocalTime.of(5,50), LocalTime.of(19, 50), (float) 6.8, 10, 74);
        september = new GeneratorMonth(Season.FALL, 9, 0, 20, 25, LocalTime.of(6,40), LocalTime.of(19, 10), (float) 5.3, 8, 79);
        october = new GeneratorMonth(Season.FALL, 6, -5, 15, 20, LocalTime.of(7, 20), LocalTime.of(18, 20), (float) 3.6, 10,82);
        november = new GeneratorMonth(Season.FALL, 1, -10, 8, 17, LocalTime.of(7, 20), LocalTime.of(16, 50), (float) 1.8, 11, 84);
        december = new GeneratorMonth(Season.WINTER, -1, -15, 4, 15, LocalTime.of(7,50), LocalTime.of(16, 20), (float) 1.2, 12, 85);

        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, "192.168.2.133:9092");
        props.put(ACKS_CONFIG, "all");
        props.put(RETRIES_CONFIG, 0);
        props.put(BATCH_SIZE_CONFIG, 16000);
        props.put(LINGER_MS_CONFIG, 100);
        props.put(BUFFER_MEMORY_CONFIG, 33554432);
        props.put(KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);
        converter = new CalendarConverter();
    }
}
