package kafka.pageclickgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
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

    static void runProducer(String bootstrapServer, String topic,
                            String urlsPath, String cookieEmailMappingsPath,
                            Integer pageClicksCount)
        throws Exception {

        final Producer<Long, String> producer = createProducer(bootstrapServer);
        long time = System.currentTimeMillis();
        PageClickGenerator pageClickGenerator = new PageClickGenerator(urlsPath, cookieEmailMappingsPath);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            for (int i = 0; i < pageClicksCount; i++) {
                final ProducerRecord<Long, String> record =
                    new ProducerRecord<>(topic, System.currentTimeMillis(),
                        objectMapper.writeValueAsString(pageClickGenerator.getNext()));
                producer.send(record).get();
                if (i % 1000 == 0) {
                    System.out.println(i + " records sent");
                }
            }
        } finally {
            producer.flush();
            producer.close();
        }
    }
}
