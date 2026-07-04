# bbl-training-service

Spring Boot REST API.

## Tech Stack

- Java 21, Spring Boot 4.1
- H2 in-memory database
- Maven, Docker

## Getting Started

### Run with Maven

```bash
./mvnw spring-boot:run
```

### Run with Docker

```bash
docker compose up --build
```

The service starts at `http://localhost:8000/bbl-training-service`.

### Run Tests

```bash
./mvnw test
```

## Configuration

| Property | Default | Description |
|---|---|---|
| `server.port` | `8000` | HTTP port |

## API Endpoints

Base URL: `http://localhost:8000/bbl-training-service`

| Method   | Path             | Auth   | Description |
|---|---|---|---|
| `GET`    | `/users`         | Public | Retrieve a list of all users. |
| `GET`    | `/users/{userId}`| Public | Retrieve details of a specific user. |
| `POST`   | `/users`         | Public | Create a new user. |
| `PUT`    | `/users/{userId}`| Public | Update details of an existing user. |
| `DELETE` | `/users/{userId}`| Public | Delete a specific user. |

## H2 Console

Available at `http://localhost:8000/bbl-training-service/h2-console` (local access only).

- JDBC URL: `jdbc:h2:mem:bbl_training_db`
- Username: `sa` (no password)

## Project Structure

```
src/main/java/com/thanapon/bbl_training_service/
├── controller/   # REST controllers
├── dto/          # Request/response DTOs
├── entity/       # JPA entities (composite key via @EmbeddedId)
├── exception/    # Custom exceptions + global handler
├── repository/   # Spring Data JPA repositories
├── service/      # Business logic (interface + imp)
```
