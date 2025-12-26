# Stock Market Data Platform

O aplicație cloud-native pentru colectarea și analiza în timp real a datelor bursiere.

## 🏗 Arhitectură

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│                 │     │                  │     │                 │
│    Frontend     │────▶│  Gateway (Java)  │────▶│   Analytics     │
│    (React)      │◀────│  Spring Boot     │◀────│   (Rust)        │
│                 │     │                  │     │                 │
└─────────────────┘     └────────┬─────────┘     └────────┬────────┘
                                 │                        │
                                 ▼                        ▼
                        ┌────────────────┐       ┌────────────────┐
                        │   PostgreSQL   │       │   Prometheus   │
                        │    Database    │       │    Metrics     │
                        └────────────────┘       └────────────────┘
```

## ✨ Funcționalități

- **Gateway REST API** - Spring Boot cu documentație OpenAPI/Swagger
- **Autentificare JWT** - Login și register cu token-uri JWT
- **WebSocket în timp real** - Actualizări live de prețuri și alerte
- **Microserviciu Analytics** - Rust pentru agregări și detecție anomalii
- **Persistență PostgreSQL** - Salvarea prețurilor și alertelor
- **Observabilitate** - Health checks, metrici Prometheus și loguri structurate
- **Dashboard modern** - React cu Tailwind CSS

## 🚀 Pornire rapidă

### Cerințe
- Docker și Docker Compose
- (Opțional) Java 17, Node.js 20, Rust pentru dezvoltare locală

### Pornire cu Docker Compose

```bash
# Clonează repository-ul
git clone <repo-url>
cd ProiectLab

# Pornește întreaga aplicație
docker compose up -d

# Verifică statusul
docker compose ps
```

### Accesare

| Serviciu | URL |
|----------|-----|
| Frontend | http://localhost:3000 |
| Gateway API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Analytics | http://localhost:8081 |
| Prometheus | http://localhost:9090 |

### Credențiale demo
- **Username:** `demo`
- **Password:** `demo123`

## 📁 Structura proiectului

```
.
├── analytics-service/     # Microserviciu Rust
│   ├── src/
│   │   ├── main.rs       # Entry point și server HTTP
│   │   ├── analysis.rs   # Algoritmi de analiză (MA, anomalii)
│   │   └── price_generator.rs  # Generator de prețuri simulate
│   ├── Cargo.toml
│   └── Dockerfile
├── frontend/              # Aplicație React
│   ├── src/
│   │   ├── components/   # Componente UI (Dashboard, Charts)
│   │   ├── hooks/        # Custom hooks (useAuth, useWebSocket)
│   │   └── services/     # API și WebSocket services
│   └── Dockerfile
├── src/                   # Spring Boot Gateway
│   └── main/java/org/example/proiectlab/
│       ├── config/       # Configurări (Security, WebSocket, OpenAPI)
│       ├── controller/   # REST Controllers
│       ├── dto/          # Data Transfer Objects
│       ├── model/        # Entități JPA
│       ├── repository/   # JPA Repositories
│       ├── security/     # JWT și autentificare
│       └── service/      # Business logic
├── observability/        # Configurații Prometheus
├── secrets/              # Docker secrets (parole)
├── docker-compose.yml    # Orchestrare containere
└── .github/workflows/    # CI/CD pipeline
```

## 🔧 Dezvoltare locală

### Backend (Spring Boot)
```bash
# Pornește PostgreSQL
docker compose up postgres -d

# Rulează aplicația
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Analytics (Rust)
```bash
cd analytics-service
cargo run
```

## 🔐 Secret Management

Secretele sunt gestionate prin Docker Secrets:
- `secrets/db_password.txt` - Parola PostgreSQL
- `secrets/jwt_secret.txt` - Cheia JWT

**⚠️ Nu comite secretele în repository pentru producție!**

## 📊 Metrici și Observabilitate

- **Health checks:** `/actuator/health` (Gateway), `/health` (Analytics)
- **Prometheus metrics:** `/actuator/prometheus` (Gateway), `/metrics` (Analytics)
- **Metrici disponibile:**
  - `stock_market.price_updates` - Număr actualizări prețuri
  - `stock_market.alerts` - Număr alerte generate
  - `stock_market.processing_time` - Latență procesare

## 🔄 API Endpoints

### Autentificare
- `POST /api/auth/register` - Înregistrare utilizator
- `POST /api/auth/login` - Autentificare

### Prețuri
- `GET /api/prices` - Ultimele prețuri pentru toate simbolurile
- `GET /api/prices/{ticker}` - Ultimul preț pentru un simbol
- `GET /api/prices/{ticker}/history` - Istoricul prețurilor
- `GET /api/prices/{ticker}/variations` - Variații recente

### Alerte
- `GET /api/alerts` - Alerte recente
- `GET /api/alerts/unread` - Alerte necitite
- `PUT /api/alerts/{id}/read` - Marchează alertă ca citită

### Simboluri
- `GET /api/symbols` - Lista tuturor simbolurilor
- `POST /api/symbols` - Adaugă simbol nou

## 🧪 Testare

```bash
# Teste Java
./mvnw test

# Teste Rust
cd analytics-service && cargo test

# Build frontend
cd frontend && npm run build
```

## 📦 CI/CD

Pipeline-ul GitHub Actions include:
1. Build și test Gateway (Java)
2. Build și test Analytics (Rust)
3. Build Frontend (React)
4. Construcție și push imagini Docker

## 📝 Licență

MIT License

