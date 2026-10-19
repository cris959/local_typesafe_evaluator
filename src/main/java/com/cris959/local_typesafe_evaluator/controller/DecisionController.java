package com.cris959.local_typesafe_evaluator.controller;

import com.cris959.local_typesafe_evaluator.model.DecisionResult;
import com.cris959.local_typesafe_evaluator.service.DecisionService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @GetMapping("/decidir")
    public DecisionResult clasificarMensaje(
            @RequestParam
            @NotBlank(message = "El parámetro 'mensaje' no puede estar vacío ni contener solo espacios.")
            String mensaje) {
        return decisionService.clasificarMensaje(mensaje);
    }
}