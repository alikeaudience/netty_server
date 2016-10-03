package jsonserver.alikeaudience.com;


import io.netty.util.concurrent.Future;
import org.apache.kafka.clients.producer.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by AlikeAudience on 22/9/2016.
 */
public final class JsonKafkaProducer {

    private static volatile JsonKafkaProducer instance;

    private final Properties props;
    private Producer<String, String> producer;

    private final String topicName;
    private final String topicKey;

    private JsonKafkaProducer() {
        props = new Properties();

        if(HttpJsonServer.kafkaConfigFile != null) {

            try {
                props.load(new FileInputStream(HttpJsonServer.kafkaConfigFile));
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            props.put("bootstrap.servers", "192.168.1.23:9092"); //the addresses of the kafka brokers
            props.put("acks", "all");
            props.put("retries", 0);
            props.put("batch.size", 16384);
            props.put("linger.ms", 1);
            props.put("buffer.memory", 33554432);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        }

        System.out.println("Instantiate a new instance of Kafka producer class with the following settings: ");
        System.out.println(" - The Kafka producer properties file is " + props);

        if(HttpJsonServer.topicName != null) topicName = HttpJsonServer.topicName;
        else topicName = "test";

        System.out.println(" - The topic name is " + topicName);

        if(HttpJsonServer.topicKey != null) topicKey = HttpJsonServer.topicKey;
        else topicKey = "test";

        System.out.println(" - The topic key is " + topicKey);

        producer = new KafkaProducer<>(props);
    }

    /**
     * Get the only instance of this class.
     *
     * @return the single instance.
     */
    public static JsonKafkaProducer getInstance() {
        if (instance == null) {
            synchronized (JsonKafkaProducer.class) {
                if (instance == null) {
                    instance = new JsonKafkaProducer();
                }
            }
        }
        return instance;
    }

    public void sendToKafka(String jsonData) {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topicName, topicKey, jsonData);
        producer.send(record,
                new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if(e != null) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    public void closeProducer() {
        producer.close();
    }
}
