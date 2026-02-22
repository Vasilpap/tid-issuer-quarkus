# TID Issuer API (Quarkus)

Backend service for registration and approval workflows.

## Stack

- Java 21
- Quarkus 3
- PostgreSQL
- Keycloak OIDC
- MinIO

## API Areas

- `/api/registration` (Representative flow)
- `/api/processing` (Employee flow)
- `/q/health`, `/q/openapi`

Client roles are read from `resource_access.quarkus-api.roles`:

- `Representative`
- `Employee`

## Local Run

1. Copy env file:

```bash
cp .env.example .env
```

2. Start infra (`tid-issuer-infra`).

3. Run API in dev mode:

```bash
./mvnw compile quarkus:dev
```

## Build and Test

```bash
./mvnw test
./mvnw verify
./mvnw package
```

## Build Docker Image

```bash
./mvnw clean -DskipTests package
docker build -f src/main/docker/Dockerfile.jvm -t ghcr.io/vasilpap/tid-issuer-quarkus:latest .
docker push ghcr.io/vasilpap/tid-issuer-quarkus:latest
```

## Notes

- Keep secrets only in local `.env`.
- Dev UI: `http://localhost:8080/q/dev/`.
