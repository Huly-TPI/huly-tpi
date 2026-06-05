# Explicación backend IA - Controladores principales

## 1. Resumen ejecutivo

* `BotConfigController`: administra la configuración del bot para backoffice: prompt base y flag de detección de riesgo. No llama a IA; configura el texto que después usa el chat.
* `ChatController`: es la puerta HTTP del chatbot. Recibe mensajes, obtiene el usuario autenticado, delega el procesamiento a casos de uso y devuelve respuesta normal, stream SSE o historial.
* `EmotionalController`: no existe una clase con ese nombre exacto. En el código la responsabilidad emocional está repartida entre `EmotionalEventController`, `EmotionalRecommendationController` y `UserEmotionalStateController`.

## 2. Mapa general del flujo

### BotConfigController

`BotConfigController -> GetBotConfigUseCase / UpdateBotConfigUseCase -> BotConfigService -> ChatConfigRepository -> ChatConfigRepositoryImpl -> IChatConfigJpaRepository -> chat_config`

### ChatController

`ChatController -> ChatUseCase -> ChatService -> PromptBuilderService + LLMChatPort + ChatMemoryPort + UserVectorMemoryService + ChatEmotionalRecommendationService`

`ChatService -> AnthropicChatAdapter -> Spring AI ChatModel -> Anthropic`

`ChatService -> JpaChatMemoryAdapter -> ChatSessionRepository / ChatMessageRepository -> JPA -> chat_session / chat_message / emotion`

`ChatService -> UserVectorMemoryService -> VectorMemoryService -> SpringAiVectorMemoryService -> EmbeddingModel + pgvector vector_store`

`ChatService -> ChatEmotionalRecommendationService -> EmotionalAnalysisPort -> AnthropicEmotionalAnalysisAdapter -> Anthropic`

`ChatEmotionalRecommendationService -> GetEmotionalRecommendationsUseCase -> EmotionalRecommendationService -> ActivityRepository -> activity`

`ChatEmotionalRecommendationService -> CreateEmotionalEventUseCase -> EmotionalEventRepository -> emotional_event`

### EmotionalController funcional

`EmotionalEventController -> CreateEmotionalEventUseCase / UpdateEmotionalEventDecisionUseCase / UpdateEmotionalEventFeedbackUseCase -> EmotionalEventRepository -> emotional_event`

`EmotionalRecommendationController -> GetEmotionalRecommendationsUseCase -> EmotionalRecommendationService -> ActivityRepository -> activity`

`UserEmotionalStateController -> SaveUserEmotionalStateUseCase -> UserEmotionalStateRepository -> user_emotional_state`

## 3. BotConfigController

Existe para exponer configuración editable del chatbot desde backoffice. La configuración guardada se usa después en `ChatService.basePrompt()` como prompt base para construir prompts enriquecidos.

Endpoints:

* `GET /api/admin/chat/config`
* `PUT /api/admin/chat/config`

Este controller está en capa presentation. Su responsabilidad es traducir HTTP/DTO a comandos de dominio y devolver DTOs. No debería conocer JPA, entidades, Anthropic, vector store ni lógica de prompt avanzada.

### Método: `getConfig`

* **Endpoint:** `GET /api/admin/chat/config`
* **Responsabilidad:** devolver la configuración actual del bot.
* **Qué recibe:** no recibe body.
* **Qué valida o delega:** no valida; delega a `GetBotConfigUseCase.execute()`.
* **A quién llama:** `getBotConfigUseCase.execute()`.
* **Qué devuelve:** `ResponseEntity<BotConfigResponse>` con `id`, `risk_detection_enabled`, `system_prompt`.
* **Por qué existe:** separa el endpoint HTTP de la forma interna `ChatConfig`.
* **Cómo lo explicaría en la reunión:** "Este método solo consulta configuración; no decide comportamiento del bot, delega el caso de uso y transforma el modelo a response."

### Método: `updateConfig`

* **Endpoint:** `PUT /api/admin/chat/config`
* **Responsabilidad:** actualizar prompt base y flag de riesgo.
* **Qué recibe:** `UpdateBotConfigRequest` con `riskDetectionEnabled` y `systemPrompt`.
* **Qué valida o delega:** usa `@Valid`; `systemPrompt` tiene `@NotBlank`. Luego arma `UpdateBotConfigCommand`.
* **A quién llama:** `updateBotConfigUseCase.execute(command)`.
* **Qué devuelve:** `BotConfigResponse` con la configuración persistida.
* **Por qué existe:** permite modificar la configuración sin exponer entidades JPA.
* **Cómo lo explicaría en la reunión:** "El controller traduce la request a un command; la regla de actualización vive en el service, no en el endpoint."

### Clase: `BotConfigController`

* **Ubicación:** `backend/src/main/java/com/huly/backend/presentation/controller/BotConfigController.java`
* **Capa:** presentation.
* **Responsabilidad:** exponer endpoints admin para leer y actualizar `ChatConfig`.
* **Quién la usa:** Spring MVC.
* **Métodos importantes:** `getConfig`, `updateConfig`, `toResponse`.
* **Qué retorna o modifica:** retorna DTOs; indirectamente modifica `chat_config` mediante el use case.
* **Motivo de diseño:** mantiene el borde HTTP separado del dominio.

### Clase: `GetBotConfigUseCase`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/useCase/GetBotConfigUseCase.java`
* **Capa:** domain/application.
* **Responsabilidad:** caso de uso de lectura de configuración.
* **Quién la usa:** `BotConfigController.getConfig`.
* **Métodos importantes:** `execute()`, que llama a `BotConfigService.getConfig()`.
* **Qué retorna o modifica:** retorna `ChatConfig`; no modifica DB.
* **Motivo de diseño:** encapsula la acción de aplicación aunque sea simple.

