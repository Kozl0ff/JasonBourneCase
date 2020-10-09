package utils;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import java.util.Properties;

public class SparkJobsUtils {
    public static void writeToMySql(Dataset<Row> dataset, String url, String table, Properties properties){
        dataset.write()
            .mode(SaveMode.Append)
            .jdbc(url, table, properties);
    }
}
