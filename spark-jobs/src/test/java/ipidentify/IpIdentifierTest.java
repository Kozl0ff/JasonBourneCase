package ipidentify;

import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.net.util.SubnetUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static ipidentify.IpIdentifier.DEGREES;
import static ipidentify.IpIdentifier.IP_SIZE;

public class IpIdentifierTest {
    private static final int ITERATIONS = 100;
    private static final String IP_BLOCKS_PATH = "src/main/resources/GeoLite2-City-Blocks-IPv4.csv";
    private static final Random random = new Random();

    @Test
    void binarySearchVersusSubnetUtils() throws IOException, CsvValidationException {
        List<List<Network>> ipBlocks = IpIdentifier.readIpBlocks(IP_BLOCKS_PATH);

        long sumBinSearchTime = 0;
        long sumSubnetUtilsTime = 0;
        for (int k = 0; k < ITERATIONS; k++) {
            String ip = generateRandomIP();

            long currentTimeMillis = System.currentTimeMillis();
            Integer geoNameIdBinSearch = IpIdentifier.getIpInfo(ip, ipBlocks);
            long binSearchTime = System.currentTimeMillis() - currentTimeMillis;
            sumBinSearchTime += binSearchTime;

            currentTimeMillis = System.currentTimeMillis();
            Integer geoNameIdSubnetUtils = getIpInfoBySubnetUtils(ip, ipBlocks);
            long subnetUtilsTime = System.currentTimeMillis() - currentTimeMillis;
            sumSubnetUtilsTime += subnetUtilsTime;

            String assertMessage = "ip: " + ip + ", iteration: " + k;
            Assertions.assertEquals(geoNameIdSubnetUtils, geoNameIdBinSearch, assertMessage);

            System.out.println("Iteration " + k + " is correct.");
            System.out.println("Binary search time: " + binSearchTime + "ms");
            System.out.println("Subnet utils time: " + subnetUtilsTime + "ms");
            System.out.println();
        }

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("Total binary search time: " + sumBinSearchTime + "ms");
        System.out.println("Average binary search time: " + sumBinSearchTime / ITERATIONS + "ms");
        System.out.println();
        System.out.println("Total subnet utils time: " + sumSubnetUtilsTime + "ms");
        System.out.println("Average subnet utils time: " + sumSubnetUtilsTime / ITERATIONS + "ms");
    }

    private Integer getIpInfoBySubnetUtils(String ipString, List<List<Network>> ipBlocks) {
        for (int i = IP_SIZE; i >= 0; i--) {
            for (Network network : ipBlocks.get(i)) {
                long currentPrefix = network.getPrefix();
                String[] octets = new String[4];
                for (int j = 3; j >= 0; j--) {
                    octets[j] = String.valueOf(currentPrefix % DEGREES[2]);
                    currentPrefix >>= 8;
                }
                String CIDR = String.join(".", octets) + "/" + i;
                SubnetUtils subnetUtils = new SubnetUtils(CIDR);
                if (subnetUtils.getInfo().isInRange(ipString)) {
                    return network.getGeoNameId();
                }
            }
        }

        return null;
    }

    private String generateRandomIP() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            stringBuilder.append(random.nextInt(256));
            if (i < 3) {
                stringBuilder.append(".");
            }
        }
        return stringBuilder.toString();
    }
}
