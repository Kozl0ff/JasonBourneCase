package ipidentify;

import java.io.Serializable;

public class IpInfo implements Serializable {
    private final static String UNDEFINED = "Undefined";

    private String ip;
    private String continent;
    private String country;
    private String city;

    public IpInfo(String continent, String country, String city) {
        this.continent = setField(continent);
        this.country = setField(country);
        this.city = setField(city);
    }

    public IpInfo(String ip) {
        this.ip = ip;
        continent = UNDEFINED;
        country = UNDEFINED;
        city = UNDEFINED;
    }

    private static String setField(String inputField) {
        if (inputField == null || inputField.length() == 0) {
            return UNDEFINED;
        } else {
            return inputField;
        }
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
