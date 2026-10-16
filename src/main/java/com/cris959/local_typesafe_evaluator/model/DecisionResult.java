package com.cris959.local_typesafe_evaluator.model;

public record DecisionResult(
        Categoria categoria,
        double confianza,
        String razon
) {
}