package jsonserver.alikeaudience.com;


import io.netty.util.concurrent.Future;
import org.apache.kafka.clients.producer.*;

import java.util.Properties;

/**
 * Created by AlikeAudience on 22/9/2016.
 */
public final class JsonKafkaProducer {

    private static volatile JsonKafkaProducer instance;

    private Properties props;
    private Producer<String, String> producer;

    private JsonKafkaProducer() {
        props = new Properties();
        props.put("bootstrap.servers", "192.168.1.22:9092"); //the addresses of the kafka brokers
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

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
        ProducerRecord<String, String> record = new ProducerRecord<String, String>("test1", "sdk", jsonData);
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
