# AGENTS.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

师生答疑系统 (Student-Teacher Q&A System) is a full-stack educational platform with:
- **Backend**: Spring Boot 3.1.5 (Java 17) monolith in `qa-system-backend/`
- **Frontend**: Vue 3.4 + Vite 5.0 SPA in `qa-system-frontend/`
- **AI Integration**: LangChain4j 0.35 with SiliconFlow API for intelligent Q&A and document rewriting

## Common Commands

### Backend (qa-system-backend/)
```bash
# Run development server
mvn spring-boot:run

# Build JAR (skip tests)
mvn clean package -DskipTests

# Run tests
mvn test

# Build for production
mvn clean package -DskipTests -Pprod
```

### Frontend (qa-system-frontend/)
```bash
# Install dependencies
npm install

# Start dev server (http://localhost:5173)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint
```

## Architecture

### Backend Structure
```
qa-system-backend/src/main/java/com/qasystem/
├── controller/          # REST API endpoints (16 controllers)
├── service/             # Business logic layer
├── entity/              # JPA/MyBatis-Plus entities
├── mapper/              # MyBatis-Plus data access
├── dto/                 # Data transfer objects
├── config/              # Spring configuration classes
├── security/            # JWT auth (JwtTokenProvider, JwtAuthenticationFilter)
├── websocket/           # WebSocket handlers (doc rewrite streaming)
├── common/              # Shared utilities (ApiResponse)
└── exception/           # Global exception handling
```

### Frontend Structure
```
qa-system-frontend/src/
├── api/                 # Axios API modules (auth, ai, doc, forum, etc.)
├── views/               # Page components by role
│   ├── admin/           # Admin dashboard, user management
│   ├── student/         # Question posting, collections, follows
│   ├── teacher/         # Answer center
│   ├── common/          # Shared views (AiAssistant, DocStudio, ChatRoom)
│   └── forum/           # Forum posts
├── layouts/             # Role-based layouts (StudentLayout, TeacherLayout, AdminLayout)
├── stores/              # Pinia stores (user state)
├── router/              # Vue Router with role-based guards
└── utils/               # Axios interceptors with JWT handling
```

### Key Design Patterns

**Backend:**
- MyBatis-Plus for ORM with `LambdaQueryWrapper` for type-safe queries
- Spring Security with custom `JwtAuthenticationFilter`
- `ServiceImpl<Mapper, Entity>` base class pattern for services
- Unified `Result<T>` response wrapper in controllers
- SSE (Server-Sent Events) for AI streaming responses
- WebSocket for document rewrite real-time output

**Frontend:**
- Pinia with `pinia-plugin-persistedstate` for token persistence
- Axios interceptors auto-attach JWT Bearer tokens
- Element Plus with auto-import via `unplugin-vue-components`
- Role-based routing with `meta.roles` guards
- `@` alias resolves to `src/`

## User Roles

Three roles with distinct permissions:
- `STUDENT`: Post questions, use AI assistant, document workspace, forum participation
- `TEACHER`: All student permissions + answer questions
- `ADMIN`: Full system management, user/content moderation, system configuration

## API Base Paths

| Module | Backend Path | Notes |
|--------|--------------|-------|
| Auth | `/api/v1/auth` | Login, register, logout |
| Questions | `/api/v1/questions` | Q&A CRUD |
| Answers | `/api/v1/answers` | Answer submissions |
| Forum | `/forum` | Posts and comments |
| AI Assistant | `/api/ai` | Chat, sessions, streaming |
| Document | `/api/v1/doc` | Upload, plagiarism check, AI rewrite |
| Admin | `/api/v1/admin` | User/content management |

## External Dependencies

- **MySQL 8.0**: Primary database
- **Redis 6.0+**: Session caching, conversation history cache
- **SiliconFlow API**: LLM provider (configured via `langchain4j.open-ai` properties)
- **Tencent COS**: Optional file storage (commented out by default)

## Database Migrations

SQL scripts are in `qa-system-backend/src/main/resources/db/`:
- `init-data.sql` - Initial schema and seed data
- `ai-schema.sql` - AI conversation tables
- `doc-check-schema.sql` - Document plagiarism check tables
- `chat_tables.sql` - Instant messaging tables
- `add-forum-table.sql` - Forum tables

## Environment Configuration

Backend environment variables (can be set in `application.yml` or as env vars):
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `JWT_SECRET`
- `SILICONFLOW_API_KEY`

Frontend dev proxy is configured in `vite.config.js` to forward `/api`, `/forum`, `/uploads`, `/ws` to `localhost:8080`.

## Testing Notes

- Backend uses Spring Boot Test with `spring-security-test`
- Run specific test class: `mvn test -Dtest=ClassName`
- Frontend has no test framework configured yet

## AI Assistant Implementation

The `AiAssistantService` uses LangChain4j with:
- Multi-turn conversation context via Redis caching (24h TTL, max 10 messages)
- RAG-style retrieval from existing Q&A database
- Custom system prompt defining "鸟儿Pro" assistant persona
- Streaming responses via `StreamingChatLanguageModel` and SSE
- Automatic question categorization (计算机科学, 高等数学, etc.)
