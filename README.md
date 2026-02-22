# TID Issuer API (Quarkus)

Backend service for the TID Issuer workflow. It manages company registration, employee approval/denial, and article document upload/download.

## Stack

- Java 21, Quarkus 3
- PostgreSQL (persistence)
- Keycloak OIDC (authentication and role-based access)
- MinIO (article document storage)

## Main API Areas

- `Representative` role: `/api/registration`
- `Employee` role: `/api/processing`
- Health and docs: `/q/health`, `/q/openapi`

Roles are client roles on `quarkus-api` (`Representative`, `Employee`) and are read from `resource_access.quarkus-api.roles`.

## Local Development

1. Copy `.env.example` to `.env` and set values.
2. Start infra services (`tid-issuer-infra`).
3. Run the API in dev mode:

```bash
./mvnw compile quarkus:dev
```

## Common Commands

```bash
./mvnw test
./mvnw verify
./mvnw package
```

## Notes

- Keep real secrets out of git; only templates are committed.
- Dev UI is available in dev mode at `http://localhost:8080/q/dev/`.
