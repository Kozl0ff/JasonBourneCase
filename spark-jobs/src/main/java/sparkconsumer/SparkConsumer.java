package sparkconsumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.*;

public final class SparkConsumer {
    public static void main(String[] args) throws Exception {
        SparkConf sparkConf = new SparkConf().setAppName("JavaDirectKafkaWordCount");
        JavaStreamingContext javaStreamingContext =
                new JavaStreamingContext(sparkConf, Durations.seconds(10));
        javaStreamingContext.checkpoint("./spark-checkpoint/test-word-count");

        String bootstrapServer;
        String topic;
        if (args.length == 2) {
             bootstrapServer = args[0];
             topic = args[1];
        } else {
            throw new IllegalArgumentException("We will wait two arguments");
        }
        Set<String> topicsSet = new HashSet<>(Collections.singletonList(topic));
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "SparkConsumer");
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        JavaInputDStream<ConsumerRecord<Long, String>> messages = KafkaUtils.createDirectStream(
                javaStreamingContext,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topicsSet, kafkaParams));
        JavaDStream<String> lines = messages.map(ConsumerRecord::value);

        lines.foreachRDD(rdd ->{
            rdd.repartition(1);
            if (!rdd.isEmpty())
                rdd.saveAsTextFile("/user/root/" + System.currentTimeMillis());
            else
                System.out.println("Is empty");
        });
        javaStreamingContext.start();
        javaStreamingContext.awaitTermination();
    }
}
