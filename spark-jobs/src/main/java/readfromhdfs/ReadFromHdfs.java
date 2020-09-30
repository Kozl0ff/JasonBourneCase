package readfromhdfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.PageClick;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.List;

public class ReadFromHdfs {

    public static void main(String args[]) {
        SparkConf conf = new SparkConf().setAppName("ReadFromHDFS");
        JavaSparkContext sc = new JavaSparkContext(conf);
        ObjectMapper objectMapper = new ObjectMapper();

        System.out.println("Starting using SparkConf:");
        boolean usingYarn = false;
        for (Tuple2<String, String> t : sc.getConf().getAll()) {
            System.out.println(t._1 + ": " + t._2);
            if (t._1.startsWith("spark.org.apache.hadoop.yarn.")) {
                usingYarn = true;
            }
        }

        String filename = args[0];
        if (usingYarn) {
            System.out.println("Reading file from HDFS: " + filename);
        } else {
            System.out.println("Reading file from local FS: " + filename);
        }
        JavaRDD<String> lines = sc.textFile(filename);
        JavaRDD<PageClick> pageClick = lines.map(s -> objectMapper.readValue(s, PageClick.class));

        List<PageClick> list = pageClick.take(5);
        System.out.println("Up to 5 samples (URLs):");
        for (PageClick l : list) {
            System.out.println(l.getUrl());
        }
    }
}
