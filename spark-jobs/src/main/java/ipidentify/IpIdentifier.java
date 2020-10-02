package ipidentify;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.spark.broadcast.Broadcast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/*
 * This class uses databases that map IPv4 in CIDR (Classless Inter-Domain Routing) notation
 * to geoname_id that in theres turn mapped to tuple of continent, country and city
 * which this ip belongs to.
 *
 * To make fast use of these DBs, this class transforms CIDR to geoname_id database to
 * list of 33 elements in which each element contains a list of all routing prefixes
 * of length equals to index in the 33-elements list.
 * This prefixes transformed to one integer by summing all octets multiplied by
 * 2^24, 2^16, 2^8, 2^0 from left octet to right.
 * Also, each prefix contained together with corresponding geoname_id.
 * After making this list, each list in the 33-elements list is sorted by prefixes.
 *
 * To determine which CIDR input IP belongs to, this class uses following algorithm:
 * 1) Transform input IP to one integer using the same method as for CIDR prefixes.
 * 2) Iterate over all possible routing prefixes length from 32 to 0:
 *     a) Calculate bitwise AND between input IP as one integer and mask of 32 bits
 *        in which first N bits are ones and others are zeroes, where
 *        N = routing prefix length on current iteration.
 *     b) Use binary search to find routing prefix equals to integer received on previous step.
 *     c) If prefix was found, it means that current IP belongs to corresponding CIDR and
 *        its geoname_id can be returned.
 *     d) Otherwise, search for it in the next list.
 * 3) If no CIDR was found, it means that used databases don't have information about current IP.
 */

public class IpIdentifier {
    static final int IP_SIZE = 32;
    private static final int IP_OCTETS = IP_SIZE / 8;
    static final long[] DEGREES = {1L << 24L, 1L << 16L, 1L << 8L, 1L};
    private static final long[] MASKS = new long[IP_SIZE + 1];

    static {
        MASKS[0] = 0;
        long maxBit = 1L << 31L;
        for (int i = 1; i <= IP_SIZE; i++) {
            MASKS[i] = (MASKS[i - 1] >> 1) + maxBit;
        }
    }

    public static Integer getIpInfo(String ipString,
                                   Broadcast<List<List<Network>>> ipBlocksBroadcast) {
        return getIpInfo(ipString, ipBlocksBroadcast.value());
    }

    public static Integer getIpInfo(String ipString,
                                    List<List<Network>> ipBlocksBroadcast) {
        List<Integer> collect = Arrays.stream(ipString.split("\\."))
            .map(Integer::parseInt)
            .collect(Collectors.toList());
        long ip = getIPAsOneLong(collect);

        for (int i = IP_SIZE; i >= 0; i--) {
            long supposedPrefix = ip & MASKS[i];
            int index = Collections.binarySearch(ipBlocksBroadcast.get(i), new Network(supposedPrefix));
            if (index >= 0) {
                return ipBlocksBroadcast.get(i).get(index).getGeoNameId();
            }
        }
        return null;
    }

    public static List<List<Network>> readIpBlocks(String path) throws IOException, CsvValidationException {
        List<List<Network>> result = new ArrayList<>();
        for (int i = 0; i <= IP_SIZE; i++) {
            result.add(new ArrayList<>());
        }
        CSVReader reader = new CSVReader(Files.newBufferedReader(Paths.get(path)));
        reader.skip(1);
        String [] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            try {
                List<Integer> collect = Arrays.stream(nextLine[0].split("[./]"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
                result.get(collect.get(4))
                    .add(new Network(getIPAsOneLong(collect), Integer.parseInt(nextLine[1])));
            } catch (NumberFormatException ignored) { }
        }
        for (List<Network> networkList : result) {
            Collections.sort(networkList);
        }
        return result;
    }

    private static long getIPAsOneLong(List<Integer> octets) {
        long prefix = 0;
        for (int i = 0; i < IP_OCTETS; i++) {
            prefix += (long)octets.get(i) * DEGREES[i];
        }
        return prefix;
    }
}
