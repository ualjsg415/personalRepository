package com.elrey.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build();

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    public String generateMedievalEvent(String headline, String summary) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("gemini.api-key no configurada");
        }
        return callWithRetry(buildPrompt(headline, summary), 2);
    }

    private String callWithRetry(String prompt, int attemptsLeft) throws Exception {
        String body = mapper.writeValueAsString(Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", prompt))
            )),
            "generationConfig", Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.9
            )
        ));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(String.format(ENDPOINT, model, apiKey)))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofSeconds(30))
            .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 429) {
            // Cuota diaria agotada — no tiene sentido reintentar hoy
            throw new RuntimeException("QUOTA_EXCEEDED: cuota diaria de Gemini agotada");
        }

        if (response.statusCode() == 503 && attemptsLeft > 0) {
            log.warn("503 servidor saturado, esperando 30s antes de reintentar ({} intentos restantes)...", attemptsLeft);
            Thread.sleep(30_000);
            return callWithRetry(prompt, attemptsLeft - 1);
        }

        if (response.statusCode() != 200) {
            log.error("Gemini API error {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Gemini devolvió " + response.statusCode());
        }

        JsonNode root = mapper.readTree(response.body());
        return root.path("candidates").get(0)
                   .path("content").path("parts").get(0)
                   .path("text").asText();
    }

    private String buildPrompt(String headline, String summary) {
        String safeSummary = summary.length() > 160 ? summary.substring(0, 160) : summary;
        return """
            Eres el narrador de "El Rey ha Muerto", un videojuego de rol medieval humorístico.
            El jugador es un rey que debe sobrevivir 10 días tomando decisiones.

            TAREA: Transforma esta noticia actual en un evento medieval del juego.

            REGLAS DE ADAPTACIÓN (ejemplos):
            - Futbolistas/deportistas → mercenarios, soldados, campeones del torneo
            - Virus/pandemia → plaga, fiebre negra, pestilencia
            - Políticos/gobierno → nobles, consejeros, el Consejo Real
            - Dinero/economía → monedas, arcas reales, tributos
            - Empresas/tecnología → gremios, alquimistas, pergaminos mágicos
            - Elecciones → sucesión, torneos, votación noble

            NOTICIA: "%s. %s"

            Responde SOLO con este JSON, sin markdown ni texto adicional:
            {
              "title": "Título del evento (máx 55 chars)",
              "description": "Descripción en segunda persona, tono medieval irónico (máx 240 chars)",
              "scene": "ELIGE_EL_MAS_APROPIADO",
              "choices": [
                {"label":"A","text":"Primera opción (máx 85 chars)","statHygiene":0,"statHunger":0,"statPopularity":0,"statWealth":0,"immediateDeath":false,"deathMessage":null},
                {"label":"B","text":"Segunda opción (máx 85 chars)","statHygiene":0,"statHunger":0,"statPopularity":0,"statWealth":0,"immediateDeath":false,"deathMessage":null},
                {"label":"C","text":"Tercera opción (máx 85 chars)","statHygiene":0,"statHunger":0,"statPopularity":0,"statWealth":0,"immediateDeath":false,"deathMessage":null}
              ]
            }

            SCENE: elige el valor más apropiado: bedroom, throne-room, dining-hall, gardens, dungeon
            STATS — sigue estas reglas de diseño:
            - Opciones malas:  stats entre -20 y -25 en la stat principal afectada. Pueden afectar 1-2 stats.
            - Opciones neutras: stats pequeños, entre -8 y +8.
            - Opciones buenas: stats entre +8 y +15. Nunca más de +15 en una sola stat.
            - Cada evento debe tener al menos UNA opción con una penalización de -20 o mayor.
            - Las sumas totales de stats por evento no deben ser muy positivas (el juego debe ser difícil).
            immediateDeath: solo true si la opción es claramente absurda/suicida; entonces incluye deathMessage gracioso.
            """.formatted(headline, safeSummary);
    }
}
