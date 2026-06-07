package com.elrey.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RssScraperService {

    private static final List<String> RSS_FEEDS = List.of(
        "https://feeds.elpais.com/mrss-s/pages/ep/site/elpais.com/portada",
        "https://e00-elmundo.uecdn.es/elmundo/rss/portada.xml",
        "https://rss.elconfidencial.com/espana/",
        "https://www.marca.com/rss/portada.xml",
        "https://www.20minutos.es/rss/"
    );

    public record NewsItem(String headline, String summary) {}

    public List<NewsItem> scrapeHeadlines(int count) {
        List<NewsItem> items = new ArrayList<>();

        for (String feedUrl : RSS_FEEDS) {
            if (items.size() >= count) break;
            try {
                Document doc = Jsoup.connect(feedUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(8000)
                    .parser(Parser.xmlParser())
                    .get();

                Elements entries = doc.select("item");
                for (Element entry : entries) {
                    if (items.size() >= count) break;

                    String title = entry.select("title").first() != null
                        ? entry.select("title").first().text() : "";
                    String desc  = entry.select("description").first() != null
                        ? entry.select("description").first().text() : "";

                    if (title.isBlank()) continue;

                    String cleanDesc = Jsoup.parse(desc).text();
                    if (cleanDesc.length() > 180) cleanDesc = cleanDesc.substring(0, 180);

                    items.add(new NewsItem(title, cleanDesc));
                }
                log.info("RSS {}: {} items totales hasta ahora", feedUrl, items.size());
            } catch (Exception e) {
                log.warn("No se pudo leer RSS {}: {}", feedUrl, e.getMessage());
            }
        }

        log.info("Scraping finalizado: {} noticias obtenidas", items.size());
        return items;
    }
}
