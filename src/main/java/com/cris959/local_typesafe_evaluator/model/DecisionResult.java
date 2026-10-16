package com.cris959.local_typesafe_evaluator.model;

public record DecisionResult(
        CategoriaMensaje categoria,
        Departamento departamento,
        NivelFrustracion nivelFrustracion,
        boolean esUrgente,        // true / false
        double confianza,        // ej: 0.95
        String razon             // breve explicación
) {
}