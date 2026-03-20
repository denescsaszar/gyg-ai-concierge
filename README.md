# 🧳 AI Travel Concierge for GetYourGuide

> Discover Berlin's best experiences through conversation — not endless scrolling.

![Status](https://img.shields.io/badge/status-live-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Vue.js](https://img.shields.io/badge/Vue.js-3-4FC08D?logo=vue.js&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white)
![Claude API](https://img.shields.io/badge/Claude%20API-Anthropic-D97706)
![License](https://img.shields.io/badge/license-MIT-blue)

### 🔗 [Live Demo → gyg-ai-concierge.vercel.app](https://gyg-ai-concierge.vercel.app)

---

## Table of Contents

- [Why This Exists](#why-this-exists)
- [The Problem & The Solution](#the-problem--the-solution)
- [How It Works](#how-it-works)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
- [Deployment](#deployment)
- [Project Structure](#project-structure)
- [What I'd Build Next](#what-id-build-next)

---

## Why This Exists

Millions of travelers visit GetYourGuide to discover experiences — but discovery is still largely manual: search, scroll, filter, repeat. What if travelers could simply **say what they want** and get personalized recommendations instantly?

This project explores that idea. Instead of browsing endless lists, a traveler describes their ideal trip:

> _"I'm in Berlin for 2 days with kids, love history and street food — budget around €100"_

…and an AI concierge returns curated, relevant experiences with reasoning about why each one fits. It understands context, preferences, and constraints — turning discovery from a chore into a conversation.

---

## The Problem & The Solution

| ❌ Traditional Discovery | ✅ AI Concierge |
|---|---|
| Keyword search with rigid filters | Natural language — describe what you want |
| Browse hundreds of listings manually | Curated recommendations in seconds |
| No understanding of group dynamics | Knows if you're traveling with kids, a partner, solo |
| Price/duration filters are separate steps | Understands "budget-friendly afternoon activity" |
| Results feel generic | Explains _why_ each experience fits your request |

---

## How It Works

**1. Ask anything** → _"What's a fun outdoor activity for a couple?"_

**2. AI understands intent** → Parses preferences: outdoor, romantic, 2 people

**3. Smart matching** → Searches the activity catalog with context-aware reasoning

**4. Personalized response** → Returns recommendations with explanations + highlights matching cards in the UI

The AI Concierge doesn't just search keywords — it **reasons** about what you'd enjoy. Ask for "something unique most tourists miss" and it won't recommend the Brandenburg Gate.

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| **Backend** | Kotlin + Spring Boot 3 | JVM-native, null-safe, concise — built for scalable microservices |
| **Frontend** | Vue 3 + TypeScript + Vite | Reactive Composition API, type-safe, instant HMR |
| **AI Engine** | Claude API (Anthropic) | Advanced reasoning for understanding traveler intent and nuance |
| **Deployment** | Railway + Vercel | Backend on Railway (JVM), frontend on Vercel (static), zero-config CI/CD |
| **Containerization** | Docker + Docker Compose | One command to run the full stack locally |

---

## Architecture

```text
┌──────────────────────────────────────────────────────┐
│              Frontend (Vue 3 + TypeScript)            │
│                                                      │
│   ┌──────────────┐    ┌────────────────────────┐     │
│   │ Activity Grid │    │   Chat Widget (AI)     │     │
│   │  Search/Filter│    │   Floating conversation │     │
│   └──────┬───────┘    └───────────┬────────────┘     │
│          │                        │                   │
│          │    REST API (/api)     │                   │
└──────────┼────────────────────────┼───────────────────┘
           │                        │
┌──────────▼────────────────────────▼───────────────────┐
│              Backend (Kotlin + Spring Boot 3)          │
│                                                        │
│   ┌─────────────────┐    ┌──────────────────────┐     │
│   │ ActivityService  │    │  ConciergeService    │     │
│   │ CRUD + Search    │    │  AI Chat + Matching  │──────── → Claude API
│   └─────────────────┘    └──────────────────────┘     │
│                                                        │
│   ┌─────────────────┐    ┌──────────────────────┐     │
│   │ ActivityController│   │ ConciergeController  │     │
│   │ GET /activities  │    │ POST /concierge/chat │     │
│   └─────────────────┘    └──────────────────────┘     │
└────────────────────────────────────────────────────────┘
```

**Key design decisions:**

- **Controller → Service → Data** pattern for clean separation of concerns
- **System prompt injection** with full activity catalog so Claude can reason over real data
- **`[ACTIVITIES:1,4,7]` tag parsing** — Claude embeds activity IDs in responses, backend extracts them to highlight cards in the frontend
- **Vite proxy in dev / direct URL in prod** — seamless local development with zero CORS issues

---

## Features

### 🤖 AI-Powered Concierge
Natural language conversation with Claude. Understands preferences, group size, budget, interests, and time constraints. Returns personalized recommendations with explanations.

### 🔍 Smart Activity Search
Filter by keyword, category, price range, and duration. Fuzzy matching across titles, descriptions, and highlights.

### 🏷️ AI Recommended Badges
When the concierge suggests activities, those cards get an "AI Recommended" badge and sort to the top — connecting the chat to the visual grid.

### ⚡ Loading & Error States
Spinner while data fetches, friendly error messages if something breaks. Production-quality UX.

### 📱 Responsive Design
Works on desktop, tablet, and mobile. GetYourGuide-inspired design with their signature `#FF5533` orange.

---

## API Endpoints

| Method | Endpoint | Description | Example |
|--------|----------|-------------|---------|
| `GET` | `/api/health` | Service health check | `{"status":"ok"}` |
| `GET` | `/api/activities` | List all 10 Berlin experiences | Returns full catalog |
| `GET` | `/api/activities/{id}` | Single activity by ID | `/api/activities/4` |
| `GET` | `/api/activities/search` | Filter with query params | `?query=food&maxPrice=50` |
| `POST` | `/api/concierge/chat` | AI concierge conversation | Send message + history |

### Chat Request Example

```json
{
  "message": "What's a good outdoor activity for a couple?",
  "conversationHistory": [
    { "role": "user", "content": "We're visiting Berlin this weekend" },
    { "role": "assistant", "content": "Welcome! Berlin has amazing experiences..." }
  ]
}
```

### Chat Response

```json
{
  "message": "I'd recommend the **Kayak Tour on the Spree River** — it's romantic, scenic, and you'll see the Reichstag from the water...",
  "recommendedActivityIds": [4, 7]
}
```

---

## Getting Started

### Prerequisites

- Java 21+ (`brew install openjdk@21`)
- Node.js 18+ (`brew install node@20`)
- An Anthropic API key → [console.anthropic.com](https://console.anthropic.com)

### 1. Clone & set up

```bash
git clone https://github.com/denescsaszar/gyg-ai-concierge.git
cd gyg-ai-concierge
```

### 2. Start the backend

```bash
cd backend
export ANTHROPIC_API_KEY=your-key-here
./gradlew bootRun
```

API runs on → [http://localhost:8080](http://localhost:8080)

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

App runs on → [http://localhost:5173](http://localhost:5173)

### 4. Or use Docker

```bash
ANTHROPIC_API_KEY=your-key-here docker-compose up
```

Full stack runs on → [http://localhost:3000](http://localhost:3000)

---

## Deployment

| Service | Platform | URL |
|---------|----------|-----|
| Backend | Railway | `charismatic-vision-production-874b.up.railway.app` |
| Frontend | Vercel | [gyg-ai-concierge.vercel.app](https://gyg-ai-concierge.vercel.app) |

Both platforms auto-deploy on push to `main`. Zero-config CI/CD.

---

## Project Structure

```text
gyg-ai-concierge/
├── backend/                          # Kotlin + Spring Boot 3
│   ├── Dockerfile                    # Multi-stage JDK → JRE build
│   └── src/main/kotlin/com/gyg/concierge/
│       ├── ConciergeApplication.kt   # Spring Boot entry point
│       ├── config/
│       │   └── WebConfig.kt          # CORS configuration
│       ├── controller/
│       │   ├── HealthController.kt   # GET /api/health
│       │   ├── ActivityController.kt # Activity CRUD endpoints
│       │   └── ConciergeController.kt# AI chat endpoint
│       ├── model/
│       │   ├── Activity.kt           # Domain model + Category enum
│       │   └── ChatModels.kt         # Request/response DTOs
│       ├── data/
│       │   └── SampleActivities.kt   # 10 curated Berlin experiences
│       └── service/
│           ├── ActivityService.kt    # Search & filter logic
│           └── ConciergeService.kt   # Claude API integration
├── frontend/                         # Vue 3 + TypeScript + Vite
│   ├── Dockerfile                    # Multi-stage Node → nginx build
│   ├── nginx.conf                    # Production reverse proxy
│   └── src/
│       ├── App.vue                   # Root component
│       ├── router/index.ts           # SPA routing
│       ├── types/activity.ts         # TypeScript interfaces
│       ├── services/api.ts           # API client layer
│       ├── components/
│       │   ├── ActivityCard.vue      # Experience card with badges
│       │   └── ChatWidget.vue        # Floating AI concierge
│       └── views/
│           └── HomeView.vue          # Main discovery page
├── docker-compose.yml                # Full-stack orchestration
└── README.md
```

---

## What I'd Build Next

If this were a production feature at GetYourGuide:

- **Streaming responses** — Claude supports streaming; show words appearing in real-time
- **Persistent conversations** — Save chat history per user session
- **Real GetYourGuide API** — Replace sample data with live inventory
- **Booking integration** — "Book now" buttons that complete the purchase flow
- **Multi-language support** — Claude can respond in the user's language automatically
- **Analytics dashboard** — Track which recommendations convert to bookings
- **Vector search** — Embed activities and queries for semantic matching at scale

---

> Built as a portfolio project demonstrating full-stack development, AI integration, and product thinking for the Associate Software Engineer role at GetYourGuide.

## License

MIT
