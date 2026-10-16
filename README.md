# Local Type-Safe Evaluator 🤖✨

Aplicación backend desarrollada en **Spring Boot** diseñada para clasificar de forma tipada (type-safe) mensajes de atención al cliente utilizando modelos de lenguaje locales ejecutados a través de **Ollama**.

## 🛠️ Tecnologías y Stack
* **Java 21**
* **Spring Boot** (REST API, Validation)
* **Spring AI / Ollama** (`qwen2.5:1.5b`)
* **Jackson** (Deserialización y mapeo JSON)
* **Bean Validation** (`jakarta.validation`)

---

## 🏗️ Arquitectura del Proyecto
El proyecto sigue los principios de **Arquitectura Limpia**, separando las responsabilidades en capas claras:
* **Controller (`DecisionController`)**: Expone los endpoints HTTP y aplica validaciones de entrada (`@NotBlank`).
* **Service (`DecisionService`)**: Orquesta la lógica de comunicación con el modelo de IA y procesa las respuestas.
* **Model (`DecisionResult` / Enum)**: Estructuras de datos tipadas (`record`) que aseguran un contrato robusto con las categorías de negocio.
* **Exception Handling (`GlobalExceptionHandler`)**: Manejo centralizado de errores HTTP y de validación con respuestas JSON limpias y estandarizadas.

---

## 📋 Categorías Soportadas
El clasificador evalúa los mensajes y los categoriza automáticamente en:
* `VENTAS`
* `RECLAMO`
* `CANCELACION`
* `FACTURACION`
* `SOPORTE_TECNICO`
* `URGENTE`
* `CONSULTA_GENERAL`
* `RECURSOS_HUMANOS`

---

## ⚙️ Configuración (`application.properties`)

````properties
spring.application.name=local-typesafe-evaluator
server.port=8008

# Configuración de Ollama (Local)
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.model=qwen2.5:1.5b
spring.ai.ollama.chat.options.temperature=0.1
````

## 🚀 Endpoints Principales
Clasificar Mensaje

° URL: **GET /decidir**

° Parámetro: **mensaje** (Requerido, no puede estar vacío)

° Ejemplo de uso:

````plaintext
http://localhost:8008/decidir?mensaje=Quiero+dar+de+baja+mi+cuenta+el+mes+que+viene
````

° Respuesta Esperada (**200 OK**):

````json
{
  "categoria": "CANCELACION",
  "confianza": 0.95,
  "razon": "El usuario solicita explícitamente dar de baja su cuenta."
}
````