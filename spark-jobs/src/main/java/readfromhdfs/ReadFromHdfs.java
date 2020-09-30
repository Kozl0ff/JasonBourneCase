package readfromhdfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import peoplegenerator.Person;


import java.io.IOException;
import java.util.List;

public class ReadFromHdfs {

    public static void main(String args[]) throws IOException {

        SparkConf conf = new SparkConf().setAppName("ReadFromHDFS");
        JavaSparkContext sc = new JavaSparkContext(conf);
        ObjectMapper objectMapper = new ObjectMapper();

        JavaRDD<String> lines = sc.textFile(args[0]);
        JavaRDD<Person> map = lines.map(s -> objectMapper.readValue(s, Person.class));

        map.take(5);
        List<Person> list = map.collect();

        for (Person person : list) {
            System.out.println(person.getUuid());
        }
    }
}