### Clase: `UpdateBotConfigUseCase`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/useCase/UpdateBotConfigUseCase.java`
* **Capa:** domain/application.
* **Responsabilidad:** caso de uso de actualización.
* **Quién la usa:** `BotConfigController.updateConfig`.
* **Métodos importantes:** `execute(UpdateBotConfigCommand)`.
* **Qué retorna o modifica:** persiste cambios mediante `BotConfigService.updateConfig`.
* **Motivo de diseño:** separa intención de aplicación de transporte HTTP.

### Clase: `BotConfigService`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/service/BotConfigService.java`
* **Capa:** domain service.
* **Responsabilidad:** regla de lectura/actualización de `ChatConfig`.
* **Quién la usa:** ambos use cases.
* **Métodos importantes:** `getConfig()` devuelve la primera config o default; `updateConfig()` conserva valores anteriores si el command trae `null`.
* **Qué retorna o modifica:** lee y guarda por `ChatConfigRepository`.
* **Motivo de diseño:** concentra la regla de default y merge parcial.

### Clase: `ChatConfigRepository`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/repository/chat/ChatConfigRepository.java`
* **Capa:** domain port.
* **Responsabilidad:** contrato de persistencia de configuración.
* **Quién la usa:** `BotConfigService` y `ChatService.basePrompt()`.
* **Métodos importantes:** `findById`, `save`, `findFirst`.
* **Qué retorna o modifica:** retorna o guarda `ChatConfig`.
* **Motivo de diseño:** inversión de dependencias: dominio no depende de JPA.

### Clase: `ChatConfigRepositoryImpl`

* **Ubicación:** `backend/src/main/java/com/huly/backend/infrastructure/repository/jpaRepository/implementation/ChatConfigRepositoryImpl.java`
* **Capa:** infrastructure.
* **Responsabilidad:** implementar `ChatConfigRepository` con Spring Data JPA.
* **Quién la usa:** Spring la inyecta donde se pide `ChatConfigRepository`.
* **Métodos importantes:** `findFirst()` usa `jpa.findAll().stream().findFirst()`, `save()` mapea dominio-entidad-dominio.
* **Qué retorna o modifica:** lee y escribe tabla `chat_config`.
* **Motivo de diseño:** adapta el puerto de dominio a persistencia real.

### Clase: `ChatConfigMapper`

* **Ubicación:** `backend/src/main/java/com/huly/backend/infrastructure/repository/mapper/ChatConfigMapper.java`
* **Capa:** infrastructure.
* **Responsabilidad:** convertir `ChatConfigEntity` a `ChatConfig` y viceversa.
* **Quién la usa:** `ChatConfigRepositoryImpl`.
* **Métodos importantes:** `toEntity`, `toDomain`.
* **Qué retorna o modifica:** no persiste; solo transforma.
* **Motivo de diseño:** evita mezclar modelo de dominio con entidad JPA.

### Clase: `ChatConfigEntity`

* **Ubicación:** `backend/src/main/java/com/huly/backend/infrastructure/repository/entity/ChatConfigEntity.java`
* **Capa:** infrastructure.
* **Responsabilidad:** entidad JPA de la tabla `chat_config`.
* **Quién la usa:** `IChatConfigJpaRepository`.
* **Métodos importantes:** getters/setters generados por Lombok.
* **Qué retorna o modifica:** representa filas de DB.
* **Motivo de diseño:** aislar detalles JPA fuera del dominio.

### DTOs/modelos conectados

* `UpdateBotConfigRequest`: request HTTP; exige `systemPrompt` no vacío.
* `BotConfigResponse`: response HTTP con nombres JSON `risk_detection_enabled` y `system_prompt`.
* `UpdateBotConfigCommand`: command de dominio con valores editables.
* `ChatConfig`: modelo de dominio con `id`, `riskDetectionEnabled`, `systemPrompt`.

## 4. ChatController

`ChatController` maneja el chatbot. Tiene tres flujos: mensaje normal, mensaje por streaming y lectura de historial. Obtiene el usuario desde `SecurityContextHolder`, busca el `AppUserEntity` por email y pasa `userId` al caso de uso.

### Método: `chat`

* **Endpoint:** `POST /api/chat`
* **Responsabilidad:** procesar un mensaje y devolver respuesta completa.
* **Qué recibe:** `ChatRequest` con `message` y `conversationId`, ambos `@NotBlank`.
* **Qué valida o delega:** validación Jakarta por `@Valid`; obtención de usuario autenticado en el controller.
* **A quién llama:** `chatUseCase.execute(request.message(), request.conversationId(), userId)`.
* **Qué devuelve:** `ChatResponse` con `huly_reply`, emoción, intensidad, `suggested_action`, `generated_challenge`, metadata de riesgo.
* **Por qué existe:** endpoint sin streaming para recibir respuesta estructurada y recomendación de actividad.
* **Cómo lo explicaría en la reunión:** "El controller autentica y traduce; el procesamiento real del chatbot ocurre en `ChatService`."

### Método: `stream`

* **Endpoint:** `POST /api/chat/stream`
* **Responsabilidad:** devolver respuesta del bot en Server-Sent Events.
* **Qué recibe:** `ChatRequest`.
* **Qué valida o delega:** valida manualmente null/blank y devuelve evento `error` si faltan datos.
* **A quién llama:** `streamChatUseCase.execute(message, conversationId, userId)`.
* **Qué devuelve:** `Flux<ServerSentEvent<ChatStreamEventResponse>>` con eventos `delta`, `metadata`, `done` o `error`.
* **Por qué existe:** permite UI con respuesta progresiva.
* **Cómo lo explicaría en la reunión:** "El stream separa texto natural por chunks y metadata al final; no entrega `suggested_action`."

