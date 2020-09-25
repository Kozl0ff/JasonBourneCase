import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function3;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import scala.Tuple2;
import java.util.*;
import java.util.regex.Pattern;

public final class JavaDirectKafkaWordCount {
    private static final Pattern SPACE = Pattern.compile(" ");
    public static void main(String[] args) throws Exception {
        SparkConf sparkConf = new SparkConf().setAppName("JavaDirectKafkaWordCount");
        JavaStreamingContext javaStreamingContext =
                new JavaStreamingContext(sparkConf, Durations.seconds(10));
        javaStreamingContext.checkpoint("./spark-checkpoint/test-word-count");
        String bootstrapServer = "172.18.64.79:9092";//kafka:9092
        String topic = "quickstart-events";//qazx
        if (args.length == 2) {
            bootstrapServer = args[0];
            topic = args[1];
        }
        Set<String> topicsSet = new HashSet<>(Collections.singletonList(topic));
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "JavaDirectKafkaWordCount");
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // Create direct kafka stream with brokers and topics
        JavaInputDStream<ConsumerRecord<Long, String>> messages = KafkaUtils.createDirectStream(
                javaStreamingContext,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topicsSet, kafkaParams));
//        List<Tuple2<String, Integer>> tuples = new ArrayList<>();
        JavaPairRDD<String, Integer> initialRDD = javaStreamingContext.sparkContext()
                .parallelizePairs(new ArrayList<>());
        Function3<String, Optional<Integer>, State<Integer>, Tuple2<String, Integer>> mappingFunc =
                (word, one, state) -> {
                    int sum = one.orElse(0) + (state.exists() ? state.get() : 0);
                    Tuple2<String, Integer> output = new Tuple2<>(word, sum);
                    state.update(sum);
                    return output;
                };
        // Get the lines, split them into words, count the words and print
        JavaDStream<String> lines = messages.map(ConsumerRecord::value);
        JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(SPACE.split(x)).iterator());
        JavaPairDStream<String, Integer> wordCounts = words.mapToPair(s -> new Tuple2<>(s, 1));
        JavaMapWithStateDStream<String, Integer, Integer, Tuple2<String, Integer>> stateDstream =
                wordCounts.mapWithState(StateSpec.function(mappingFunc).initialState(initialRDD));
        //stateDstream.print();
        //stateDstream.print(2);
        //stateDstream.dstream().saveAsTextFiles("/user/root","json");
        stateDstream.foreachRDD(rdd ->{
                rdd.repartition(1);
            if (!rdd.isEmpty())
                rdd.saveAsObjectFile("/user/root/" + System.currentTimeMillis());
            else
                System.out.println("Is empty");
        });
        // Start the computation
        javaStreamingContext.start();
        javaStreamingContext.awaitTermination();
    }
}
