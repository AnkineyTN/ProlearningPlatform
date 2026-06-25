# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

E-learning platform backend. **Java 21**, **Spring Boot 3.2.4**, **Maven** (`mvnw` wrapper).

- Base package: `com.cabybara.prolearningplatform`
- Entry point: [ProlearningplatformApplication.java](src/main/java/com/cabybara/prolearningplatform/ProlearningplatformApplication.java)
- API context path `/api` on port `8080`. Swagger UI at `/api/api-docs`.

## Commands

The local shell is **PowerShell on Windows**, so use the `.cmd` wrapper:

| Task | Command |
|---|---|
| Build | `.\mvnw.cmd clean package` |
| Run locally | `.\mvnw.cmd spring-boot:run` |
| Run all tests | `.\mvnw.cmd clean test` (exactly what CI runs — see [ci.yml](.github/workflows/ci.yml)) |
| Run a single test | `.\mvnw.cmd test -Dtest=ClassName#methodName` |

On Linux/CI the wrapper is `./mvnw`.

## Required setup / gotchas

- **`.env` is required.** `DotenvApplicationInitializer` loads a `.env` at startup; [application.yml](src/main/resources/application.yml) references many env vars with no defaults (`POSTGRES_URL`, `POSTGRES_USERNAME/PASSWORD`, `REDIS_HOST/PORT`, Cloudinary, PayOS, Google OAuth2, Firebase credentials JSON, mail). **The app will not start without them.** `.env.prod` shows which vars matter.
- **BYOK env vars (new):** `LLM_MASTER_KEY` (Base64 of exactly 32 bytes — generate with `openssl rand -base64 32`; **must stay constant** across restarts, changing it breaks decryption of stored keys) and `AI_INTERNAL_API_KEY` (shared secret with the AI Service). Both are required; the app starts without them but AI features will fail at runtime.
- **Database schema is NOT auto-managed**: `spring.jpa.hibernate.ddl-auto: none`. Schema changes are **manual SQL scripts** in [src/database/](src/database/) (e.g. `roadmap.sql`, `payment.sql`). New entities/columns require a matching SQL script there — Hibernate will not create them.
- **Profiles**: `dev` (default) / `prod`, selected via the `PROFILE` env var.
- External infra: PostgreSQL, Redis (caching, per-entity TTLs), RabbitMQ (async email), Firebase FCM, PayOS payments, Cloudinary media. `docker-compose.yml` currently only runs the prebuilt backend image (Redis/Postgres services are commented out).

## Architecture & project structure (navigation map)

All Java lives under `src/main/java/com/cabybara/prolearningplatform/`.

**Layered flow:** `controller/` (REST, ~35) → `service/<feature>/` (interface) + `service/<feature>/impl/` (logic) → `repository/` (Spring Data JPA, + `repository/impl/` for custom queries) → `model/<feature>/` (JPA entities, ~54). DTOs travel in/out via `dto/request/<feature>/` and `dto/response/<feature>/`, converted with `mapper/` (MapStruct).

**Top-level packages:**
```
controller/               REST endpoints (flat, one class per area)
service/<feature>/        interfaces + impl/ subpackage  (business logic)
repository/               Spring Data JPA  (+ impl/ for custom/native queries)
model/<feature>/          JPA entities  (+ composite_key/ for @IdClass/@EmbeddedId)
dto/request/<feature>/    inbound payloads
dto/response/<feature>/   outbound payloads
dto/internal/             service-to-service / AI / payos / roadmap internal DTOs
dto/helper/               shared DTO helpers (incl. Social)
mapper/                   MapStruct entity <-> DTO mappers (+ helpers/)
configuration/            Spring config: security, redis, rabbitmq, swagger, etc.
consumer/email/           RabbitMQ listeners (async email)
scheduler/                cron jobs (reminders, summaries, cleanup)
aspect/                   AOP cross-cutting concerns
converter/                JPA attribute converters
enums/                    enums (+ AI/)
exception/                custom exceptions + handlers
utils/                    utilities
```

**Feature domains** — each appears as parallel slices across `controller / service/<f> / model/<f> / dto/{request,response}/<f>`. Use this to jump straight to a feature:

| Domain | Controller(s) | service / model / dto subpackage |
|---|---|---|
| Auth & users | `AuthController`, `UserController`, `AdminUserController` | `auth`, `user`, `otp`, `permission` |
| Exams | `ExamController` | `exam` |
| Flashcards | `FlashcardController`, `CardItemController`, `FlashcardReviewController`, `FlashcardStudySessionController`, `FlashcardGameHistoryController` | `flashcard`, `flashcard_study_session`, `review`, `set` |
| Notes | `NoteController`, `NoteWebSocketController`, `CollabController` | `note` |
| Roadmaps | `RoadmapController` | `roadmap` |
| Knowledge analysis | `KnowledgeAnalysisController` | `knowledge`, `ai` |
| Payments | `PaymentController`, `PayOSWebhookController` | `payment` |
| Pomodoro | `PomodoroController`, `PomodoroAdminController` | `pomodoro` |
| Todos / goals | `TodoController`, `GoalController` | `todo` |
| Social | `SocialController` | `social` |
| Notifications | `NotificationController`, `UserNotificationPreferenceController`, `SetNotificationPreferenceController` | `notification`, `fcm`, `noti` |
| Onboarding | `OnboardingSubmissionController`, `AdminOnboardingController` | `onboarding` |
| Appeals | `UserAppealController`, `AdminAppealController` | `appeal` |
| Activity / stats | `ActivityLogController`, `AdminPlatformStatsController` | `activity`, `admin` |
| Search | `SearchController` | `search` |
| Uploads / assets | `AssetUploadController` | `upload`, `asset`, `cloudinary`, `file` |
| Review bundles | `ReviewBundleController`, `ReviewDevController` | `review` |
| LLM config (BYOK) | `UserLlmConfigController` | `llm` |

