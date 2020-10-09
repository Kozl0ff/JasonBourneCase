package ipidentify;

import com.opencsv.exceptions.CsvValidationException;
import models.PageClick;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import utils.SparkJobsUtils;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class SparkJob {
    private static final String GET_IP_INFO_UDF = "getIpInfo";

    public static void main(String[] args) throws IOException, CsvValidationException {
        String databaseUrl = "jdbc:mysql://172.18.64.114:3306";
        String pageClicksHdfsPath = "/user/root/page-clicks/stream-*/part-*";
        String geoNameToLocationsHdfsPath = "/user/root/GeoLite2-City-Locations-en.csv";
        String ipBlocksPath = "/spark_volume/resources/GeoLite2-City-Blocks-IPv4.csv";
        if (args.length >= 1) {
            databaseUrl = args[0];
        }
        if (args.length >= 2) {
            pageClicksHdfsPath = args[1];
        }
        if (args.length >= 4) {
            geoNameToLocationsHdfsPath = args[2];
            ipBlocksPath = args[3];
        }

        SparkConf sparkConf = new SparkConf().setAppName("IpIdentify");
        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);
        SQLContext sqlContext = new SQLContext(javaSparkContext);
        SparkSession sparkSession = sqlContext.sparkSession();

        final Broadcast<List<List<Network>>> ipBlocksBroadcast =
            javaSparkContext.broadcast(IpIdentifier.readIpBlocks(ipBlocksPath));

        Encoder<PageClick> pageClickEncoder = Encoders.bean(PageClick.class);
        Dataset<PageClick> pageClickDataset = sparkSession.read()
            .json(pageClicksHdfsPath)
            .as(pageClickEncoder)
            .cache();

        sparkSession.udf().register(GET_IP_INFO_UDF, (UDF1<String, Integer>) ip ->
            IpIdentifier.getIpInfo(ip, ipBlocksBroadcast), DataTypes.IntegerType);

        Dataset<Row> pageClicksWithGeoNameId = pageClickDataset.withColumn("geoname_id",
            functions.callUDF(GET_IP_INFO_UDF, pageClickDataset.col("ip")));

        Dataset<Row> geoNameToLocations = sparkSession.read()
            .option("header", "true")
            .csv(geoNameToLocationsHdfsPath)
            .select("geoname_id", "continent_name", "city_name", "country_iso_code");

        Dataset<Row> pageClickWithIpInfoDataset = pageClicksWithGeoNameId.join(
            geoNameToLocations,
            pageClicksWithGeoNameId.col("geoname_id")
                .equalTo(geoNameToLocations.col("geoname_id")),
            "left");

        Dataset<Row> ipInfoCached = pageClickWithIpInfoDataset
            .select("continent_name", "city_name", "country_iso_code").cache();
        Dataset<Row> continentCount = ipInfoCached
            .groupBy("continent_name")
            .count()
            .sort(functions.col("count").desc())
            .withColumn("last_modified_time", functions.current_timestamp());
        Dataset<Row> countryCount = ipInfoCached
            .groupBy("country_iso_code")
            .count()
            .sort(functions.col("count").desc())
            .withColumn("last_modified_time", functions.current_timestamp());
        Dataset<Row> cityCount = ipInfoCached
            .groupBy("city_name")
            .count()
            .sort(functions.col("count").desc())
            .withColumn("last_modified_time", functions.current_timestamp());

        System.out.println("pageClicksWithGeoNameId.count() = " + pageClicksWithGeoNameId.count());
        System.out.println("pageClickWithIpInfoDataset.count() = " + pageClickWithIpInfoDataset.count());
        continentCount.show(10);
        countryCount.show(10);
        cityCount.show(10);

        Properties connectionProperties = new Properties();
        connectionProperties.put("driver", "com.mysql.jdbc.Driver");
        connectionProperties.put("user", "user");
        connectionProperties.put("password", "user");

        SparkJobsUtils.writeToMySql(continentCount, databaseUrl, "bdata.continent", connectionProperties);
        SparkJobsUtils.writeToMySql(countryCount, databaseUrl, "bdata.country", connectionProperties);
        SparkJobsUtils.writeToMySql(cityCount, databaseUrl, "bdata.city", connectionProperties);
    }
}
