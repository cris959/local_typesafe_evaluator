# Local Type-Safe Evaluator 🤖✨

Aplicación backend desarrollada en **Spring Boot** diseñada para clasificar de forma tipada (type-safe) mensajes de atención al cliente utilizando modelos de lenguaje locales ejecutados a través de **Ollama**.

## 🛠️ Tecnologías y Stack
* **Java 21**
* **Spring Boot** (REST API, Validation)
* **Spring AI**   
° Ollama: **qwen2.5:1.5b** (Entorno local)   
° Mistral AI: **open-mistral-7b** (Entorno en la nube)
* **Jackson** (Deserialización y mapeo JSON)
* **Bean Validation** (`jakarta.validation`)

---

## 🚀 Arquitectura y Proveedores de IA
El proyecto implementa un enrutador inteligente (**ModelRouter**) que conmuta dinámicamente entre el entorno local y la nube según el parámetro de la petición:

° Entorno Local (Ollama):

° Modelo: **qwen2.5:1.5b**

° Propósito: Procesamiento offline, menor latencia y consultas estándar o de respaldo.

° Entorno Cloud (Mistral AI):

° Modelo: **open-mistral-7b** (optimizado para mayor velocidad, estabilidad y tolerancia en cuotas de desarrollo).

° Propósito: Resolución de consultas estructuradas de mayor exigencia y análisis contextual (**modo=mistral** o **modo=compleja**).

___

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

# 2. Configuración de Mistral AI (Cloud)
spring.ai.mistralai.api-key=${MISTRAL_API_KEY}
spring.ai.mistralai.chat.options.model=open-mistral-7b
spring.ai.mistralai.chat.options.temperature=0.1
````

## 🕹️ Endpoints de Prueba
Puedes alternar el motor de IA directamente desde los parámetros de la petición HTTP

° Usando modelo local (Ollama):

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
° Usando modelo en la nube (Mistral AI):

````plaintext
http://localhost:8008/decidir?mensaje=Llevo+3+días+sin+poder+cobrar&modo=mistral
````

° Respuesta Esperada (**200 OK**):

````json
{
  "categoria": "FACTURACION",
  "departamento": "FACTURACION_Y_COBROS",
  "nivelFrustracion": "FRUSTRADO",
  "esUrgente": true,
  "confianza": 0.98,
  "razon": "El cliente reporta un problema persistente en el cobro de sus facturas durante tres días consecutivos, lo que indica un impacto directo en su operación financiera y genera alta urgencia."
}
````

## 🚀 Enrutamiento Dinámico e Inteligente de Modelos (Model Router)
El proyecto implementa un sistema de enrutamiento automático de LLMs capaz de evaluar la complejidad del mensaje entrante y derivar la consulta al modelo más adecuado de forma 100% transparente, optimizando tanto los tiempos de respuesta como el consumo de recursos.

### 🛠️ Arquitectura de Modelos (Tiers)
El enrutamiento se gestiona a través del enum **ModelTier** y soporta tres niveles de procesamiento:

1. **LOCAL** (**qwen2.5:1.5b** via Ollama): Destinado a consultas sencillas, saludos, interacciones rápidas o procesamiento local sin costo de red.

2. **MISTRAL** (**open-mistral-7b** via Mistral AI): Diseñado para análisis técnicos estructurados, explicaciones paso a paso, diseño de bases de datos y tareas conceptuales intermedias.

3. **OPENAI** (**openai/gpt-oss-20b** via Openai): Utilizado para reclamos urgentes, errores críticos de sistema, situaciones de alta frustración del usuario o cuando se requiere baja latencia con máxima capacidad de razonamiento.

### 💡 Puntos Clave de la Implementación
* Evaluador Automático: Utiliza la velocidad y precisión de Groq como motor evaluador inicial para analizar el prompt y seleccionar el **ModelTier** óptimo sin requerir parámetros manuales de modo en los endpoints REST.

* Garantía Type-Safe: Deserialización estructurada mediante Jackson mapeada directamente a los DTOs/Records del dominio (**DecisionResult**), asegurando respuestas JSON válidas.

* Inyección de Dependencias Calificada: Uso de **@Qualifier** para gestionar múltiples beans de **ChatModel** (**ollamaChatModel**, **mistralAiChatModel**, **openAiChatModel**) dentro del contenedor de Spring Boot.

### 📊 Diagrama

````mermaid
flowchart TD
    A[Cliente / HTTP GET /decidir] --> B[DecisionController]
    B --> C[DecisionService]
    C --> D[ModelRouter]

    subgraph Evaluador de Enrutamiento
        D --> E[Openai Evaluador - openai/gpt-oss-20b]
        E --> F{Determinar ModelTier}
    end

    F -- LOCAL --> G[Ollama: qwen2.5:1.5b]
    F -- MISTRAL --> H[Mistral AI: open-mistral-7b]
    F -- OPENAI --> I[Openai: openai/gpt-oss-20b]

    G --> J[DecisionResult - JSON Tipado]
    H --> J
    I --> J

    J --> K[Respuesta al Cliente]
````
### Ejemplos de Uso y Pruebas del API
Puedes probar el endpoint de enrutamiento inteligente utilizando los siguientes casos de prueba, que demuestran cómo el sistema evalúa la complejidad y la urgencia para derivar la consulta al modelo adecuado (**LOCAL**, **MISTRAL** o **OPENAI**), retornando un JSON tipado estructurado:

1. Saludo / Interacción Básica (**LOCAL**)
* Endpoint / URL de prueba:
````text
http://localhost:8008/decidir?mensaje=Hola%2C%20%C2%BFC%C3%B3mo%20est%C3%A1s%3F
````
* Respuesta esperada (**DecisionResult**):

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

2. Consulta Técnica Compleja (**MISTRAL**)
* Endpoint / URL de prueba:
````text
http://localhost:8008/decidir?mensaje=Expl%C3%ADcame%20paso%20a%20paso%20c%C3%B3mo%20estructurar%20una%20tabla%20de%20base%20de%20datos%20relacional%20para%20un%20sistema%20de%20comercio%20electr%C3%B3nico.
````
* Respuesta esperada (**DecisionResult**):
````json
{
  "categoria": "CONSULTA_GENERAL",
  "departamento": "SOPORTE_TECNICO",
  "nivelFrustracion": "CALMADO",
  "esUrgente": false,
  "confianza": 0.98,
  "razon": "El usuario solicita orientación técnica sobre diseño de bases de datos, no requiere atención inmediata ni urgencia."
}
````

3. Reclamo Crítico y Urgente (**OPENAI**)
* Endpoint / URL de prueba:
````text
http://localhost:8008/decidir?mensaje=Llevo%20tres%20d%C3%ADas%20sin%20poder%20cobrar%20mis%20facturas%20y%20el%20sistema%20falla%2C%20exijo%20una%20soluci%C3%B3n%20urgente.
````
* Respuesta esperada (**DecisionResult**):
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
