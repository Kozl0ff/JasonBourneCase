package kafka.pageclickgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaProducer {
    private static Producer<Long, String> createProducer(String bootstrapServer) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "PageClickKafkaGenerator");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class.getName());
        return new org.apache.kafka.clients.producer.KafkaProducer<>(props);
    }

    static void runProducer(String bootstrapServer, String topic, String urlsPath) throws Exception {
        final Producer<Long, String> producer = createProducer(bootstrapServer);
        long time = System.currentTimeMillis();
        PageClickGenerator pageClickGenerator = new PageClickGenerator(urlsPath);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            while (true) {
                final ProducerRecord<Long, String> record =
                    new ProducerRecord<>(topic, System.currentTimeMillis(),
                        objectMapper.writeValueAsString(pageClickGenerator.getNext()));

                RecordMetadata metadata = producer.send(record).get();

                long elapsedTime = System.currentTimeMillis() - time;
                System.out.printf("sent record(key=%s value=%s) " +
                        "meta(partition=%d, offset=%d) time=%d\n",
                    record.key(), record.value(), metadata.partition(),
                    metadata.offset(), elapsedTime);

                Thread.sleep(1000);
            }
        } finally {
            producer.flush();
            producer.close();
        }
    }
}
