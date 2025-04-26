## [Unreleased]
### Added
- Implemented AI Chat Service (`AIService`) using Sambanova API.
- Added configuration for Sambanova API key, base URL, and model (`ai.sambanova` in `application.yml`, `SambanovaProperties`).
- Added DTOs for Sambanova API request/response (`ChatRequestDTO`, `ChatMessageDTO`, `ChatResponseDTO`, etc.) in `api` module.
- Exposed AI chat functionality via `POST /api/ai/chat` endpoint in `AIChatController` (requires authentication).
- Added `RestTemplate` bean to `ServiceAutoConfig`.
- Added streaming response support through `POST /api/ai/chat/stream` endpoint and `streamingEnabled` configuration option.
- Enhanced `AIService` to support `List<ChatMessageDTO>` as input for more advanced prompt engineering.
- Added advanced endpoints with multi-message support: `POST /api/ai/chat/advanced` and `POST /api/ai/chat/stream/advanced`.
- Implemented article summary generation functionality through `POST /api/ai/summary` endpoint.
- Improved exception handling with custom `AIServiceException` and `AIConfigurationException` runtime exceptions.