### Método: `getHistory`

* **Endpoint:** `GET /api/chat/{conversationId}/messages`
* **Responsabilidad:** listar mensajes guardados de una conversación.
* **Qué recibe:** path `conversationId`, query `page` y `size`.
* **Qué valida o delega:** arma `PageRequest` ordenado por `createdAt`; delega búsqueda.
* **A quién llama:** `listChatHistoryUseCase.execute(conversationId, pageable)`.
* **Qué devuelve:** `ChatHistoryPageResponse`.
* **Por qué existe:** hidratar la UI con historial persistido.
* **Cómo lo explicaría en la reunión:** "El historial viene de DB normal; no reconstruye recomendaciones ni memoria vectorial."

### Clase: `ChatController`

* **Ubicación:** `backend/src/main/java/com/huly/backend/presentation/controller/ChatController.java`
* **Capa:** presentation.
* **Responsabilidad:** borde HTTP del chat.
* **Quién la usa:** Spring MVC.
* **Métodos importantes:** `chat`, `stream`, `getHistory`, mappers privados.
* **Qué retorna o modifica:** retorna DTOs; indirectamente guarda mensajes, memorias y eventos emocionales.
* **Motivo de diseño:** centraliza adaptación HTTP/SSE, aunque hoy también busca el usuario con un repositorio de infraestructura.

### Clase: `ChatUseCase`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/useCase/chat/ChatUseCase.java`
* **Capa:** domain/application.
* **Responsabilidad:** caso de uso de mensaje normal.
* **Quién la usa:** `ChatController.chat`.
* **Métodos importantes:** `execute(message, conversationId, userId)`.
* **Qué retorna o modifica:** retorna `ChatReply`; delega efectos a `ChatService`.
* **Motivo de diseño:** separa endpoint de orquestación del chat.

### Clase: `StreamChatUseCase`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/useCase/chat/StreamChatUseCase.java`
* **Capa:** domain/application.
* **Responsabilidad:** caso de uso de chat streaming.
* **Quién la usa:** `ChatController.stream`.
* **Métodos importantes:** `execute(...)`.
* **Qué retorna o modifica:** retorna `Flux<ChatStreamEvent>`; delega a `ChatService.streamMessage`.
* **Motivo de diseño:** expone la intención de aplicación sin detalles HTTP SSE.

### Clase: `ListChatHistoryUseCase`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/useCase/chat/ListChatHistoryUseCase.java`
* **Capa:** domain/application.
* **Responsabilidad:** listar mensajes de una conversación.
* **Quién la usa:** `ChatController.getHistory`.
* **Métodos importantes:** `execute(conversationId, pageable)`.
* **Qué retorna o modifica:** retorna `Page<ChatMessage>` desde `ChatMessageRepository`.
* **Motivo de diseño:** separa consulta de historial del controller.

### Clase: `ChatService`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/service/chat/ChatService.java`
* **Capa:** domain service.
* **Responsabilidad:** orquestar prompt, memoria, IA, persistencia de chat, memoria vectorial y recomendación emocional.
* **Quién la usa:** `ChatUseCase` y `StreamChatUseCase`.
* **Métodos importantes:** `processMessage`, `streamMessage`, `buildBlockingContext`, `buildStreamingContext`, `enrichWithEmotionalRecommendation`, `saveUserMessage`, `saveAssistantMessage`.
* **Qué retorna o modifica:** retorna `ChatReply` o eventos; guarda mensajes en DB normal; guarda memoria vectorial del mensaje de usuario; puede crear `emotional_event`.
* **Motivo de diseño:** concentra el flujo de negocio del chat y depende de puertos (`LLMChatPort`, `ChatMemoryPort`) en vez de adapters concretos.

### Clase: `PromptBuilderService`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/service/chat/PromptBuilderService.java`
* **Capa:** domain service.
* **Responsabilidad:** construir prompts enriquecidos para IA.
* **Quién la usa:** `ChatService` y `ChatEmotionalRecommendationService`.
* **Métodos importantes:** `buildEnrichedPrompt`, `buildStreamingPrompt`, `buildMetadataPrompt`, `buildEmotionalAnalysisPrompt`.
* **Qué retorna o modifica:** retorna strings de prompt; no persiste.
* **Motivo de diseño:** evita duplicar instrucciones de IA y mantiene separado el armado del prompt.

### Clase: `AnthropicChatAdapter`

* **Ubicación:** `backend/src/main/java/com/huly/backend/infrastructure/adapter/anthropic/AnthropicChatAdapter.java`
* **Capa:** infrastructure adapter.
* **Responsabilidad:** implementar `LLMChatPort` y `StreamingLLMChatPort` usando Spring AI `ChatModel`.
* **Quién la usa:** `ChatService` vía interfaces.
* **Métodos importantes:** `chat`, `stream`, `parseResponse`, `buildMessages`.
* **Qué retorna o modifica:** llama a Anthropic; parsea JSON a `ChatReply`; en stream devuelve chunks de texto.
* **Motivo de diseño:** permite cambiar proveedor de IA sin cambiar el dominio, si se crea otro adapter.

### Clase: `AnthropicEmotionalAnalysisAdapter`

* **Ubicación:** `backend/src/main/java/com/huly/backend/infrastructure/adapter/anthropic/AnthropicEmotionalAnalysisAdapter.java`
* **Capa:** infrastructure adapter.
* **Responsabilidad:** analizar emocionalmente un mensaje con IA y devolver `EmotionalAnalysisResult`.
* **Quién la usa:** `ChatEmotionalRecommendationService` vía `EmotionalAnalysisPort`.
* **Métodos importantes:** `analyze`, `parseResponse`, `parseEmotion`.
* **Qué retorna o modifica:** llama a Anthropic; no persiste.
* **Motivo de diseño:** separa análisis IA externo de la lógica de recomendación.

