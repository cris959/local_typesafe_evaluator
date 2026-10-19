package com.cris959.local_typesafe_evaluator.service;

import com.cris959.local_typesafe_evaluator.model.DecisionResult;
import com.cris959.local_typesafe_evaluator.model.RoutingDecision;
import com.cris959.local_typesafe_evaluator.router.ModelRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;


@Service
public class DecisionService {

    private static final Logger log = LoggerFactory.getLogger(DecisionService.class);

    private final ModelRouter modelRouter;
    private final ObjectMapper objectMapper;

    public DecisionService(ModelRouter modelRouter, ObjectMapper objectMapper) {
        this.modelRouter = modelRouter;
        this.objectMapper = objectMapper;
    }

    public DecisionResult clasificarMensaje(String mensaje) {
        // 1. Obtener la decisión de ruteo automática mediante el evaluador inteligente
        RoutingDecision decision = modelRouter.route(mensaje);
        log.info("Enrutado automático -> Modelo: {} | Tier: {}", decision.model(), decision.tier());

        // 2. Resolver el ChatModel correspondiente según el Tier elegido
        ChatModel chatModel = modelRouter.resolveModel(decision.tier());
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

            // Limpieza robusta de bloques markdown y espacios extra
            String jsonLimpio = rawResponse.replace("```json", "")
                    .replace("```", "")
                    .trim();

            // Extraer solo la parte que contiene el JSON si el modelo escribió texto alrededor
            int startIndex = jsonLimpio.indexOf("{");
            int endIndex = jsonLimpio.lastIndexOf("}");
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                jsonLimpio = jsonLimpio.substring(startIndex, endIndex + 1);
            }

            return objectMapper.readValue(jsonLimpio, DecisionResult.class);

        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la evaluación con la IA: " + e.getMessage(), e);
        }
    }
}