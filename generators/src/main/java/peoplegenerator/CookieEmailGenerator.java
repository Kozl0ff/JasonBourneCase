package peoplegenerator;

import com.opencsv.exceptions.CsvValidationException;
import kafka.utils.GeneratorHelpers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CookieEmailGenerator {
    private Random random = new Random();

    public void generateAndWriteDate(int n, String pathToUsers, String pathToCookieEmail) throws IOException, CsvValidationException {
        List<String> emails = GeneratorHelpers.readColumnFromCsvFile(pathToUsers, 4);

        try (FileWriter writer = new FileWriter(pathToCookieEmail, false)) {
            writer.write("cookie,email,timestamp" + System.lineSeparator());
            writer.flush();
            for (int i = 0; i < n; i++) {
                String data = UUID.randomUUID().toString() + "," + emails.get(random.nextInt(emails.size())) + ","
                        + GeneratorHelpers.generateTimestamp();
                writer.write(data + System.lineSeparator());
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Example: 1000 generators/src/main/resources/people.csv generators/src/main/resources/userCookieEmailMappings.csv
     */
    public static void main(String args[]) throws IOException, CsvValidationException {

        CookieEmailGenerator cookieEmailGenerator = new CookieEmailGenerator();
        int numberOfRows = Integer.parseInt(args[0]);
        String pathToUsers = args[1];
        String pathToUserCookieMappings = args[2];
        cookieEmailGenerator.generateAndWriteDate(numberOfRows, pathToUsers, pathToUserCookieMappings);
    }
}
