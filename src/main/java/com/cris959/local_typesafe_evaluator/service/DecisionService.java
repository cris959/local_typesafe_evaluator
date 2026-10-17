package com.cris959.local_typesafe_evaluator.service;

import com.cris959.local_typesafe_evaluator.model.DecisionResult;
import com.cris959.local_typesafe_evaluator.router.ModelRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;


@Service
public class DecisionService {

    private final ModelRouter modelRouter;
    private final ObjectMapper objectMapper;

    public DecisionService(ModelRouter modelRouter, ObjectMapper objectMapper) {
        this.modelRouter = modelRouter;
        this.objectMapper = objectMapper;
    }


    public DecisionResult clasificarMensaje(String mensaje, String modo) {
        // Obtenemos el ChatModel correspondiente (Ollama o Groq) mediante el Router
        ChatModel chatModel = modelRouter.route(modo);
        ChatClient chatClient = ChatClient.create(chatModel);

        String promptSistema = """
                Eres un evaluador de mensajes de atención al cliente.
                Debes responder **únicamente** con un objeto JSON válido, sin bloques de código markdown ni texto adicional.

                Estructura obligatoria del JSON:
                {
                  "categoria": "RECLAMO",
                  "departamento": "SOPORTE_TECNICO",
                  "nivelFrustracion": "FRUSTRADO",
                  "esUrgente": true,
                  "confianza": 0.95,
                  "razon": "El usuario reporta fallas continuas en la cobranza desde hace varios días."
                }

                Reglas para cada campo:
                - "categoria": Una de [VENTAS, RECLAMO, CANCELACION, FACTURACION, SOPORTE_TECNICO, URGENTE, CONSULTA_GENERAL, RECURSOS_HUMANOS].
                - "departamento": Uno de [FACTURACION_Y_COBROS, SOPORTE_TECNICO, VENTAS, ATENCION_AL_CLIENTE, RECURSOS_HUMANOS].
                - "nivelFrustracion": Uno de [CALMADO, FRUSTRADO, MUY_ENOJADO].
                - "esUrgente": true si requiere atención inmediata, false si es una consulta normal.
                - "confianza": número decimal entre 0.0 y 1.0.
                - "razon": breve explicación en español del motivo de la evaluación.
                """;

        try {
            String rawResponse = chatClient.prompt()
                    .system(promptSistema)
                    .user(mensaje)
                    .call()
                    .content();

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new RuntimeException("La respuesta del modelo de IA está vacía.");
            }

            String jsonLimpio = rawResponse.replace("```json", "").replace("```", "").trim();

            return objectMapper.readValue(jsonLimpio, DecisionResult.class);

        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la evaluación con la IA: " + e.getMessage(), e);
        }
    }

    // Sobrecarga por defecto para mantener compatibilidad usando el modo "local"
    public DecisionResult clasificarMensaje(String mensaje) {
        return clasificarMensaje(mensaje, "local");
    }
}