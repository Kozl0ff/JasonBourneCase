package counthosts;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import models.PageClick;
import org.apache.spark.sql.*;
import java.util.Properties;

public class SparkCountUrl {
    public static final String mySqlUrl = "jdbc:mysql://172.18.64.90:3306";

    public static void writeToMySql(Dataset<Row> dataset, String url, String table, Properties properties) {
        dataset.write().mode(SaveMode.Append).jdbc(url, table, properties);
    }

    public static void main(String[] args) {
        String pageClicksPath = "/user/root/page-clicks/stream-*/part-*";
        if (args.length >= 1) {
            pageClicksPath = args[0];
        }
        SparkConf sparkConf = new SparkConf().setAppName("UrlIdentify");
        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);
        SQLContext sqlContext = new SQLContext(javaSparkContext);
        SparkSession sparkSession = sqlContext.sparkSession();

        Properties connectionProperties = new Properties();
        connectionProperties.put("driver", "com.mysql.jdbc.Driver");
        connectionProperties.put("user", "user");
        connectionProperties.put("password", "user");

        Encoder<PageClick> pageClickEncoder = Encoders.bean(PageClick.class);
        Dataset<PageClick> pageClickDataset = sparkSession.read()
                .json(pageClicksPath)
                .as(pageClickEncoder);

        Dataset<Row> urlInfo = pageClickDataset
                .select(
                        functions.regexp_replace(
                                pageClickDataset.col("refererUrl"),
                                "^(http[s]?://)?(.*?)/.*", "$2"
                        ).as("referrer_host"),
                        pageClickDataset.col("timestamp").substr(0,10).as("dtime")
                );
        Dataset<Row> rowDataset = urlInfo.groupBy("referrer_host", "dtime").count();
        rowDataset.show();

        writeToMySql(rowDataset, mySqlUrl, "bdata.urlcount", connectionProperties);
    }
}
