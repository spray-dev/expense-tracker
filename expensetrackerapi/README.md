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

## Password-reset email (Brevo HTTPS)

Password-reset emails use `POST https://api.brevo.com/v3/smtp/email` through Spring `RestClient`, with the sender name `Expense Tracker` and the existing Portuguese plain-text message. The token/reset flow is unchanged. HTTP provider errors propagate with the Brevo status; connection/read failures also propagate. Outbound HTTP clients use a 3-second connection timeout and a 10-second read timeout, with redirects disabled.

Set these process environment variables for both local development and Railway:

- `BREVO_API_KEY`: a Brevo API key, not an SMTP key. Store it as a secret.
- `MAIL_FROM`: a sender email address authorized in Brevo.
- `FRONTEND_URL`: the frontend base URL, without a trailing slash (production must use the deployed HTTPS frontend). The dev profile defaults to `http://localhost:5173`.

Keep the existing production variables `SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `CORS_ALLOWED_ORIGIN`.

`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, and `MAIL_PASSWORD` are no longer used and can be removed from Railway. No SMTP connection or mail starter is needed. Never commit credentials. After deployment, request a reset for your own account and verify email delivery and the reset link; automated email tests use a mock HTTP server and do not send email.
