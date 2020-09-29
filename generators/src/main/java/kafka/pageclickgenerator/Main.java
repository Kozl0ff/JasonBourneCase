package kafka.pageclickgenerator;

import kafka.utils.KafkaUtils;
import kafka.utils.TopicAdmin;

public class Main {

    public static void main(String[] args) throws Exception {
        String bootstrapServer = KafkaUtils.BOOTSTRAP_SERVERS;
        String topic = KafkaUtils.TOPIC;
        String urlsPath = "generators/src/main/resources/urls.csv";
        if (args.length >= 2) {
            bootstrapServer = args[0];
            topic = args[1];
        }
        if (args.length >= 3) {
            urlsPath = args[2];
        }
        TopicAdmin.createTopicIfNotExists(bootstrapServer, topic);
        KafkaProducer.runProducer(bootstrapServer, topic, urlsPath);
    }
}
