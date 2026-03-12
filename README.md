# KiduAventuMundo API (Ktor + Kotlin/JVM)

Backend nuevo e independiente, compatible con el contrato actual del frontend Android.

## Contrato compatible

- `POST /users`
- `GET /users/{nickname}`
- `POST /login`
- `GET /health`
- Puerto por defecto: `8080`
- JSON de `User` en camelCase exacto:

```json
{
  "id": 0,
  "name": "string",
  "age": 0,
  "nickname": "string",
  "passwordHash": "string",
  "avatarId": "avatar_1",
  "securityQuestion": "string",
  "securityAnswerHash": "string",
  "stars": 0
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

- Hashea en backend `passwordHash` y `securityAnswerHash` antes de guardar.
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
  "passwordHash": "$2a$10$...",
  "avatarId": "avatar_1",
  "securityQuestion": "Color favorito",
  "securityAnswerHash": "$2a$10$...",
  "stars": 0
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
  "password": "1234"
}
```

- `200 OK` si credenciales correctas.
- `401 Unauthorized` si incorrectas.

Respuesta ejemplo:

```json
{
  "success": true,
  "message": "Login successful"
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