**Cross-cutting:**
- Auth: JWT + Google OAuth2 + Spring Security ([SecurityConfig.java](src/main/java/com/cabybara/prolearningplatform/configuration/SecurityConfig.java)).
- Caching: `service/redis/` with per-entity TTLs from `application.yml`.
- i18n via `messages_*.properties` (English/Vietnamese); email templates in `resources/templates/email/`.
- DB migrations: SQL scripts in [src/database/](src/database/), one per feature.

**Rule of thumb for finding code:** identify the feature domain from the table, then look across the same-named subpackage in `controller/`, `service/<feature>/impl/`, `model/<feature>/`, `repository/`, and `dto/{request,response}/<feature>/`.

## Conventions

- **Uniform response envelope**: controllers don't return raw DTOs. They return `ResponseEntity<ApiResponse<T>>` built via `ResponseUtil.success(message, data, metadata)` / `ResponseUtil.error(...)` (in `utils/`). For paginated endpoints, pass a `PaginationResponseDto.builder()...build()` as the `metadata` and `page.getContent()` as the `data`. Method-level auth uses `@PreAuthorize` and Swagger docs use `@Operation`/`@Tag`.
- **Lombok** is used — prefer its annotations over hand-written boilerplate. Request DTOs use `@Data` with jakarta validation annotations; response DTOs use `@Data @Builder`.
- **MapStruct** for entity↔DTO mapping — add mapper methods rather than manual conversion. Partial/PATCH updates use `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` so null fields are skipped (see `mapper/UserMapper.java`).
- Keep new endpoints/services consistent with the existing controller→service→repository layering (interface in `service/<feature>/`, implementation in `service/<feature>/impl/`).
- When adding/altering persisted fields, add a SQL migration script in `src/database/` (`ddl-auto` is `none`).

## BYOK LLM Architecture (branch `new/config-model`)

Each user owns their own LLM provider + model + API key. All AI generation endpoints forward the user's active config to the AI Service via HTTP headers.

**Key design decisions:**
- **Encryption at service layer** (`utils/AesGcmEncryptor.java`, AES-256-GCM, master key from env). Do NOT use a JPA `AttributeConverter` — Hibernate-managed converters can't inject Spring `@Value`.
- **`AiServiceClient`** (`service/ai/AiServiceClient.java`) is the single call site for the AI Service. It adds `X-Internal-Api-Key` to every call and `X-LLM-Provider` / `X-LLM-Model` / `X-LLM-Api-Key` to generation calls. All 5 AI impl classes now inject `AiServiceClient` (not `RestHttpClientUtil` directly).
- **`/files/process` is internal-only** — no `X-LLM-*` headers. Use `postInternalOnly()`, not `postForGeneration()`.
- **Async threads don't have SecurityContext**: `heavyTaskExecutor` is a plain `ThreadPoolTaskExecutor`. For the topic-assignment async chain and roadmap content generation, `userId` is passed explicitly as a method parameter rather than resolved from `SecurityContextHolder`.
- **Partial unique index** in PostgreSQL (`WHERE is_active = TRUE`) ensures exactly 1 active config per user at the DB level — no application-side uniqueness check needed.
- **API key never logged**: `RestHttpClientUtil` body/response logging is at `DEBUG` level. Header values are never logged anywhere.
- **DB table**: `src/database/user_llm_config.sql` — must be applied manually (as with all migrations).
- **FE integration guide**: `docs/FE_BYOK_INTEGRATION.md`.

**Fallback behavior:** when a user has no active LLM config, `getDecryptedConfig()` returns `null`. `AIServiceClient.generationHeaders(null)` omits the 3 `X-LLM-*` headers → AI Service falls back to its own default provider/model (Groq). No error is thrown for the "no config" case.

**Error mapping from AI Service (in `AiServiceClient`):**
| AI Service status | Mapped to | Reason |
|---|---|---|
| 401 | 502 | Internal key misconfiguration (server-side) |
| 400 "missing required llm" | 400 `LlmNotConfiguredException` | Defensive — should not occur with fallback in place |
| 402 / 403 | 400 | User's provider key issue (billing/permission) |
| 429 | 429 | Provider rate limit |
| Others | 502 | AI service unavailable |

**Timezone fix:** `TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))` added as the first line of `main()` in `ProlearningplatformApplication.java`. The alias `Asia/Saigon` is rejected by the PostgreSQL server; the canonical IANA name is accepted.

## Testing

Test coverage is currently minimal — essentially the context-load smoke test `ProlearningplatformApplicationTests.java`. CI compiles and runs `./mvnw clean test`.

Notable test classes added for BYOK:
- `AesGcmEncryptorTest` — round-trip, tamper detection, invalid key
- `UserLlmConfigServiceImplTest` — CRUD lifecycle, auto-active, ownership check, decryption
