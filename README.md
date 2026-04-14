# KiduAventuMundo API (Ktor + Kotlin/JVM)

Documentacion de entrega disponible en [docs/DOCUMENTACION_ENTREGA.md](C:\Users\ismae\IdeaProjects\KiduaventuMundoApi\docs\DOCUMENTACION_ENTREGA.md).

Backend nuevo e independiente, compatible con el contrato actual del frontend Android.

## Contrato compatible

- `POST /users`
- `GET /users/{nickname}`
- `GET /users/id/{userId}`
- `PUT /users/{userId}/avatar`
- `PUT /users/{userId}/password`
- `POST /login`
- `GET /session`
- `PUT /session`
- `DELETE /session`
- `GET /users/{userId}/progress/levels`
- `GET /users/{userId}/progress/levels/{level}`
- `PUT /users/{userId}/progress/levels/{level}`
- `GET /users/{userId}/progress/levels/{level}/activities`
- `PUT /users/{userId}/progress/levels/{level}/activities/{activityIndex}`
- `POST /users/{userId}/progress/events`
- `GET /users/{userId}/progress/events`
- `GET /users/{userId}/progress/summary`
- `GET /health`
- Puerto por defecto: `8080`
- JSON de `User` en snake_case exacto:

```json
{
  "id": 0,
  "name": "string",
  "age": 0,
  "nickname": "string",
  "password_hash": "string",
  "avatar_id": "avatar_1",
  "security_question": "string",
  "security_answer_hash": "string",
  "stars": 0,
  "is_active": true,
  "created_at": "2026-04-14T00:00:00Z",
  "updated_at": "2026-04-14T00:00:00Z"
}
```

## Requisitos

- JDK 21
- IntelliJ IDEA
- SQL Server disponible (local o remoto)

## Estructura del proyecto

```text
src/
  main/
    kotlin/com/ismael/kiduaventumundo/
      Main.kt
      config/DatabaseSettings.kt
      db/DatabaseFactory.kt
      db/UsersTable.kt
      dto/AuthDtos.kt
      errors/AppExceptions.kt
      model/User.kt
      plugins/HTTP.kt
      plugins/Monitoring.kt
      plugins/Serialization.kt
      plugins/StatusPages.kt
      repository/UserRepository.kt
      repository/ExposedUserRepository.kt
      routes/Routes.kt
      service/UserService.kt
    resources/application.conf
  test/
    kotlin/com/ismael/kiduaventumundo/UserRoutesIntegrationTest.kt
http/
  health.http
  users.http
  login.http
```

## Configuración SQL Server

El backend lee configuración desde variables de entorno y/o `application.conf`.

Variables soportadas:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `DB_ENCRYPT`
- `DB_TRUST_SERVER_CERTIFICATE`

Valores iniciales de ejemplo están en `src/main/resources/application.conf` como placeholders para que los reemplaces manualmente.

## Ejecutar en IntelliJ (Run)

1. Abre el proyecto.
2. Abre `Main.kt` en `src/main/kotlin/com/ismael/kiduaventumundo/Main.kt`.
3. Ejecuta la función `main()` con el botón Run.
4. El servidor inicia en `http://localhost:8080`.

## Ejecutar por Gradle (opcional)

```bash
./gradlew run
```

En Windows PowerShell:

```powershell
.\gradlew.bat run
```

## Endpoints y respuestas

### `POST /users`

- `201 Created` en éxito.
- `409 Conflict` si `nickname` ya existe.
- `400 Bad Request` si payload inválido.

Ejemplo respuesta éxito:

```json
{
  "id": 1,
  "name": "Ismael",
  "age": 9,
  "nickname": "kidu123",
  "password_hash": "sha256_hash",
  "avatar_id": "avatar_1",
  "security_question": "Color favorito",
  "security_answer_hash": "sha256_hash",
  "stars": 0,
  "is_active": true,
  "created_at": "2026-04-14T00:00:00Z",
  "updated_at": "2026-04-14T00:00:00Z"
}
```

### `GET /users/{nickname}`

- `200 OK` + `User` si existe.
- `404 Not Found` si no existe.

### `POST /login`

Request:

```json
{
  "nickname": "kidu123",
  "password_hash": "1234"
}
```

- `200 OK` para credenciales válidas o inválidas; la respuesta indica `success`.

Respuesta ejemplo:

```json
{
  "success": true,
  "user": {
    "id": 1,
    "name": "Ismael",
    "age": 9,
    "nickname": "kidu123",
    "password_hash": "1234",
    "avatar_id": "avatar_1",
    "security_question": "Color favorito",
    "security_answer_hash": "azul",
    "stars": 0,
    "is_active": true,
    "created_at": "2026-04-14T00:00:00Z",
    "updated_at": "2026-04-14T00:00:00Z"
  },
  "message": null
}
```

### `GET /health`

- `200 OK`

```json
{
  "status": "ok"
}
```

## Formato de error JSON

```json
{
  "code": "BAD_REQUEST",
  "message": "nickname must not be empty",
  "details": "... opcional ..."
}
```

## SQL Server: tabla esperada

La app crea/verifica tabla `users` automáticamente con Exposed (incluyendo índice único en `nickname`).

Referencia de columnas:

- `id BIGINT IDENTITY PRIMARY KEY`
- `name NVARCHAR(120) NOT NULL`
- `age INT NOT NULL`
- `nickname NVARCHAR(80) NOT NULL UNIQUE`
- `password_hash NVARCHAR(255) NOT NULL`
- `avatar_id NVARCHAR(80) NOT NULL`
- `security_question NVARCHAR(255) NOT NULL`
- `security_answer_hash NVARCHAR(255) NOT NULL`
- `stars INT NOT NULL DEFAULT 0`
- `created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()`

## Tests de integración

Cubiertos con Ktor test engine:

- registro exitoso
- registro duplicado
- login exitoso
- login fallido
- get user existente
- get user inexistente

Ejecutar tests:

```powershell
.\gradlew.bat test
```

## Troubleshooting SQL Server

- Error de login SQL: revisa `DB_USER` y `DB_PASSWORD`.
- Error de conexión: revisa `DB_HOST`, `DB_PORT` y firewall.
- Certificado TLS: ajusta `DB_ENCRYPT` y `DB_TRUST_SERVER_CERTIFICATE` según tu entorno.
- Si usas instancia local y falla hostname, prueba `DB_HOST=localhost`.
