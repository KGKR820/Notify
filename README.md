# Notify

A real-time notification system built with **Spring Boot** and **WebSocket (STOMP)**. Users
subscribe to a "channel," and the moment someone publishes an event to that channel, every
subscriber gets the notification pushed to their browser instantly — no polling, no refresh.
Think of it as a minimal version of how YouTube notifies subscribers when a channel uploads a
new video.

## What This Project Demonstrates

- Persistent, bidirectional communication between server and browser using WebSockets
- The STOMP messaging protocol layered on top of WebSocket for pub/sub-style messaging
- Per-user message queues, so notifications are routed to the right connected client
- A simple channel subscribe/publish domain model backed by MongoDB

## Tech Stack

| Layer      | Technology                              |
|------------|------------------------------------------|
| Language   | Java 21                                   |
| Framework  | Spring Boot 4.1 (Spring Framework 7)      |
| Real-time  | Spring WebSocket + STOMP messaging        |
| Database   | MongoDB (via Spring Data MongoDB)         |
| Build tool | Maven                                     |
| Frontend   | Plain HTML/JS + SockJS + Stomp.js         |

## Project Structure

```
notify/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/kgkr/notify/
│   │   │   ├── NotifyApplication.java        # Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   └── WebSocketConfig.java      # STOMP endpoint + message broker setup
│   │   │   ├── controller/
│   │   │   │   ├── SubscriptionController.java   # POST /api/subscriptions/subscribe
│   │   │   │   └── NotificationController.java   # POST /api/notifications/publish
│   │   │   ├── dto/
│   │   │   │   ├── User.java, Channel.java, Subscription.java, Notification.java
│   │   │   │   └── SubscriptionRequest.java, ChannelEvent.java, NotificationDto.java
│   │   │   ├── repo/
│   │   │   │   ├── SubscriptionRepo.java     # MongoRepository for Subscription
│   │   │   │   └── NotificationRepository.java
│   │   │   └── service/
│   │   │       ├── SubscriptionService.java  # subscribe logic, lookup subscribers
│   │   │       ├── NotificationService.java  # sends the WebSocket push
│   │   │       └── WebSocketEventListener.java # logs connect/disconnect events
│   │   └── resources/
│   │       ├── application.properties        # server + MongoDB config
│   │       └── static/index.html             # demo frontend
│   └── test/java/com/kgkr/notify/
│       └── NotifyApplicationTests.java
```

## How It Works

1. **Connect** — The browser opens a WebSocket connection (via SockJS, with a STOMP fallback)
   to `ws://localhost:8080/ws`, identifying itself with a `userId`.
2. **Subscribe** — A client calls `POST /api/subscriptions/subscribe` with a `userId` and
   `channelId`. This is saved as a `Subscription` document in MongoDB — it just records "this
   user cares about this channel."
3. **Publish** — Some event source (in this demo, a manual `curl` call) hits
   `POST /api/notifications/publish` with a `channelId` and event details (e.g. a new video
   title).
4. **Fan-out** — `NotificationService` looks up every user subscribed to that channel and sends
   each one a message on their personal STOMP queue: `/user/{userId}/queue/notifications`.
5. **Push** — Any browser currently connected as that `userId` receives the message instantly
   over its open WebSocket connection and renders it — no refresh needed.

```
Publisher --> POST /api/notifications/publish --> NotificationService
                                                        │
                                     looks up subscribers of channelId
                                                        │
                                          for each subscriber's userId
                                                        ▼
                              STOMP message --> /user/{userId}/queue/notifications
                                                        │
                                                        ▼
                                        Connected browser tab (WebSocket)
```

## Prerequisites

- JDK 21+
- Maven (or use the bundled `./mvnw` wrapper — no local Maven install needed)
- Docker (for running MongoDB — see setup below)

## Setup

### 1. Run MongoDB with Docker

```bash
docker run -d \
  --name notify-mongo \
  -p 27017:27017 \
  -v notify-mongo-data:/data/db \
  mongo:latest
```

This starts MongoDB on `localhost:27017` with a persistent named volume, matching the
connection settings already in `application.properties`. No extra configuration needed.

Confirm it's running:
```bash
docker ps
```

To stop/start it later:
```bash
docker stop notify-mongo
docker start notify-mongo
```

### 2. Build the project

```bash
./mvnw clean package
```

## Run

```bash
./mvnw spring-boot:run
```

or run the packaged jar directly:

```bash
java -jar target/notify-0.0.1-SNAPSHOT.jar
```

The app starts on `http://localhost:8080`.

## Example Walkthrough

**1. Open the demo frontend in two browser tabs:** `http://localhost:8080/index.html`

- Tab 1 → enter User ID `1` → click **Connect**
- Tab 2 → enter User ID `2` → click **Connect**

**2. Subscribe both users to channel `100`:**

```bash
curl -X POST http://localhost:8080/api/subscriptions/subscribe \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "channelId": 100}'

curl -X POST http://localhost:8080/api/subscriptions/subscribe \
  -H "Content-Type: application/json" \
  -d '{"userId": 2, "channelId": 100}'
```

Expected response for each:
```
User 1 subscribed to channel 100
```

**3. Publish a new event to channel `100`:**

```bash
curl -X POST http://localhost:8080/api/notifications/publish \
  -H "Content-Type: application/json" \
  -d '{"channelId": 100, "videoId": "abc123", "videoTitle": "New Upload!"}'
```

Expected response:
```
Notification sent to 2 subscribers.
```

**4. Check both browser tabs** — each should instantly show:

```
[Channel 100] New video: New Upload!
```

That confirms the full flow: subscribe → publish → real-time fan-out to every connected
subscriber, with no page refresh.

## Notes

This project is for personal learning / portfolio use.
