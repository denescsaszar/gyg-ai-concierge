# AI Travel Concierge for GetYourGuide

> An AI-powered travel experience discovery engine that helps travelers find personalized activities through natural language conversation.

Instead of browsing endless lists, travelers simply describe what they're looking for — _"I'm in Berlin for 2 days with kids, love history and street food"_ — and the AI concierge returns curated, relevant experiences with smart reasoning about why each one fits.

## Why This Exists

Millions of travelers visit GetYourGuide to find experiences, but discovery is still largely manual: search, scroll, filter, repeat. This project explores how AI can transform that journey into a conversation — understanding intent, preferences, and context to surface the right experiences instantly.

## Tech Stack

| Layer              | Technology                | Why                                                                                 |
| ------------------ | ------------------------- | ----------------------------------------------------------------------------------- |
| **Backend**        | Kotlin + Spring Boot 3    | JVM-native, concise, type-safe — built for scalable microservices                   |
| **Frontend**       | Vue 3 + TypeScript + Vite | Reactive, type-safe UI with fast dev experience                                     |
| **AI Engine**      | Claude API (Anthropic)    | Advanced reasoning for understanding traveler intent and generating recommendations |
| **Infrastructure** | Docker Compose            | One command to run the full stack locally                                           |

## Architecture

```text
┌─────────────────────────────────────────────────┐
│                   Frontend (Vue 3)              │
│         Chat UI ← → Activity Cards/Grid         │
└──────────────────┬──────────────────────────────┘
                   │ REST API
┌──────────────────▼──────────────────────────────┐
│               Backend (Spring Boot)             │
│  ┌─────────────┐  ┌─────────────┐              │
│  │ Activity API │  │ AI Concierge│──→ Claude API│
│  │  (CRUD)      │  │  Service    │              │
│  └─────────────┘  └─────────────┘              │
└─────────────────────────────────────────────────┘
```

## Features

- **Natural language search** — Describe your ideal experience in plain English
- **AI-powered recommendations** — Context-aware suggestions with explanations for each match
- **Smart filtering** — Budget, duration, group size, interests — parsed from conversation
- **Activity discovery** — Browse, search, and explore curated travel experiences
- **Responsive design** — Works seamlessly on desktop and mobile

## Getting Started

### Prerequisites

- Java 21+
- Node.js 18+
- An Anthropic API key ([get one here](https://console.anthropic.com))

### Run the backend

```bash
cd backend
./gradlew bootRun
```

The API will be available at `http://localhost:8080`

### Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`

## API Endpoints

| Method | Endpoint                 | Description               |
| ------ | ------------------------ | ------------------------- |
| `GET`  | `/api/health`            | Service health check      |
| `GET`  | `/api/activities`        | List all activities       |
| `GET`  | `/api/activities/search` | Search with filters       |
| `POST` | `/api/concierge/chat`    | AI concierge conversation |

## Project Structure

```text
gyg-ai-concierge/
├── backend/                  # Kotlin/Spring Boot API
│   └── src/main/kotlin/com/gyg/concierge/
│       ├── controller/       # REST endpoints
│       ├── model/            # Domain models
│       ├── service/          # Business logic + AI integration
│       └── repository/       # Data access
├── frontend/                 # Vue 3 + TypeScript SPA
│   └── src/
│       ├── components/       # Reusable UI components
│       ├── views/            # Page-level components
│       ├── services/         # API client layer
│       └── types/            # TypeScript interfaces
└── docker-compose.yml
```

## License

MIT
