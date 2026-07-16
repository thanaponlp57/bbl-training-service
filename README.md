# bbl-training-service

Spring Boot REST API for user management, secured with JWT (RS256) authentication.

## Tech Stack

- Java 21, Spring Boot 4.1
- Spring Security + JWT (RS256, via `jjwt`)
- Spring Data JPA + H2 in-memory database
- Lombok, MapStruct
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

### Default User

On first startup (empty database), `DataSeeder` creates an admin account for local testing:

- Username: `admin`
- Password: `admin123`

## Configuration

| Property               | Default            | Description |
|---|---|---|
| `server.port`          | `8000`              | HTTP port |
| `JWT_PRIVATE_KEY`      | *(none)*            | Base64 PKCS8 DER RSA private key. If unset, a key pair is generated in memory on startup (fine for local dev, but tokens won't survive a restart or work across multiple instances). |
| `JWT_PUBLIC_KEY`       | *(none)*            | Base64 X509 DER RSA public key, paired with `JWT_PRIVATE_KEY`. |
| `JWT_EXPIRATION_MS`    | `3600000` (1 hour)  | JWT token lifetime in milliseconds. |

Generate a persistent key pair with:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl pkcs8 -topk8 -inform PEM -outform DER -in private.pem -nocrypt | base64 | tr -d '\n'   # JWT_PRIVATE_KEY
openssl rsa -in private.pem -pubout -outform DER | base64 | tr -d '\n'                          # JWT_PUBLIC_KEY
```

## Authentication

All endpoints except `POST /auth/login` and the H2 console require a JWT bearer token.

```
POST /auth/login
Authorization: none
Body: { "username": "admin", "password": "admin123" }
```

Returns `{ "status": "success", "data": { "token": "<jwt>" } }`. Send the token on subsequent
requests as `Authorization: Bearer <jwt>`.

## API Endpoints

Base URL: `http://localhost:8000/bbl-training-service`

| Method   | Path              | Auth   | Description |
|---|---|---|---|
| `POST`   | `/auth/login`     | Public | Authenticate and receive a JWT. |
| `GET`    | `/users`          | Bearer | Retrieve a list of all users. |
| `GET`    | `/users/{userId}` | Bearer | Retrieve details of a specific user. |
| `POST`   | `/users`          | Bearer | Create a new user (username must be unique). |
| `PUT`    | `/users/{userId}` | Bearer | Update details of an existing user. |
| `DELETE` | `/users/{userId}` | Bearer | Soft-delete a specific user (row is kept, `deleted_at` is stamped). |

All responses are wrapped in a common envelope: `{ "status": "success"|"error", "message": "...", "data"|"error": ... }`.

## H2 Console

Available at `http://localhost:8000/bbl-training-service/h2-console` (local access only).

- JDBC URL: `jdbc:h2:mem:bbl_training_db`
- Username: `sa` (no password)

## Notes

- **Soft delete**: `UserEntity` uses `@SQLDelete`/`@SQLRestriction` so deletes are UPDATEs that set
  `deleted_at`, and every query automatically excludes soft-deleted rows.
- **Concurrency**: `PUT /users/{userId}` takes a pessimistic write lock on the row for the duration
  of the update transaction to prevent lost updates from concurrent requests.
- **Auditing**: `created_at`/`updated_at` and `created_by_user_id`/`updated_by_user_id` are filled in
  automatically via Spring Data JPA auditing, with the latter pair sourced from the authenticated
  user's ID on the JWT (`SecurityAuditorAware`).

## Project Structure

```
src/main/java/com/thanapon/bbl_training_service/
├── config/       # Security, JPA auditing, and data seeding configuration
├── controller/   # REST controllers
├── dto/          # Request/response DTOs
├── entity/       # JPA entities
├── exception/    # Custom exceptions + global handler
├── mapper/       # MapStruct entity/DTO mappers
├── repository/   # Spring Data JPA repositories
├── security/     # JWT service, filter, and auth entry point
├── service/      # Business logic (interface + imp)
├── validation/   # Custom bean validation (e.g. unique username)
```
