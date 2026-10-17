package com.cris959.local_typesafe_evaluator.router;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ModelRouter {

    private final ChatModel ollamaChatModel;
    private final ChatModel mistralAiChatModel; // Maneja peticiones hacia Mistral AI

    public ModelRouter(
            @Qualifier("ollamaChatModel") ChatModel ollamaChatModel,
            @Qualifier("mistralAiChatModel")ChatModel mistralAiChatModel) {
        this.ollamaChatModel = ollamaChatModel;
        this.mistralAiChatModel = mistralAiChatModel;
    }
    public ChatModel route(String tipoConsulta) {
        if (tipoConsulta == null) {
            return ollamaChatModel;
        }

        return switch (tipoConsulta.toLowerCase().trim()) {
            case "compleja", "mistral" -> mistralAiChatModel; // Deriva a Mistral AI
            default -> ollamaChatModel;                     // Deriva a Ollama local por defecto
        };
    }
}