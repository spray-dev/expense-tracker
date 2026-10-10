# Expense Tracker API

Spring Boot backend for Expense Tracker. See the [project README](../README.md) for features, API contracts, and full setup.

## Development

Requires JDK 25, PostgreSQL, and process environment variables `DB_PASSWORD` and `JWT_SECRET` (a Base64-encoded signing key of at least 32 random bytes). No backend dotenv loader is supplied; keep secrets outside the repository.

From this directory:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
.\mvnw.cmd test "-Dspring.profiles.active=dev"
```

Use `./mvnw` on Unix-like systems. The API defaults to port 8080. The dev profile enables SQL logging and defaults `DB_URL` to `jdbc:postgresql://localhost:5432/expensetracker`, `DB_USERNAME` to `postgres`, and `CORS_ALLOWED_ORIGIN` to `http://localhost:5173`. All three can be overridden in the process environment.

The full test suite requires Docker accessible to Testcontainers (`postgres:18-alpine`); the context test also connects to the configured database and runs Flyway. Always use a development/test database.

## Production and shared configuration

Set `SPRING_PROFILES_ACTIVE=prod` and provide `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `CORS_ALLOWED_ORIGIN`. Build with `.\mvnw.cmd package`, then run `java -jar target/expensetrackerapi-0.0.1-SNAPSHOT.jar`. Hosting-specific TLS/database connection options and secret injection remain deployment decisions.

- `application.properties`: shared required environment values, Hibernate `validate`, SQL logging off, and unchanged JWT lifetime (`604800000` ms / 7 days).
- `application-dev.properties`: local URL, username, and CORS fallbacks; SQL logging on.
- `application-prod.properties`: SQL logging explicitly off; required values inherited without local fallbacks.

With no active profile, shared settings apply and explicit environment values are required. Select exactly one of dev/prod; do not combine them. Missing required values prevent successful startup. Flyway remains enabled and owns schema changes through `src/main/resources/db/migration`; Hibernate validates after migration.

`SecurityConfig` reads `app.cors.allowed-origin`, backed by `CORS_ALLOWED_ORIGIN`. Supply one exact frontend origin such as `https://expenses.example.com`, without a path or trailing slash. Allowed HTTP methods, headers, and authentication rules are unchanged.
