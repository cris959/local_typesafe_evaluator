package com.cris959.local_typesafe_evaluator.router;

import com.cris959.local_typesafe_evaluator.model.ModelTier;
import com.cris959.local_typesafe_evaluator.model.RoutingDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ModelRouter {

    private static final Logger log = LoggerFactory.getLogger(ModelRouter.class);

    private final ChatModel ollamaChatModel;
    private final ChatModel mistralAiChatModel; // Maneja peticiones hacia Mistral AI
    private final ChatModel openAiChatModel; // Inyecta Groq (Starter OpenAI)
    private final ChatClient evaluatorClient;


    public ModelRouter(
            @Qualifier("ollamaChatModel") ChatModel ollamaChatModel,
            @Qualifier("mistralAiChatModel") ChatModel mistralAiChatModel,
            @Qualifier("openAiChatModel") ChatModel openAiChatModel,
            @Qualifier("ollamaChatModel") ChatModel evaluatorModel) { // <-- Calificamos explícitamente el evaluador
        this.ollamaChatModel = ollamaChatModel;
        this.mistralAiChatModel = mistralAiChatModel;
        this.openAiChatModel = openAiChatModel;
        // Creamos el ChatClient usando específicamente Ollama como evaluador local rápido
        this.evaluatorClient = ChatClient.create(evaluatorModel);
    }

    public RoutingDecision route(String prompt) {
        String systemPrompt = """
            Analiza el mensaje del usuario y selecciona el nivel del modelo más adecuado.
            Opciones disponibles:
            1. LOCAL (%s): %s
            2. MISTRAL (%s): %s
            3. GROQ (%s): %s

            Responde únicamente con el nombre exacto del tier seleccionado (LOCAL, MISTRAL o GROQ), sin texto adicional.
            """.formatted(
                ModelTier.LOCAL.getModelId(), ModelTier.LOCAL.getDescription(),
                ModelTier.MISTRAL.getModelId(), ModelTier.MISTRAL.getDescription(),
                ModelTier.OPENAI.getModelId(), ModelTier.OPENAI.getDescription()
        );

        try {
            String seleccion = evaluatorClient.prompt()
                    .system(systemPrompt)
                    .user(prompt)
                    .call()
                    .content()
                    .trim()
                    .toUpperCase();

            // Limpieza extra por si el modelo devuelve texto de más
            if (seleccion.contains("MISTRAL")) seleccion = "MISTRAL";
            else if (seleccion.contains("GROQ") || seleccion.contains("OPENAI")) seleccion = "OPENAI";
            else seleccion = "LOCAL";

            ModelTier tier = ModelTier.valueOf(seleccion);
            log.info("Enrutamiento inteligente con Groq -> Seleccionado: {}", tier);

            return new RoutingDecision(tier, tier.getModelId(), 0.95, Map.of());
        } catch (Exception e) {
            log.warn("Fallo en la selección automática de tier con Groq. Usando LOCAL por defecto. Motivo: {}", e.getMessage());
            return new RoutingDecision(ModelTier.LOCAL, ModelTier.LOCAL.getModelId(), 0.80, Map.of());
        }
    }

    public ChatModel resolveModel(ModelTier tier) {
        return switch (tier) {
            case MISTRAL -> mistralAiChatModel;
            case OPENAI ->  openAiChatModel;
            default -> ollamaChatModel;
        };
    }
}