### Clase: `ChatEmotionalRecommendationService`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/service/chat/ChatEmotionalRecommendationService.java`
* **Capa:** domain service.
* **Responsabilidad:** decidir si el mensaje merece recomendación, pedir actividades recomendadas y persistir el evento emocional.
* **Quién la usa:** `ChatService.enrichWithEmotionalRecommendation`.
* **Métodos importantes:** `evaluate`, `analyze`, `recommendAndPersistEvent`, `resolveRecommendationAnalysis`, `toSuggestedAction`.
* **Qué retorna o modifica:** retorna `ChatRecommendationOutcome`; puede crear `EmotionalEvent`.
* **Motivo de diseño:** mantiene fuera de `ChatService` la lógica emocional específica del chatbot.

### Clase: `JpaChatMemoryAdapter`

* **Ubicación:** `backend/src/main/java/com/huly/backend/infrastructure/adapter/memory/JpaChatMemoryAdapter.java`
* **Capa:** infrastructure adapter.
* **Responsabilidad:** implementar `ChatMemoryPort` con DB relacional.
* **Quién la usa:** `ChatService`.
* **Métodos importantes:** `getHistory`, `addMessage`.
* **Qué retorna o modifica:** lee/crea sesión y guarda mensajes en `chat_session`/`chat_message`.
* **Motivo de diseño:** dominio pide memoria conversacional por puerto; JPA queda aislado.

### Clase: `UserVectorMemoryService`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/service/vector/UserVectorMemoryService.java`
* **Capa:** domain service.
* **Responsabilidad:** buscar recuerdos relevantes y guardar memorias del usuario.
* **Quién la usa:** `ChatService`, y otros flujos como diario/onboarding.
* **Métodos importantes:** `findRelevantUserMemories`, `rememberChatMessage`, `rememberJournalEntry`, `rememberOnboardingGoals`.
* **Qué retorna o modifica:** consulta vector store; guarda memoria vectorial si la política lo permite.
* **Motivo de diseño:** abstrae memoria semántica por usuario.

### Clase: `SpringAiVectorMemoryService`

* **Ubicación:** `backend/src/main/java/com/huly/backend/infrastructure/adapter/vector/SpringAiVectorMemoryService.java`
* **Capa:** infrastructure adapter.
* **Responsabilidad:** implementar `VectorMemoryService` con `EmbeddingModel`, `JdbcTemplate` y pgvector.
* **Quién la usa:** `UserVectorMemoryService` vía puerto.
* **Métodos importantes:** `saveMemory`, `findRelevantMemories`, `deleteMemories`.
* **Qué retorna o modifica:** guarda embeddings en `vector_store`; busca por distancia coseno.
* **Motivo de diseño:** encapsula detalles de embeddings, SQL y pgvector.

### Clase: `NoOpVectorMemoryService`

* **Ubicación:** `backend/src/main/java/com/huly/backend/infrastructure/adapter/vector/NoOpVectorMemoryService.java`
* **Capa:** infrastructure adapter.
* **Responsabilidad:** implementación nula de memoria vectorial.
* **Quién la usa:** Spring cuando `spring.ai.vectorstore.type=none`.
* **Métodos importantes:** `saveMemory`, `findRelevantMemories`, `deleteMemories`.
* **Qué retorna o modifica:** no guarda nada; retorna lista vacía.
* **Motivo de diseño:** permite correr el sistema sin proveedor de embeddings.

### Clase: `VectorMemoryPolicy`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/service/vector/VectorMemoryPolicy.java`
* **Capa:** domain service.
* **Responsabilidad:** validar y decidir qué contenido se recuerda.
* **Quién la usa:** `SpringAiVectorMemoryService`.
* **Métodos importantes:** `normalizeContent`, `shouldRemember`, `validateSaveCommand`, `validateAndNormalizeQuery`.
* **Qué retorna o modifica:** no persiste; filtra mensajes triviales y señales sensibles.
* **Motivo de diseño:** la política de memoria no queda mezclada con SQL.

### Clase: `ChatbotVectorMemoryPolicy`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/service/vector/ChatbotVectorMemoryPolicy.java`
* **Capa:** domain service.
* **Responsabilidad:** definir señales del chatbot que merecen memoria.
* **Quién la usa:** `VectorMemoryPolicy`.
* **Métodos importantes:** `sourceType`, `shouldRemember`.
* **Qué retorna o modifica:** retorna boolean de política.
* **Motivo de diseño:** cada fuente puede tener reglas distintas.

### Clase: `UserProfileFactExtractor`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/service/vector/UserProfileFactExtractor.java`
* **Capa:** domain service.
* **Responsabilidad:** extraer hechos simples de perfil y mejorar queries sobre datos personales.
* **Quién la usa:** `UserVectorMemoryService`.
* **Métodos importantes:** `extractProfileFacts`, `asksForProfileFact`, `buildProfileRecallQuery`.
* **Qué retorna o modifica:** retorna textos de memoria o queries; no persiste directamente.
* **Motivo de diseño:** separar heurística de perfil del flujo general de chat.

### Repositorios conectados al chat

* `ChatSessionRepository`: puerto de dominio para sesiones.
* `ChatMessageRepository`: puerto de dominio para mensajes.
* `ChatSessionRepositoryImpl`: adapter JPA que crea/busca `chat_session`.
* `ChatMessageRepositoryImpl`: adapter JPA que guarda `chat_message` y `emotion`.
* `RiskWordRepository`: usado por `ChatService` para enriquecer prompt con palabras de riesgo.
* `ChatConfigRepository`: usado por `ChatService.basePrompt()` para obtener prompt base.

