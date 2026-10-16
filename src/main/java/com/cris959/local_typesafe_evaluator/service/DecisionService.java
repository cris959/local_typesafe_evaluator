package com.cris959.local_typesafe_evaluator.service;

import com.cris959.local_typesafe_evaluator.model.DecisionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;



@Service
public class DecisionService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public DecisionService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        // Spring AI configura automáticamente la conexión con Ollama sin rompernos la cabeza
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public DecisionResult clasificarMensaje(String mensaje) {
        String promptSistema = """
                Eres un clasificador de atención al cliente. Debes responder **únicamente** con un objeto JSON válido, sin texto adicional ni bloques de markdown.
                Estructura obligatoria del JSON:
                {
                  "categoria": "SOPORTE_TECNICO",
                  "confianza": 0.95,
                  "razon": "breve explicación"
                }
                Las categorías permitidas son: SOPORTE_TECNICO, FACTURACION, CONSULTA_GENERAL, URGENTE, VENTAS, RECLAMO, CANCELACION, RECURSOS_HUMANOS.
                """;

        try {
            String rawResponse = chatClient.prompt()
                    .system(promptSistema)
                    .user(mensaje)
                    .call()
                    .content();

            // Validación de seguridad para evitar NullPointerException
            if (rawResponse == null || rawResponse.isBlank()) {
                throw new RuntimeException("La respuesta del modelo local está vacía.");
            }

            // Limpiamos marcas de código markdown de forma segura
            String jsonLimpio = rawResponse.replace("```json", "").replace("```", "").trim();

            // Deserializamos directamente al record con Jackson
            return objectMapper.readValue(jsonLimpio, DecisionResult.class);

        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la clasificación con la IA: " + e.getMessage(), e);
        }
    }
}