package kafka.pageclickgenerator;

import kafka.utils.GeneratorHelpers;
import models.PageClick;

import java.io.File;
import java.io.FileNotFoundException;
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
        pageClick.setTimestamp(GeneratorHelpers.generateTimestamp());
        return pageClick;
    }

    private String generateIP() {
        return random.nextInt(256) + "." +
            random.nextInt(256) + "." +
            random.nextInt(256) + "." +
            random.nextInt(256);
    }
}