### DTOs/modelos conectados al chat

* `ChatRequest`: `message`, `conversationId`.
* `ChatResponse`: `huly_reply`, `detected_emotion`, `intensity`, `suggested_action`, `generated_challenge`, `metadata`.
* `ChatStreamEventResponse`: evento SSE para stream.
* `ChatHistoryPageResponse` y `ChatMessageResponse`: historial paginado.
* `ChatReply`: modelo interno de respuesta del bot.
* `ConversationMessage`: mensaje del historial enviado al modelo.
* `SuggestedChatAction`: actividad recomendada con `activityId` y `emotionalEventId`.
* `EmotionalAnalysisResult`: salida estructurada del análisis emocional IA.

### Configuraciones relevantes

* `SecurityConfig`: todo queda autenticado salvo rutas permitidas; `/api/chat` requiere JWT.
* `AIConfig`: crea `ChatClient` si está habilitado.
* `ChatModelConfig`: expone `AnthropicChatModel` como `ChatModel` cuando `app.ai.provider=anthropic`.
* `application.properties`: Anthropic para chat; OpenAI opcional para embeddings; vector store apagado por defecto.
* `V12__create_vector_store.sql`: crea `vector_store` con pgvector e índices.

## 5. EmotionalController

No encontré `EmotionalController` como clase real. La funcionalidad emocional conectada a IA/recomendaciones está en tres controladores:

* `EmotionalEventController`
* `EmotionalRecommendationController`
* `UserEmotionalStateController`

Además, `ChatController` usa internamente esta lógica por `ChatEmotionalRecommendationService`: cuando el chatbot recomienda una actividad, crea un `EmotionalEvent`.

### Método: `EmotionalEventController.create`

* **Endpoint:** `POST /api/emotional-events`
* **Responsabilidad:** registrar un evento emocional.
* **Qué recibe:** `EmotionalEventRequest` con source, emoción, VAD, intensidad, recomendación y actividad.
* **Qué valida o delega:** `@Valid` en DTO; use case valida rangos y existencia de actividades.
* **A quién llama:** `createEmotionalEventUseCase.execute(toCommand(request))`.
* **Qué devuelve:** `201 Created` con `EmotionalEventResponse`.
* **Por qué existe:** persistir eventos emocionales manuales o generados por distintos módulos.
* **Cómo lo explicaría en la reunión:** "Este endpoint registra el evento; no calcula IA, solo valida y persiste datos emocionales."

### Método: `EmotionalEventController.updateDecision`

* **Endpoint:** `PATCH /api/emotional-events/{id}/decision`
* **Responsabilidad:** guardar si el usuario aceptó, ignoró o eligió otra actividad.
* **Qué recibe:** path `id`, body `EmotionalEventDecisionRequest`.
* **Qué valida o delega:** `decision` es `@NotNull`; el use case resuelve `chosenActivityId`.
* **A quién llama:** `updateDecisionUseCase.execute(id, new UpdateRecommendationDecisionCommand(...))`.
* **Qué devuelve:** evento actualizado.
* **Por qué existe:** cerrar el ciclo de recomendación con feedback de decisión.
* **Cómo lo explicaría en la reunión:** "La aceptación/rechazo no vive en chat; se guarda como decisión del evento emocional."

### Método: `EmotionalEventController.updateFeedback`

* **Endpoint:** `PATCH /api/emotional-events/{id}/feedback`
* **Responsabilidad:** guardar feedback posterior sobre una recomendación/evento.
* **Qué recibe:** `feedbackScore`, `feedbackText`.
* **Qué valida o delega:** el use case valida score 1..5.
* **A quién llama:** `updateFeedbackUseCase.execute(...)`.
* **Qué devuelve:** evento actualizado.
* **Por qué existe:** registrar evaluación subjetiva de la recomendación.
* **Cómo lo explicaría en la reunión:** "Es feedback explícito del usuario sobre el evento emocional persistido."

### Método: `EmotionalRecommendationController.recommend`

* **Endpoint:** `POST /api/emotional-recommendations`
* **Responsabilidad:** calcular ranking de actividades según VAD/intensidad/objetivo.
* **Qué recibe:** `EmotionalRecommendationRequest`.
* **Qué valida o delega:** DTO valida rangos y campos requeridos; use case valida rangos nuevamente.
* **A quién llama:** `getRecommendationsUseCase.execute(toQuery(request))`.
* **Qué devuelve:** `EmotionalRecommendationResponse` con lista de recomendaciones y `fallbackUsed`.
* **Por qué existe:** exponer el motor de recomendación emocional sin pasar por chat.
* **Cómo lo explicaría en la reunión:** "Este endpoint no llama IA; usa datos emocionales ya calculados para rankear actividades."

### Método: `UserEmotionalStateController.save`

* **Endpoint:** `POST /api/emotional-states`
* **Responsabilidad:** guardar estado emocional VAD de usuario.
* **Qué recibe:** `UserEmotionalStateRequest` con `userId`, VAD, `intensity`, `source`.
* **Qué valida o delega:** `@Valid` valida rangos y obligatorios.
* **A quién llama:** `saveUserEmotionalStateUseCase.execute(...)`.
* **Qué devuelve:** `201 Created` con `UserEmotionalState`.
* **Por qué existe:** persistir mediciones emocionales simples.
* **Cómo lo explicaría en la reunión:** "Es un registro de estado emocional; no hay IA ni recomendación en este endpoint."

### Clase: `EmotionalEventController`

* **Ubicación:** `backend/src/main/java/com/huly/backend/presentation/controller/EmotionalEventController.java`
* **Capa:** presentation.
* **Responsabilidad:** endpoints CRUD parcial para eventos emocionales.
* **Quién la usa:** frontend o flujos externos; el chatbot crea eventos por use case, no por HTTP.
* **Métodos importantes:** `create`, `updateDecision`, `updateFeedback`.
* **Qué retorna o modifica:** modifica `emotional_event`.
* **Motivo de diseño:** separa decisiones/feedback del flujo de chat.

