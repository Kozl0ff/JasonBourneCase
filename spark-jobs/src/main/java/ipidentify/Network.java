package ipidentify;

import java.io.Serializable;

public class Network implements Serializable, Comparable<Network> {
    private Long prefix;
    private Integer geoNameId;

    public Network(Long prefix, Integer geoNameId) {
        this.prefix = prefix;
        this.geoNameId = geoNameId;
    }

    public Network(Long prefix) {
        this.prefix = prefix;
        this.geoNameId = null;
    }

    public Long getPrefix() {
        return prefix;
    }

    public void setPrefix(Long prefix) {
        this.prefix = prefix;
    }

    public Integer getGeoNameId() {
        return geoNameId;
    }

    public void setGeoNameId(Integer geoNameId) {
        this.geoNameId = geoNameId;
    }

    @Override
    public int compareTo(Network o) {
        return this.prefix.compareTo(o.getPrefix());
    }
}
