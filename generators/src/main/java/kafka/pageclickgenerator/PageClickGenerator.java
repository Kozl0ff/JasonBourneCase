package kafka.pageclickgenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.*;

public class PageClickGenerator {
    private List<String> urls;
    private List<String> refererUrls;
    private Integer length = 0;
    private Random random;

    public PageClickGenerator(String urlsPath) throws FileNotFoundException {
        urls = new ArrayList<>();
        refererUrls = new ArrayList<>();
        random = new Random();

        File file = new File(urlsPath);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String[] split = scanner.nextLine().split(",");
            urls.add(split[0]);
            refererUrls.add(split[1]);
            length += 2;
        }
    }

    public PageClick getNext() {
        int rand = random.nextInt(length);
        PageClick pageClick = new PageClick();
        pageClick.setCookie(UUID.randomUUID().toString());
        pageClick.setIp(generateIP());
        if (rand < length / 2) {
            pageClick.setUrl(urls.get(rand));
        } else {
            pageClick.setUrl(refererUrls.get(rand - length / 2));
        }
        pageClick.setRefererUrl(refererUrls.get(rand / 2));
        pageClick.setTimestamp(generateTimestamp());
        return pageClick;
    }

    private String generateIP() {
        return random.nextInt(256) + "." +
            random.nextInt(256) + "." +
            random.nextInt(256) + "." +
            random.nextInt(256);
    }

    private String generateTimestamp() {
        int year = 14 + random.nextInt(5);
        long offset = Timestamp.valueOf("20" + year + "-01-01 00:00:00").getTime();
        long end = Timestamp.valueOf("20" + (year + 1) + "-01-01 00:00:00").getTime();
        long diff = end - offset + 1;
        return new Timestamp(offset + (long)(Math.random() * diff)).toString();
    }
}
