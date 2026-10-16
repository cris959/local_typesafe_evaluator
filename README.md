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

## 📋 Evaluaciones y Atributos Tipados

Cada mensaje es procesado para extraer un análisis completo 100% en español:

- **Categoría Principal (`categoria`)**:
    - `VENTAS`, `RECLAMO`, `CANCELACION`, `FACTURACION`, `SOPORTE_TECNICO`, `URGENTE`, `CONSULTA_GENERAL`, `RECURSOS_HUMANOS`
- **Departamento Asignado (`departamento`)**:
    - `FACTURACION_Y_COBROS`, `SOPORTE_TECNICO`, `VENTAS`, `ATENCION_AL_CLIENTE`, `RECURSOS_HUMANOS`
- **Nivel de Frustración (`nivelFrustracion`)**:
    - `CALMADO`, `FRUSTRADO`, `MUY_ENOJADO`
- **Nivel de Urgencia (`esUrgente`)**:
    - `true` / `false`
- **Confianza (`confianza`)**:
    - Puntuación de precisión decimal (entre `0.0` y `1.0`)
- **Justificación (`razon`)**:
    - Explicación concisa generada por la IA sobre la decisión tomada

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
http://localhost:8008/decidir?mensaje=Llevo+3+días+sin+poder+cobrar+mis+ventas+y+nadie+me+responde
````

° Respuesta Esperada (**200 OK**):

````json
{
  "categoria": "RECLAMO",
  "departamento": "SOPORTE_TECNICO",
  "nivelFrustracion": "FRUSTRADO",
  "esUrgente": true,
  "confianza": 0.95,
  "razon": "El usuario reporta fallas continuas en la cobranza desde hace varios días."
}
````