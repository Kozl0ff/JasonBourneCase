package joinandwritetables;

import models.PageClick;
import models.Person;
import org.apache.spark.sql.*;
import utils.SparkJobsUtils;

import java.util.Properties;

public class JoinAndWriteTables {
    public static final String mySqlUrl = "jdbc:mysql://172.18.64.79:3306";

    public static void main(String[] args) {
        SparkSession sparkSession = SparkSession.builder()
                .appName("joinAndWriteTables")
                .getOrCreate();

        String pageClicksPath = "/user/root/page-clicks/stream-*/part-*";
        String peoplePath = "/user/root/peopleWithUUID.csv";
        String cookieEmailsPath = "/user/root/userCookieEmailMappings.csv";

        Properties connectionProperties = new Properties();
        connectionProperties.put("driver", "com.mysql.jdbc.Driver");
        connectionProperties.put("user", "user");
        connectionProperties.put("password", "user");

        Encoder<PageClick> pageClickEncoder = Encoders.bean(PageClick.class);
        if (args.length >= 3) {
            pageClicksPath = args[0];
            peoplePath = args[1];
            cookieEmailsPath = args[2];
        }
        Dataset<PageClick> pageClickDataset = sparkSession.read()
                .json(pageClicksPath)
                .as(pageClickEncoder);

        Encoder<Person> personEncoder = Encoders.bean(Person.class);
        Dataset<Person> personDataset = sparkSession.read()
                .option("header", "true")
                .csv(peoplePath)
                .as(personEncoder);

        Dataset<Row> cookieEmailsDataset = sparkSession.read()
                .option("header", "true")
                .csv(cookieEmailsPath);

        Dataset<Row> pageClickJoinCookie = pageClickDataset
                .join(cookieEmailsDataset, "cookie");
        Dataset<Row> joinByEmail = pageClickJoinCookie
                .join(personDataset, "email");
        joinByEmail.show(5);

        Dataset<Row> genderAndBirthDateDs = joinByEmail.select("gender", "birthDate").cache();

        RelationalGroupedDataset gender = joinByEmail.groupBy("gender");
        Dataset<Row> genderCount = gender.count()
                .withColumn("date_and_time", functions.current_timestamp());
        genderCount.show(5);

        RelationalGroupedDataset groupedByYearString = genderAndBirthDateDs
                .withColumn(
                        "year",
                        genderAndBirthDateDs
                                .col("birthDate")
                                .substr(0, 4)
                )
                .groupBy("year");
        Dataset<Row> birthDateCount = groupedByYearString
                .count()
                .withColumn("date_and_time", functions.current_timestamp());

        System.out.println("Checking if we counted all rows:");
        System.out.println("Total count of rows: " + genderAndBirthDateDs.count());
        System.out.println("Sum by year total: ");
        birthDateCount.drop("birthDate").agg(functions.sum("count")).show();

        System.out.println("BirthDate:::");
        birthDateCount.show(10);

        SparkJobsUtils.writeToMySql(genderCount, mySqlUrl, "bdata.gender", connectionProperties);
        SparkJobsUtils.writeToMySql(birthDateCount, mySqlUrl, "bdata.birthDate", connectionProperties);
    }
}
