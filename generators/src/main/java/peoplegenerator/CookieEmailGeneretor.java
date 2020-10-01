package peoplegenerator;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CookieEmailGeneretor {
    Random random = new Random();

    private String generateTimestamp() {
        int year = 14 + random.nextInt(5);
        long start = Timestamp.valueOf("20" + year + "-01-01 00:00:00").getTime();
        long end = Timestamp.valueOf("20" + (year + 1) + "-01-01 00:00:00").getTime();
        long diff = end - start;
        return new Timestamp(start + (long)(Math.random() * diff)).toString();
    }

    public void generateAndWriteDate(int n, String pathToUsers, String pathToCookieEmail) throws IOException, CsvValidationException {
        List<String> list = new LinkedList<String>();
        CSVReader reader = new CSVReader(new FileReader(pathToUsers));
        String [] nextLine;

        while ((nextLine = reader.readNext()) != null) {
            list.add(nextLine[4]);
        }

        try(FileWriter writer = new FileWriter(pathToCookieEmail, false)) {
            for (int i = 0; i <= n; i++) {
                String data = UUID.randomUUID().toString() + "," + list.get(random.nextInt(list.size())) + ","
                        + generateTimestamp();
                writer.write(data + "\n");
                writer.flush();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Example: 1000 generators/src/main/resources/people.csv generators/src/main/resources/userCookieEmailMappings.csv
     */
    public static void main(String args []) throws IOException, CsvValidationException {

        CookieEmailGeneretor cookieEmailGeneretor = new CookieEmailGeneretor();
        int numberOfRows = Integer.parseInt(args[0]);
        String pathToUsers = args[1];
        String pathToUserCookieMappings = args[2];
        cookieEmailGeneretor.generateAndWriteDate(numberOfRows, pathToUsers, pathToUserCookieMappings);
    }
}
