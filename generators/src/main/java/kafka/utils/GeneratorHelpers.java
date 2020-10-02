package kafka.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneratorHelpers {
    private static Random random = new Random();

    public static String generateTimestamp() {
        int year = 14 + random.nextInt(5);
        long start = Timestamp.valueOf("20" + year + "-01-01 00:00:00").getTime();
        long end = Timestamp.valueOf("20" + (year + 1) + "-01-01 00:00:00").getTime();
        long diff = end - start;
        return new Timestamp(start + (long) (Math.random() * diff)).toString();
    }

    public static List<String> readColumnFromCsvFile(String pathToUsers, int column) throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new FileReader(pathToUsers));
        String[] nextLine;

        List<String> result = new ArrayList<>();

        while ((nextLine = reader.readNext()) != null) {
            result.add(nextLine[column]);
        }

        return result;
    }
}