package webcrawler.crawler.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import webcrawler.crawler.model.UrlDepthPair;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class WebCrawler {

    // takes url -> traverse using bfs/dfs
    // max depth 2
    //store visited url to avoid traversing again

    private final Set<String> visitedUrls = new HashSet<>();
    private final int maxDepth = 2;

    public void crawl(String startUrl) {
        Queue<UrlDepthPair> queue = new LinkedList<>();

        try {
            URI startUri = normalize(startUrl);
            if (startUri == null) {
                System.err.println("Start url is null");
                return;
            }
            String domain = startUri.getHost();
            queue.offer(new UrlDepthPair(startUri.toString(), 0));
            visitedUrls.add(startUri.toString());

            while (!queue.isEmpty()) {
                UrlDepthPair current = queue.poll();
                if (current.getDepth() > maxDepth) continue;

                System.out.println("\nVisited: " + current.getUrl());
                List<String> links = extractLinks(current.getUrl());

                if(!links.isEmpty()) {
                    System.out.println("Links found");
                }

                for (String link : links) {
                    URI linkUri = normalize(link);
                    if(linkUri == null | !linkUri.getHost().equals(domain)) continue;

                    String normalizedUrl = link.toString();
                    if(!visitedUrls.contains(normalizedUrl)) {
                        visitedUrls.add(normalizedUrl);
                        queue.offer(new UrlDepthPair(normalizedUrl, current.getDepth() + 1));
                        System.out.println(" - " + normalizedUrl);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Start url is error");

        }
    }

    private URI normalize(String url) {
        try {
            URI uri = new URI(url).normalize();
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equals("http") || scheme.equals("https"))) {
                return null;
            }
            return uri;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private List<String> extractLinks(String url) {
        List<String> links = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();
            for(Element link : doc.select("a[href]")) {
                String href = link.attr("abs:href");
                if(href != null && !href.isEmpty()){
                    links.add(href);
                }
            }
        } catch (IOException e) {
            System.err.println("Extract links error: " + url);
        }
        return links;
    }
}
