package kafka.pageclickgenerator;

public class PageClick {
    private String url;
    private String refererUrl;
    private String cookie;
    private String timestamp;
    private String ip;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRefererUrl() {
        return refererUrl;
    }

    public void setRefererUrl(String refererUrl) {
        this.refererUrl = refererUrl;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