### Clase: `EmotionalRecommendationController`

* **Ubicación:** `backend/src/main/java/com/huly/backend/presentation/controller/EmotionalRecommendationController.java`
* **Capa:** presentation.
* **Responsabilidad:** exponer recomendación emocional basada en VAD.
* **Quién la usa:** clientes HTTP que ya tienen datos emocionales.
* **Métodos importantes:** `recommend`.
* **Qué retorna o modifica:** retorna ranking; no persiste.
* **Motivo de diseño:** reutilizar motor de recomendación sin IA conversacional.

### Clase: `UserEmotionalStateController`

* **Ubicación:** `backend/src/main/java/com/huly/backend/presentation/controller/UserEmotionalStateController.java`
* **Capa:** presentation.
* **Responsabilidad:** guardar estado emocional VAD.
* **Quién la usa:** clientes HTTP.
* **Métodos importantes:** `save`.
* **Qué retorna o modifica:** guarda `user_emotional_state`.
* **Motivo de diseño:** endpoint simple para telemetría emocional.

### Clase: `CreateEmotionalEventUseCase`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/useCase/CreateEmotionalEventUseCase.java`
* **Capa:** domain/application.
* **Responsabilidad:** validar y crear `EmotionalEvent`.
* **Quién la usa:** `EmotionalEventController` y `ChatEmotionalRecommendationService`.
* **Métodos importantes:** `execute`, `validate`, `validateActivityExists`.
* **Qué retorna o modifica:** guarda `emotional_event`.
* **Motivo de diseño:** permite crear eventos desde HTTP o desde chat sin duplicar reglas.

### Clase: `UpdateEmotionalEventDecisionUseCase`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/useCase/UpdateEmotionalEventDecisionUseCase.java`
* **Capa:** domain/application.
* **Responsabilidad:** actualizar decisión de recomendación.
* **Quién la usa:** `EmotionalEventController.updateDecision`.
* **Métodos importantes:** `execute`, `resolveChosenActivityId`.
* **Qué retorna o modifica:** actualiza `recommendationDecision`, `chosenActivityId`, `updatedAt`.
* **Motivo de diseño:** encapsula regla: `IGNORED` no tiene actividad elegida; `ACCEPTED` usa la recomendada si no llega otra.

### Clase: `UpdateEmotionalEventFeedbackUseCase`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/useCase/UpdateEmotionalEventFeedbackUseCase.java`
* **Capa:** domain/application.
* **Responsabilidad:** guardar feedback 1..5 y texto.
* **Quién la usa:** `EmotionalEventController.updateFeedback`.
* **Métodos importantes:** `execute`.
* **Qué retorna o modifica:** actualiza `feedbackScore`, `feedbackText`, `updatedAt`.
* **Motivo de diseño:** mantiene validación de feedback fuera del controller.

### Clase: `GetEmotionalRecommendationsUseCase`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/useCase/GetEmotionalRecommendationsUseCase.java`
* **Capa:** domain/application.
* **Responsabilidad:** validar query emocional y pedir ranking de actividades.
* **Quién la usa:** `EmotionalRecommendationController` y `ChatEmotionalRecommendationService`.
* **Métodos importantes:** `execute`, `validateRange`.
* **Qué retorna o modifica:** lee actividades y retorna `EmotionalRecommendationResult`; no persiste.
* **Motivo de diseño:** coordina repositorio y servicio de ranking.

### Clase: `EmotionalRecommendationService`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/service/EmotionalRecommendationService.java`
* **Capa:** domain service.
* **Responsabilidad:** rankear actividades según VAD, efectos esperados, objetivo e intensidad.
* **Quién la usa:** `GetEmotionalRecommendationsUseCase`.
* **Métodos importantes:** `recommend`, `toRecommendation`, `isInRange`, `effectScore`, `goalScore`, `intensityScore`.
* **Qué retorna o modifica:** retorna recomendaciones ordenadas; no llama IA ni DB.
* **Motivo de diseño:** lógica de negocio pura y testeable.

### Clase: `SaveUserEmotionalStateUseCase`

* **Ubicación:** `backend/src/main/java/com/huly/backend/domain/useCase/SaveUserEmotionalStateUseCase.java`
* **Capa:** domain/application.
* **Responsabilidad:** crear y guardar `UserEmotionalState`.
* **Quién la usa:** `UserEmotionalStateController`.
* **Métodos importantes:** `execute`.
* **Qué retorna o modifica:** guarda `user_emotional_state`.
* **Motivo de diseño:** separa construcción del modelo de la capa HTTP.

### Repositorios y entidades emocionales

* `EmotionalEventRepository`: puerto de dominio para guardar/buscar eventos.
* `EmotionalEventRepositoryImpl`: adapter JPA; mapea `EmotionalEvent` a `EmotionalEventEntity`.
* `EmotionalEventEntity`: entidad JPA de `emotional_event`.
* `ActivityRepository`: puerto para leer actividades y validar existencia.
* `ActivityRepositoryImpl`: adapter JPA sobre `activity`.
* `Activity`: modelo de dominio con rangos VAD y efectos.
* `UserEmotionalStateRepository`: puerto para guardar estado emocional.
* `UserEmotionalStateRepositoryImpl`: adapter JPA.
* `UserEmotionalStateEntity`: entidad JPA de `user_emotional_state`.

### DTOs/modelos emocionales conectados

