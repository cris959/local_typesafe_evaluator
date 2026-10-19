package com.cris959.local_typesafe_evaluator.model;

public enum ModelTier {

    LOCAL("qwen2.5:1.5b",
            "ÚNICAMENTE para saludos cortos, preguntas triviales o de cortesía sin ninguna complejidad."),

    MISTRAL("open-mistral-7b",
            "Exclusivamente para explicaciones técnicas paso a paso, arquitectura, diseño de bases de datos o análisis conceptuales."),

    OPENAI("openai/gpt-oss-20b",
            "EXCLUSIVO para reclamos urgentes de clientes, errores críticos del sistema, frustración alta o cuando se exija velocidad y resolución inmediata.");

    private final String modelId;
    private final String description;

    ModelTier(String modelId, String description) {
        this.modelId = modelId;
        this.description = description;
    }

    public String getModelId() {
        return modelId;
    }

    public String getDescription() {
        return description;
    }
}