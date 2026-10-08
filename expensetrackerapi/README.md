# Expense Tracker API

Spring Boot backend for Expense Tracker. Start with the [project README](../README.md) for features, architecture, setup, configuration, API overview, security notes, and current verification status.

## Development

Requires JDK 25, PostgreSQL, and process environment variables `DB_PASSWORD` and `JWT_SECRET`. Optional `DB_URL` and `DB_USERNAME` override local defaults. The signing key is Base64 encoded. No backend dotenv loader or `.env.example` is supplied.

From this directory:

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
```

Use `./mvnw` on Unix-like systems. The API defaults to port 8080. The full test suite requires Docker accessible to Testcontainers (`postgres:18-alpine`); the context test also connects to the configured database. See the root README for the latest verification limitation.

## Where to look

- `src/main/resources/application.properties`: database placeholders, JWT lifetime, and development schema/logging settings.
- `src/main/java/com/expensetracker/controller`: implemented HTTP contracts.
- `dto`: validated requests and response records.
- `service`: authentication, account lifecycle, expenses, dashboard, and budgets.
- `repository` and `specification`: ownership-scoped persistence and filters.
- `config` and `security`: BCrypt, JWT encoding/decoding, stateless security, and local CORS.
- `src/test/java`: controller, service, CSV, repository, and context tests.

Development currently uses Hibernate schema updates. Production configuration, migrations, and deployment are pending; do not treat the local configuration as a production setup.
