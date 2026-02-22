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

## Runtime Dependencies

- PostgreSQL reachable from `QUARKUS_DATASOURCE_JDBC_URL`
- Keycloak realm endpoint reachable from `QUARKUS_OIDC_AUTH_SERVER_URL`
- MinIO reachable from `MINIO_URL`

The API will start only when required configuration values are present (especially MinIO keys and datasource settings).

## Key Environment Variables

- `QUARKUS_DATASOURCE_JDBC_URL`
- `QUARKUS_DATASOURCE_USERNAME`
- `QUARKUS_DATASOURCE_PASSWORD`
- `QUARKUS_OIDC_AUTH_SERVER_URL`
- `QUARKUS_OIDC_CLIENT_ID`
- `QUARKUS_OIDC_CREDENTIALS_SECRET`
- `MINIO_URL`
- `MINIO_ACCESS_KEY`
- `MINIO_SECRET_KEY`
- `MINIO_BUCKET`

See `.env.example` for the full template used in local and deployment scenarios.

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

Quick runtime check (container only):

```bash
docker run --rm -p 18080:8080 ghcr.io/vasilpap/tid-issuer-quarkus:latest
```

Then test from another terminal:

```bash
curl -i http://localhost:18080/q/health
```

Note: if Keycloak/DB/MinIO are not available, startup warnings or failures are expected.

## Notes

- Keep secrets only in local `.env`.
- Dev UI: `http://localhost:8080/q/dev/`.
