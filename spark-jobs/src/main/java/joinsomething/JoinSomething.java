package joinsomething;

import models.PageClick;
import models.Person;
import org.apache.spark.sql.*;

public class JoinSomething {
    public static void main(String[] args) {
        SparkSession sparkSession = SparkSession.builder()
            .appName("joinSomething")
            .getOrCreate();

        String pageClicksPath = "/user/root/page-clicks/stream-*/part-*";
        String peoplePath = "/user/root/peopleWithUUID.csv";
        String cookieEmailsPath = "/user/root/userCookieEmailMappings.csv";

        Encoder<PageClick> pageClickEncoder = Encoders.bean(PageClick.class);
        if (args.length >= 3) {
            pageClicksPath = args[0];
            peoplePath = args[1];
            cookieEmailsPath = args[2];
        }
        Dataset<PageClick> pageClickDataset = sparkSession.read()
            .json(pageClicksPath)
            .as(pageClickEncoder);
        //        pageClickDataset.show(5);

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

        Dataset<Row> select = joinByEmail.select("gender", "birthDate").cache();
        RelationalGroupedDataset gender = joinByEmail.groupBy("gender");
        Dataset<Row> genderCount = gender.count();
        genderCount.show(5);
    }
}
