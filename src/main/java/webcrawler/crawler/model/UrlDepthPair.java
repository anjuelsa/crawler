package webcrawler.crawler.model;

public class UrlDepthPair {
    private String url;
    private int depth;
    public UrlDepthPair(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }
    public String getUrl() {return url;}
    public int getDepth() {return depth;}
}