* `EmotionalEventRequest`: entrada para registrar evento.
* `EmotionalEventDecisionRequest`: `decision`, `chosenActivityId`.
* `EmotionalEventFeedbackRequest`: score y texto.
* `EmotionalEventResponse`: salida completa del evento.
* `EmotionalRecommendationRequest`: entrada VAD para recomendar.
* `EmotionalRecommendationResponse`: lista y `fallbackUsed`.
* `EmotionalRecommendationItemResponse`: actividad, score y razón.
* `UserEmotionalStateRequest`: entrada VAD para estado emocional.
* `EmotionalEventSource`: `CHATBOT`, `DIARY`, `JOURNAL`, `CLOUD`, `NUBE`, `OTHER`.
* `RecommendationDecision`: `ACCEPTED`, `IGNORED`, `CHOSE_OTHER`.

## 6. Flujo completo explicado como historia

### BotConfigController

Cuando el frontend de backoffice pide la configuración, el controller no decide nada sobre IA. Recibe la request, llama al caso de uso y devuelve un DTO. El caso de uso delega en `BotConfigService`, que sabe obtener la primera configuración o devolver un default. Cuando se actualiza, el service conserva valores anteriores si llegan `null` y guarda por el repositorio de dominio. La implementación real usa JPA, pero esa dependencia queda en infraestructura.

### ChatController

Cuando el frontend manda un mensaje a `POST /api/chat`, el controller obtiene el email del usuario autenticado desde Spring Security, busca su id y delega en `ChatUseCase`. `ChatService` arma contexto: prompt base desde `chat_config`, palabras de riesgo, historial conversacional y memoria vectorial relevante. Después llama a `LLMChatPort`, que en esta configuración es `AnthropicChatAdapter`. La respuesta se parsea como JSON estructurado. Luego se hace análisis emocional separado con `EmotionalAnalysisPort`; si corresponde, se rankea una actividad, se crea un `emotional_event` y se agrega `suggested_action` a la respuesta. Finalmente se guardan el mensaje del usuario, el del asistente y memoria vectorial del mensaje del usuario.

En streaming, el modelo devuelve texto natural por chunks. Al final se calcula metadata emocional/riesgo con otro prompt, se guarda el mensaje del asistente y se emiten eventos `metadata` y `done`. No se devuelve `suggested_action` en el DTO de streaming.

### EmotionalController funcional

La parte emocional permite registrar eventos, guardar decisión del usuario, feedback y estados VAD. También puede calcular recomendaciones a partir de datos VAD ya conocidos. El cálculo de recomendación no llama a IA: usa actividades persistidas con rangos VAD y efectos esperados. La IA aparece cuando el chatbot necesita transformar texto libre en análisis emocional estructurado; ese análisis se usa para crear el evento y proponer una actividad.

## 7. Preguntas que probablemente me puede hacer el senior

* **¿Por qué el controller llama a un use case y no directamente al repository?**  
  Porque el controller es borde HTTP. El use case expresa la intención de aplicación y evita acoplar transporte con persistencia.

* **¿Dónde está la lógica de negocio del chat?**  
  Principalmente en `ChatService`, `PromptBuilderService`, `ChatEmotionalRecommendationService`, `EmotionalRecommendationService` y políticas de vector memory.

* **¿Qué pasaría si mañana cambiamos Anthropic por OpenAI?**  
  Habría que crear otro adapter que implemente `LLMChatPort`, `StreamingLLMChatPort` y probablemente `EmotionalAnalysisPort`. El dominio no debería cambiar.

* **¿El controller conoce infraestructura?**  
  `ChatController` sí conoce `AppUserRepository` y `AppUserEntity`, que son infraestructura. Es un punto a revisar.

* **¿Se respeta inversión de dependencias?**  
  En IA, memoria y repositorios principales sí: dominio depende de puertos. Hay excepciones, como la búsqueda de usuario en `ChatController`.

* **¿Qué parte es testeable sin levantar Spring?**  
  `EmotionalRecommendationService`, `BotConfigService`, use cases y políticas de memoria son testeables con mocks o instancias simples.

* **¿La recomendación emocional usa IA?**  
  El ranking no. La IA se usa para convertir texto del chat en `EmotionalAnalysisResult`.

* **¿Qué guarda la DB normal?**  
  Configuración, sesiones, mensajes, emociones, eventos emocionales, actividades y estados emocionales.

* **¿Qué guarda la base vectorial?**  
  Memorias semánticas del usuario, como mensajes de chat recordables y hechos de perfil, en `vector_store`.

* **¿Qué pasa con `conversationId`?**  
  Es el identificador externo de conversación. Si no existe sesión, `JpaChatMemoryAdapter.addMessage` crea una `chat_session` asociada al usuario.

* **¿Aceptar o rechazar recomendación está conectado al chat?**  
  El chat crea el `emotional_event` y devuelve `emotional_event_id`. La aceptación/rechazo se guarda por `PATCH /api/emotional-events/{id}/decision`, fuera de `ChatController`.

* **¿El stream es equivalente al POST normal?**  
  No. El stream devuelve texto y metadata final, pero no incluye `suggested_action`.

## 8. Posibles puntos débiles o cosas a revisar

* **Punto a revisar:** `ChatController` usa `AppUserRepository` y `AppUserEntity` directamente.  
  **Por qué podría ser un problema:** acopla presentation con infraestructura JPA.  
  **Cómo lo defendería si me preguntan:** funciona como adaptación rápida para obtener `userId` desde autenticación.  
  **Cómo se podría mejorar a futuro:** crear un `GetCurrentUserUseCase` o un servicio/port de usuario autenticado.

* **Punto a revisar:** `ChatConfigRepositoryImpl.findFirst()` usa `findAll().stream().findFirst()`.  
  **Por qué podría ser un problema:** no define orden ni limita desde DB.  
  **Cómo lo defendería si me preguntan:** el diseño parece asumir una sola fila de configuración.  
  **Cómo se podría mejorar a futuro:** crear query `findFirstByOrderByIdAsc()` o constraint/config singleton.

