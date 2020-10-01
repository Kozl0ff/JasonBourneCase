package kafka.utils;

import java.sql.Timestamp;
import java.util.Random;

public class GeneratorHelpers {
    private static Random random = new Random();

    public static String generateTimestamp() {
        int year = 14 + random.nextInt(5);
        long start = Timestamp.valueOf("20" + year + "-01-01 00:00:00").getTime();
        long end = Timestamp.valueOf("20" + (year + 1) + "-01-01 00:00:00").getTime();
        long diff = end - start;
        return new Timestamp(start + (long)(Math.random() * diff)).toString();
    }
}
