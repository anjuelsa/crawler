package webcrawler.crawler;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import webcrawler.crawler.crawler.WebCrawler;

@SpringBootApplication
public class CrawlerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length == 0) {
            System.err.println("Please provide a starting URL");
            System.exit(1);
        }

        String startUrl = args[0];
        WebCrawler crawler = new WebCrawler();
        crawler.crawl(startUrl);
}
}
