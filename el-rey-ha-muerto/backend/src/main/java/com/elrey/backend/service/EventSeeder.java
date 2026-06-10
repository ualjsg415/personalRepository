package com.elrey.backend.service;

import com.elrey.backend.entity.Choice;
import com.elrey.backend.entity.GameEvent;
import com.elrey.backend.repository.ChoiceRepository;
import com.elrey.backend.repository.GameEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventSeeder implements ApplicationRunner {

    private static final int ROTATION_DAYS = 3;

    @Value("${app.ai-events-enabled:true}")
    private boolean aiEventsEnabled;

    private final GameEventRepository eventRepo;
    private final ChoiceRepository    choiceRepo;
    private final RssScraperService   scraper;
    private final GeminiService       gemini;

    private final ObjectMapper mapper = new ObjectMapper();

    private record EventWithChoices(GameEvent event, List<Choice> choices) {}

    @Override
    public void run(ApplicationArguments args) {
        generateIfNeeded();
    }

    // Ejecuta a las 03:00 cada 3 días
    @Scheduled(cron = "0 0 3 */3 * *")
    public void scheduledRotation() {
        log.info("=== Rotación programada de eventos ===");
        generateIfNeeded();
    }

    private boolean isBatchRecent() {
        return eventRepo.findTopBySourceOrderByScrapedAtDesc("ai_generated")
                .map(e -> e.getScrapedAt() != null &&
                          e.getScrapedAt().isAfter(LocalDateTime.now().minusDays(ROTATION_DAYS)))
                .orElse(false);
    }

    private void generateIfNeeded() {
        if (!aiEventsEnabled) {
            log.info("Generación AI desactivada (app.ai-events-enabled=false). Usando eventos manuales.");
            return;
        }
        if (isBatchRecent()) {
            log.info("Lote de eventos AI reciente (< {} días), seeder omitido.", ROTATION_DAYS);
            return;
        }

        log.info("=== EventSeeder: iniciando scraping + generación IA ===");

        // ── Fase 1: Scraping RSS ──────────────────────────────────────────────
        List<RssScraperService.NewsItem> news = scraper.scrapeHeadlines(15);
        if (news.size() < 10) {
            log.warn("Solo {} noticias obtenidas (necesito 10). Se mantienen los eventos manuales.", news.size());
            return;
        }

        // ── Fase 2: Generar eventos con Gemini ───────────────────────────────
        List<EventWithChoices> generated = new ArrayList<>();
        for (int day = 1; day <= 10; day++) {
            RssScraperService.NewsItem item = news.get(day - 1);
            try {
                log.info("Día {}/10: generando desde '{}'", day, item.headline());
                String json = gemini.generateMedievalEvent(item.headline(), item.summary());
                EventWithChoices ec = parseEventWithChoices(json, day);
                if (ec != null) {
                    generated.add(ec);
                    log.info("Día {}: '{}' OK", day, ec.event().getTitle());
                } else {
                    log.warn("Día {}: respuesta Gemini inválida, se omite.", day);
                }
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "";
                if (msg.contains("QUOTA_EXCEEDED")) {
                    log.warn("Cuota diaria de Gemini agotada. Se usarán eventos manuales hasta mañana.");
                    break; // Abortar — no tiene sentido intentar los días restantes
                }
                log.error("Día {}: error en Gemini: {}", day, e.getMessage());
            }
            // RPM gratuito = 5 → mínimo 12s entre llamadas; usamos 15s con margen
            if (day < 10) {
                log.info("Esperando 15s para respetar límite de frecuencia (5 RPM)...");
                try { Thread.sleep(15_000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }

        if (generated.size() < 10) {
            log.warn("Solo {}/10 eventos generados. Se mantienen los eventos manuales.", generated.size());
            return;
        }

        // ── Fase 3: Guardar eventos AI (los manuales se conservan para el historial) ──
        for (EventWithChoices ec : generated) {
            GameEvent saved = eventRepo.save(ec.event());
            for (Choice c : ec.choices()) {
                c.setEvent(saved);
                choiceRepo.save(c);
            }
        }
        log.info("=== EventSeeder: {} eventos medievales generados y guardados ===", generated.size());
    }

    // ── Parser del JSON que devuelve Gemini ───────────────────────────────────

    private EventWithChoices parseEventWithChoices(String raw, int day) {
        try {
            // Limpiar posibles bloques markdown ```json ... ```
            String json = raw.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("(?s)^```[a-z]*\\s*", "").replaceAll("```\\s*$", "").trim();
            }

            JsonNode root = mapper.readTree(json);

            String title = root.path("title").asText("Evento del día " + day);
            String desc  = root.path("description").asText("Un nuevo desafío aguarda.");
            String scene = root.path("scene").asText("throne-room");

            if (!List.of("bedroom","throne-room","dining-hall","gardens","dungeon").contains(scene)) {
                scene = "throne-room";
            }

            GameEvent event = GameEvent.builder()
                .source("ai_generated")
                .dayTarget(day)
                .title(title.length() > 200 ? title.substring(0, 200) : title)
                .description(desc)
                .scene(scene)
                .scrapedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

            List<Choice> choices = new ArrayList<>();
            for (JsonNode c : root.path("choices")) {
                String deathMsg = c.path("deathMessage").isNull() || c.path("deathMessage").isMissingNode()
                    ? null : c.path("deathMessage").asText(null);

                choices.add(Choice.builder()
                    .label(c.path("label").asText("A"))
                    .text(c.path("text").asText("..."))
                    .statHygiene(clamp(c.path("statHygiene").asInt(0)))
                    .statHunger(clamp(c.path("statHunger").asInt(0)))
                    .statPopularity(clamp(c.path("statPopularity").asInt(0)))
                    .statWealth(clamp(c.path("statWealth").asInt(0)))
                    .immediateDeath(c.path("immediateDeath").asBoolean(false))
                    .deathMessage(deathMsg)
                    .hiddenFlag(null)
                    .flagTriggerDelay(3)
                    .build());
            }

            if (choices.size() != 3) {
                log.warn("Se esperaban 3 opciones, Gemini devolvió {}", choices.size());
                return null;
            }

            return new EventWithChoices(event, choices);

        } catch (Exception e) {
            log.error("Error parseando JSON de Gemini: {}", e.getMessage());
            log.debug("JSON recibido: {}", raw);
            return null;
        }
    }

    private int clamp(int v) { return Math.max(-25, Math.min(25, v)); }
}
