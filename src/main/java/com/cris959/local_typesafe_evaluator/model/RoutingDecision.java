package com.cris959.local_typesafe_evaluator.model;

import java.util.Map;

public record RoutingDecision(
        ModelTier tier,
        String model,
        double confidence,
        Map<String, Double> probabilities
) {}