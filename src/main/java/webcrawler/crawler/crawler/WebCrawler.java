package webcrawler.crawler.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webcrawler.crawler.model.UrlDepthPair;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class WebCrawler {
    private static final Logger LOG = LoggerFactory.getLogger(WebCrawler.class);

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
                LOG.error("Start url is null");
                return;
            }
            String domain = startUri.getHost();
            queue.offer(new UrlDepthPair(startUri.toString(), 0));
            visitedUrls.add(startUri.toString());

            while (!queue.isEmpty()) {
                UrlDepthPair current = queue.poll();
                if (current.getDepth() > maxDepth) continue;

                LOG.info("Visited [{}]: {}", current.getDepth(), current.getUrl());
                if (!isHtmlLink(current.getUrl())) {
                    LOG.debug("Skipping non-HTML URL: {}", current.getUrl());
                    continue;
                }

                List<String> links = extractLinks(current.getUrl());

                if(!links.isEmpty()) {
                    LOG.info("Links found");
                }

                for (String link : links) {
                    URI linkUri = normalize(link);

                    if (linkUri == null) {
                        LOG.debug("Skipping null or invalid link: {}", link);
                        continue;
                    }

                    String host = linkUri.getHost();
                    if (host == null || !host.equals(domain)) {
                        LOG.debug("Skipping link from different domain or null host: {}", linkUri);
                        continue;
                    }

                    String normalizedUrl = linkUri.toString();
                    if (!visitedUrls.contains(normalizedUrl)) {
                        visitedUrls.add(normalizedUrl);
                        queue.offer(new UrlDepthPair(normalizedUrl, current.getDepth() + 1));
                        LOG.debug("Queueing: {}", normalizedUrl);
                    }
                }

            }

        } catch (Exception e) {
            LOG.error("Something went wrong when trying to crawl urls", e);

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
        } catch (org.jsoup.HttpStatusException e) {
            LOG.warn("HTTP error {} fetching URL: {}", e.getStatusCode(), url);
        } catch (IOException e) {
            LOG.warn("I/O error fetching URL: {} - {}", url, e.getMessage());

        } catch (Exception e) {
            LOG.error("Unexpected error while extracting links from URL: {}", url, e);
        }
        return links;
    }

    private static final List<String> BLOCKED_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".webp",
            ".pdf", ".zip", ".rar", ".exe", ".tar", ".gz", ".mp3", ".mp4", ".avi"
    );

    private boolean isHtmlLink(String url) {
        String lower = url.toLowerCase();
        return BLOCKED_EXTENSIONS.stream().noneMatch(lower::endsWith);
    }
}