* **Punto a revisar:** `riskDetectionEnabled` se guarda pero `ChatService` siempre carga `riskWordRepository.findAllActive()`.  
  **Por qué podría ser un problema:** no se ve en el código que el flag desactive riesgo.  
  **Cómo lo defendería si me preguntan:** el campo existe para configuración, pero su uso efectivo no está conectado en este flujo.  
  **Cómo se podría mejorar a futuro:** aplicar el flag al construir prompt o retirar el campo si no se usa.

* **Punto a revisar:** `POST /api/chat/stream` no devuelve `suggested_action`.  
  **Por qué podría ser un problema:** el flujo streaming no tiene la misma capacidad funcional que el normal.  
  **Cómo lo defendería si me preguntan:** está diseñado para texto progresivo; metadata se calcula al final.  
  **Cómo se podría mejorar a futuro:** emitir un evento final de recomendación con `suggested_action`.

* **Punto a revisar:** `EmotionalRecommendationRequest.source` no tiene `@NotNull`, pero el record acepta source.  
  **Por qué podría ser un problema:** puede llegar null aunque conceptualmente sea parte del evento.  
  **Cómo lo defendería si me preguntan:** el ranking actual no usa `source` para calcular score.  
  **Cómo se podría mejorar a futuro:** validar si el source es obligatorio para auditoría.

* **Punto a revisar:** `UserEmotionalStateController` devuelve modelo de dominio directamente.  
  **Por qué podría ser un problema:** expone dominio como contrato HTTP.  
  **Cómo lo defendería si me preguntan:** es un endpoint simple y el modelo coincide con la respuesta esperada.  
  **Cómo se podría mejorar a futuro:** crear `UserEmotionalStateResponse`.

* **Punto a revisar:** `ChatEmotionalRecommendationService` tiene umbrales hardcodeados.  
  **Por qué podría ser un problema:** ajustar sensibilidad requiere cambiar código.  
  **Cómo lo defendería si me preguntan:** son reglas de negocio explícitas y testeables.  
  **Cómo se podría mejorar a futuro:** mover umbrales a configuración versionada.

* **Punto a revisar:** algunos services de dominio tienen anotaciones Spring (`@Service`, `@Component`).  
  **Por qué podría ser un problema:** dominio queda acoplado al framework.  
  **Cómo lo defendería si me preguntan:** es una arquitectura limpia pragmática, no purista; permite inyección sencilla.  
  **Cómo se podría mejorar a futuro:** separar módulo de dominio puro y wiring en application/infrastructure.

## 9. Glosario rápido

* **Controller:** clase Spring que recibe HTTP y devuelve DTOs; ejemplo `ChatController`.
* **Use Case:** clase que representa una acción de aplicación; ejemplo `ChatUseCase`.
* **Service:** clase con lógica u orquestación; ejemplo `ChatService`.
* **Repository:** puerto de dominio para persistencia; ejemplo `ChatConfigRepository`.
* **DTO:** objeto de entrada/salida HTTP; ejemplo `ChatResponse`.
* **Entity:** clase JPA ligada a tabla; ejemplo `ChatMessageEntity`.
* **Interface:** contrato que permite invertir dependencias; ejemplo `LLMChatPort`.
* **Adapter:** implementación concreta de un puerto; ejemplo `AnthropicChatAdapter`.
* **Vector Store:** tabla `vector_store` con embeddings para buscar recuerdos por similitud.
* **Embedding:** vector numérico generado por `EmbeddingModel` para representar texto.
* **Chat Memory:** historial conversacional guardado en DB normal por `JpaChatMemoryAdapter`.
* **Streaming:** respuesta progresiva por SSE en `POST /api/chat/stream`.
* **IA externa:** Anthropic vía Spring AI `ChatModel`; OpenAI puede usarse para embeddings.
* **Clean Architecture:** separación entre presentation, casos de uso, dominio, puertos e infraestructura.

## 10. Resumen final para memorizar

## Resumen para decir en la reunión

En esta parte del backend explico tres áreas principales.  
Primero, `BotConfigController` administra la configuración del chatbot: prompt base y flag de riesgo.  
Ese controller no llama a IA; solo delega en use cases y termina persistiendo en `chat_config`.  
Segundo, `ChatController` es el flujo central del chatbot.  
Recibe el mensaje, obtiene el usuario autenticado y delega en `ChatUseCase` o `StreamChatUseCase`.  
`ChatService` arma el contexto con prompt base, historial, palabras de riesgo y memoria vectorial.  
La respuesta se genera por `LLMChatPort`, implementado por `AnthropicChatAdapter`.  
Después se guarda el historial en DB normal y se intenta guardar memoria vectorial del usuario.  
Si el análisis emocional detecta malestar, `ChatEmotionalRecommendationService` pide una recomendación, crea un `EmotionalEvent` y devuelve una `suggested_action`.  
Tercero, no hay una clase `EmotionalController`; la parte emocional está repartida en eventos, recomendaciones y estados emocionales.  
`EmotionalRecommendationService` no usa IA: rankea actividades por VAD, efectos e intención del usuario.  
La IA emocional aparece cuando el chatbot convierte texto libre en `EmotionalAnalysisResult`.  
La decisión del usuario se guarda con `PATCH /api/emotional-events/{id}/decision`.  
La arquitectura usa varios puertos de dominio para aislar IA, memoria y repositorios.  
El punto más discutible es que `ChatController` busca el usuario con un repositorio JPA directamente.  
La mejora natural sería mover esa obtención de usuario a un caso de uso o puerto de aplicación.
