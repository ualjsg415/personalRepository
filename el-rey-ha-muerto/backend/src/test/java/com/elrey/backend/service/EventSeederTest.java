package com.elrey.backend.service;

import com.elrey.backend.entity.GameEvent;
import com.elrey.backend.repository.ChoiceRepository;
import com.elrey.backend.repository.GameEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventSeederTest {

    @Mock GameEventRepository eventRepo;
    @Mock ChoiceRepository choiceRepo;
    @Mock RssScraperService scraper;
    @Mock GeminiService gemini;

    @InjectMocks EventSeeder eventSeeder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventSeeder, "aiEventsEnabled", true);
    }

    @Test
    void no_genera_si_ai_events_desactivado() throws Exception {
        ReflectionTestUtils.setField(eventSeeder, "aiEventsEnabled", false);

        eventSeeder.run(null);

        verify(scraper, never()).scrapeHeadlines(anyInt());
        verify(gemini, never()).generateMedievalEvent(any(), any());
    }

    @Test
    void no_genera_si_lote_reciente() throws Exception {
        GameEvent recent = GameEvent.builder()
                .id(1L).scrapedAt(LocalDateTime.now().minusHours(2)).build();

        when(eventRepo.findTopBySourceOrderByScrapedAtDesc("ai_generated"))
                .thenReturn(Optional.of(recent));

        eventSeeder.run(null);

        verify(scraper, never()).scrapeHeadlines(anyInt());
        verify(gemini, never()).generateMedievalEvent(any(), any());
    }

    @Test
    void no_genera_si_no_hay_suficientes_noticias() throws Exception {
        when(eventRepo.findTopBySourceOrderByScrapedAtDesc("ai_generated"))
                .thenReturn(Optional.empty());
        when(scraper.scrapeHeadlines(15)).thenReturn(List.of(
                new RssScraperService.NewsItem("Noticia 1", "Desc 1"),
                new RssScraperService.NewsItem("Noticia 2", "Desc 2")
        ));

        eventSeeder.run(null);

        verify(gemini, never()).generateMedievalEvent(any(), any());
    }

    @Test
    void genera_si_no_hay_lote_previo() throws Exception {
        when(eventRepo.findTopBySourceOrderByScrapedAtDesc("ai_generated"))
                .thenReturn(Optional.empty());

        List<RssScraperService.NewsItem> news = List.of(
                new RssScraperService.NewsItem("Noticia 1", "Desc 1"),
                new RssScraperService.NewsItem("Noticia 2", "Desc 2"),
                new RssScraperService.NewsItem("Noticia 3", "Desc 3"),
                new RssScraperService.NewsItem("Noticia 4", "Desc 4"),
                new RssScraperService.NewsItem("Noticia 5", "Desc 5"),
                new RssScraperService.NewsItem("Noticia 6", "Desc 6"),
                new RssScraperService.NewsItem("Noticia 7", "Desc 7"),
                new RssScraperService.NewsItem("Noticia 8", "Desc 8"),
                new RssScraperService.NewsItem("Noticia 9", "Desc 9"),
                new RssScraperService.NewsItem("Noticia 10", "Desc 10"),
                new RssScraperService.NewsItem("Noticia 11", "Desc 11"),
                new RssScraperService.NewsItem("Noticia 12", "Desc 12"),
                new RssScraperService.NewsItem("Noticia 13", "Desc 13"),
                new RssScraperService.NewsItem("Noticia 14", "Desc 14"),
                new RssScraperService.NewsItem("Noticia 15", "Desc 15")
        );
        when(scraper.scrapeHeadlines(15)).thenReturn(news);
        when(gemini.generateMedievalEvent(any(), any()))
                .thenThrow(new RuntimeException("QUOTA_EXCEEDED: cuota diaria de Gemini agotada"));

        eventSeeder.run(null);

        verify(scraper).scrapeHeadlines(15);
        verify(gemini, atLeastOnce()).generateMedievalEvent(any(), any());
    }

    @Test
    void para_generacion_inmediatamente_al_detectar_quota_exceeded() throws Exception {
        when(eventRepo.findTopBySourceOrderByScrapedAtDesc("ai_generated"))
                .thenReturn(Optional.empty());

        List<RssScraperService.NewsItem> news = List.of(
                new RssScraperService.NewsItem("N1", "D1"), new RssScraperService.NewsItem("N2", "D2"),
                new RssScraperService.NewsItem("N3", "D3"), new RssScraperService.NewsItem("N4", "D4"),
                new RssScraperService.NewsItem("N5", "D5"), new RssScraperService.NewsItem("N6", "D6"),
                new RssScraperService.NewsItem("N7", "D7"), new RssScraperService.NewsItem("N8", "D8"),
                new RssScraperService.NewsItem("N9", "D9"), new RssScraperService.NewsItem("N10", "D10"),
                new RssScraperService.NewsItem("N11", "D11"), new RssScraperService.NewsItem("N12", "D12"),
                new RssScraperService.NewsItem("N13", "D13"), new RssScraperService.NewsItem("N14", "D14"),
                new RssScraperService.NewsItem("N15", "D15")
        );
        when(scraper.scrapeHeadlines(15)).thenReturn(news);
        when(gemini.generateMedievalEvent(any(), any()))
                .thenThrow(new RuntimeException("QUOTA_EXCEEDED: cuota diaria de Gemini agotada"));

        eventSeeder.run(null);

        // Debe llamar solo 1 vez (la primera lanza QUOTA_EXCEEDED y el loop para)
        verify(gemini, times(1)).generateMedievalEvent(any(), any());
    }
}
