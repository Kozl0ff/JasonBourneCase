import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.IOException;
import java.util.List;

public class ReadFromHdfs {

    public static void main(String args[]) throws IOException {

        SparkConf conf = new SparkConf().setAppName("appName");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc.textFile(args[0]);
        List<String> collect = lines.collect();
        System.out.println("Content of the files - lines");
        for (String l : collect) {
            System.out.println(l);
        }
    }
}
