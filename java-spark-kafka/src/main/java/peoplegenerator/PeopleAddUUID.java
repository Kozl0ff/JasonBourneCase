package peoplegenerator;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.*;

public class PeopleAddUUID {
    public static void main(String[] args) {
        SparkSession sparkSession = SparkSession.builder()
            .appName("Java Spark read people from CSV")
            .master("local[*]")
            .getOrCreate();

        Encoder<Person> personEncoder = Encoders.bean(Person.class);
        Dataset<Person> personDataset = sparkSession.read()
            .option("header", "true")
            .csv("src/main/resources/people.csv")
            .as(personEncoder);

        Dataset<Person> personDatasetWithUUID = personDataset.map(
            (MapFunction<Person, Person>) row ->
                new Person(row.getFirstName(), row.getLastName(), row.getBirthDate(),
                    row.getGender(), row.getEmail(), row.getDeviceID()), personEncoder);
        personDataset.show();
        personDataset.select("uuid").show();
        personDatasetWithUUID.show();
        personDatasetWithUUID.select("uuid").show();
        personDatasetWithUUID.write()
            .mode(SaveMode.Overwrite)
            .option("header", "true")
            .csv("src/main/resources/peopleWithUUID.csv");
    }
}
