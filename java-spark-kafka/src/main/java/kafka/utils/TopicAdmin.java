package kafka.utils;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class TopicAdmin {
    public static void createTopicIfNotExists(String bootstrapServer, String topic)
        throws ExecutionException, InterruptedException {

        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);

        AdminClient admin = AdminClient.create(config);
        ListTopicsResult listTopics = admin.listTopics();
        Set<String> names = listTopics.names().get();
        boolean contains = names.contains(topic);
        if (!contains) {
            List<NewTopic> topicList = new ArrayList<>();
            Map<String, String> configs = new HashMap<>();
            int partitions = 3;
            short replication = 1;
            NewTopic newTopic = new NewTopic(topic, partitions, replication).configs(configs);
            topicList.add(newTopic);
            admin.createTopics(topicList);
        }
        admin.close();
    }
}
