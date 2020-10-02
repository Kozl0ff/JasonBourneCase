package kafka.pageclickgenerator;

import com.opencsv.exceptions.CsvValidationException;
import kafka.utils.GeneratorHelpers;
import models.PageClick;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class PageClickGenerator {
    private List<String> urls = new ArrayList<>();
    private List<String> cookies;
    private List<String> refererUrls = new ArrayList<>();
    private Integer length = 0;
    private Random random = new Random();

    public PageClickGenerator(String urlsPath, String cookieEmailMappingsPath) throws IOException, CsvValidationException {
        // Read URls from the file so that we can randomly use them later
        File file = new File(urlsPath);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String[] split = scanner.nextLine().split(",");
            urls.add(split[0]);
            refererUrls.add(split[1]);
            length += 2;
        }

        // Read cookies from CookieEmailMappings file
        cookies = GeneratorHelpers.readColumnFromCsvFile(cookieEmailMappingsPath, 0);
    }

    public PageClick getNext() {
        int rand = random.nextInt(length);
        PageClick pageClick = new PageClick();
        pageClick.setCookie(getRandomCookieFromMappings());
        pageClick.setIp(generateIP());
        if (rand < length / 2) {
            pageClick.setUrl(urls.get(rand));
        } else {
            pageClick.setUrl(refererUrls.get(rand - length / 2));
        }
        pageClick.setRefererUrl(refererUrls.get(rand / 2));
        pageClick.setTimestamp(GeneratorHelpers.generateTimestamp());
        return pageClick;
    }

    private String getRandomCookieFromMappings() {
        return cookies.get(random.nextInt(cookies.size()));
    }

    private String generateIP() {
        return random.nextInt(256) + "." +
                random.nextInt(256) + "." +
                random.nextInt(256) + "." +
                random.nextInt(256);
    }
}