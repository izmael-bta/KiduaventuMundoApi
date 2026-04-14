package com.ismael.kiduaventumundo

import com.ismael.kiduaventumundo.model.*
import com.ismael.kiduaventumundo.repository.UserRepository
import com.ismael.kiduaventumundo.service.UserService
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserRoutesIntegrationTest {
    @Test
    fun registerSuccessful() = testApplication {
        application { module(userServiceOverride = UserService(InMemoryUserRepository())) }

        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "id": 0,
                  "name": "Ismael",
                  "age": 10,
                  "nickname": "kidu",
                  "password_hash": "hash123",
                  "avatar_id": "avatar_1",
                  "security_question": "pet",
                  "security_answer_hash": "ans123",
                  "stars": 5,
                  "is_active": true,
                  "created_at": "",
                  "updated_at": ""
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("\"nickname\":\"kidu\""))
    }

    @Test
    fun loginSuccessful() = testApplication {
        application { module(userServiceOverride = UserService(InMemoryUserRepository())) }

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "id": 0,
                  "name": "Isa",
                  "age": 7,
                  "nickname": "isa",
                  "password_hash": "sha256_abc",
                  "avatar_id": "avatar_1",
                  "security_question": "q",
                  "security_answer_hash": "a",
                  "stars": 0,
                  "is_active": true,
                  "created_at": "",
                  "updated_at": ""
                }
                """.trimIndent()
            )
        }

        val loginResponse = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"nickname":"isa","password_hash":"sha256_abc"}""")
        }

        assertEquals(HttpStatusCode.OK, loginResponse.status)
    }

    @Test
    fun loginInvalidCredentialsReturnsCompatibleBody() = testApplication {
        application { module(userServiceOverride = UserService(InMemoryUserRepository())) }

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "id": 0,
                  "name": "Isa",
                  "age": 7,
                  "nickname": "isa",
                  "password_hash": "sha256_abc",
                  "avatar_id": "avatar_1",
                  "security_question": "q",
                  "security_answer_hash": "a",
                  "stars": 0,
                  "is_active": true
                }
                """.trimIndent()
            )
        }

        val loginResponse = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"nickname":"isa","password_hash":"wrong"}""")
        }

        assertEquals(HttpStatusCode.OK, loginResponse.status)
        assertTrue(loginResponse.bodyAsText().contains("\"success\":false"))
    }

    @Test
    fun getUserById() = testApplication {
        application { module(userServiceOverride = UserService(InMemoryUserRepository())) }

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "id": 0,
                  "name": "Isa",
                  "age": 7,
                  "nickname": "isa",
                  "password_hash": "sha256_abc",
                  "avatar_id": "avatar_1",
                  "security_question": "q",
                  "security_answer_hash": "a",
                  "stars": 0,
                  "is_active": true,
                  "created_at": "",
                  "updated_at": ""
                }
                """.trimIndent()
            )
        }

        val response = client.get("/users/id/1")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"id\":1"))
    }

    @Test
    fun sessionAndSummaryFlowStayCompatible() = testApplication {
        application { module(userServiceOverride = UserService(InMemoryUserRepository())) }

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "id": 0,
                  "name": "Ana",
                  "age": 8,
                  "nickname": "ana",
                  "password_hash": "pw",
                  "avatar_id": "avatar_2",
                  "security_question": "q",
                  "security_answer_hash": "a",
                  "stars": 0,
                  "is_active": true
                }
                """.trimIndent()
            )
        }

        val sessionResponse = client.put("/session") {
            contentType(ContentType.Application.Json)
            setBody("""{"id":1,"user_id":1}""")
        }
        val summaryResponse = client.get("/users/1/progress/summary")

        assertEquals(HttpStatusCode.OK, sessionResponse.status)
        assertEquals(HttpStatusCode.OK, summaryResponse.status)
        assertTrue(summaryResponse.bodyAsText().contains("\"user_id\":1"))
    }
}

private class InMemoryUserRepository : UserRepository {
    private val users = mutableMapOf<Long, User>()
    private var sequence = 1L
    private var session = SessionRow(id = 1, user_id = null)
    private val summaries = mutableMapOf<Long, UserProgressSummary>()

    override fun create(user: User): User {
        val created = user.copy(
            id = sequence++,
            created_at = "2026-03-08T00:00:00Z",
            updated_at = "2026-03-08T00:00:00Z"
        )
        users[created.id] = created
        summaries[created.id] = UserProgressSummary(
            user_id = created.id,
            total_stars = 0,
            activities_completed = 0,
            current_level = 1,
            levels_unlocked = 0,
            updated_at = "2026-03-08T00:00:00Z"
        )
        return created
    }

    override fun findByNickname(nickname: String): User? = users.values.firstOrNull { it.nickname == nickname }

    override fun findById(userId: Long): User? = users[userId]

    override fun updateAvatar(userId: Long, avatarId: String): User? {
        val user = users[userId] ?: return null
        val updated = user.copy(avatar_id = avatarId, updated_at = "2026-03-08T00:01:00Z")
        users[userId] = updated
        return updated
    }

    override fun updatePassword(userId: Long, passwordHash: String): User? {
        val user = users[userId] ?: return null
        val updated = user.copy(password_hash = passwordHash, updated_at = "2026-03-08T00:01:00Z")
        users[userId] = updated
        return updated
    }

    override fun getSession(): SessionRow = session

    override fun upsertSession(sessionRow: SessionRow): SessionRow {
        session = sessionRow
        return session
    }

    override fun clearSession(): SessionRow {
        session = SessionRow(id = 1, user_id = null)
        return session
    }

    override fun getEnglishLevels(): List<EnglishLevel> = emptyList()

    override fun getEnglishActivities(level: Int): List<EnglishActivity> = emptyList()

    override fun getUserLevelProgress(userId: Long): List<UserLevelProgress> = emptyList()

    override fun getUserLevelProgress(userId: Long, level: Int): UserLevelProgress? = null

    override fun upsertUserLevelProgress(progress: UserLevelProgress): UserLevelProgress = progress

    override fun getUserActivityProgress(userId: Long, level: Int): List<UserActivityProgress> = emptyList()

    override fun upsertUserActivityProgress(progress: UserActivityProgress): UserActivityProgress = progress

    override fun createProgressEvent(progressEvent: ProgressEvent): ProgressEvent = progressEvent.copy(id = 1)

    override fun getProgressEvents(userId: Long): List<ProgressEvent> = emptyList()

    override fun getUserProgressSummary(userId: Long): UserProgressSummary? = summaries[userId]

    override fun recalculateUserProgressSummary(userId: Long): UserProgressSummary {
        return summaries[userId] ?: UserProgressSummary(
            user_id = userId,
            total_stars = 0,
            activities_completed = 0,
            current_level = 1,
            levels_unlocked = 0,
            updated_at = "2026-03-08T00:00:00Z"
        ).also { summaries[userId] = it }
    }
}
