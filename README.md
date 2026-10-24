# Cricket Live – Real-Time Cricket Score Management System

A scalable and real-time cricket score management system built with **Spring Boot** that simulates, processes, and displays live cricket match information via REST APIs, Server-Sent Events (SSE), and a responsive dark-mode web dashboard.

---

## 📋 Student & Assignment Details
- **Student Name:** Swanand Uttarwar
- **Division:** CS L
- **Roll No:** 33
- **Repository:** [https://github.com/SWANAND2025/cricket-live](https://github.com/SWANAND2025/cricket-live)

---

## 🚀 Key Features
- **Real-Time Simulation Engine:** Background scheduler (`@Scheduled`) simulates ball-by-ball match progress every 1.5 seconds with random probabilistic outcomes (wickets, dot balls, boundaries, sixes).
- **Zero-Polling Live Push (SSE):** Uses Server-Sent Events (`SseEmitter`) to push live scores to connected clients instantly without high-overhead HTTP polling.
- **Thread-Safe Architecture:** Handles live matches in memory using concurrent collections (`ConcurrentHashMap`, `CopyOnWriteArrayList`) with synchronized per-match state updates.
- **Interactive Web Dashboard:** Clean, responsive UI with real-time score updates, live/completed match cards, detailed batting and bowling cards, and live commentary.
- **Match Management:** Ability to dynamically trigger and create new matches on demand via REST endpoint.

---

## 🛠️ Tech Stack
- **Backend:** Java 17+, Spring Boot 3.2.5 (Spring Web, Task Scheduling)
- **Frontend:** HTML5, CSS3, JavaScript (Server-Sent Events / EventSource API)
- **Build Tool:** Maven

---

## 📂 Project Structure
```
cricket-live/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── cricket/
        │               ├── CricketApplication.java   # Spring Boot entry point (@EnableScheduling)
        │               ├── Match.java                # Match entity & state
        │               ├── MatchController.java      # REST & SSE endpoints
        │               ├── MatchService.java         # Simulation engine & SSE broadcast
        │               ├── Player.java               # Player stats (runs, balls, strike rate, economy)
        │               └── Team.java                 # Team roster & score tracking
        └── resources/
            ├── application.properties                # App configuration
            └── static/
                └── index.html                        # Live scoreboard web UI
```

---

## 🚦 How to Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/SWANAND2025/cricket-live.git
   cd cricket-live
   ```

2. **Run the application using Maven:**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the dashboard:**
   Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

---

## 📡 REST API Documentation

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/matches` | Get all matches with current live scores |
| `GET` | `/api/matches/{id}` | Get specific match details (scores, commentary, active batters & bowler) |
| `GET` | `/api/matches/{id}/players` | Batting & bowling stats per team for a match |
| `GET` | `/api/matches/{id}/summary` | Match summary (result, top scorer, best bowler) |
| `POST` | `/api/matches?teamA={}&teamB={}&overs={}` | Dynamically start a new simulated match |
| `GET` | `/api/stream` | Server-Sent Events (SSE) stream broadcasting real-time score updates |

---

## 💡 Architecture & Scalability
- **Current State:** Lightweight, standalone in-memory state with fast local execution and zero external database dependencies.
- **Production Scale-Out:**
  - Store match and ball states in **Redis** with distributed locks.
  - Broadcast ball-by-ball events using an event broker like **Apache Kafka** or **RabbitMQ**.
  - Deploy multiple stateless Spring Boot instances behind an Application Load Balancer.
