package com.ismael.kiduaventumundo.repository

import com.ismael.kiduaventumundo.db.*
import com.ismael.kiduaventumundo.model.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class ExposedUserRepository : UserRepository {
    override fun create(user: User): User = dbQuery {
        val now = nowUtc()
        val inserted = UsersTable.insert {
            it[name] = user.name
            it[age] = user.age
            it[nickname] = user.nickname
            it[passwordHash] = user.password_hash
            it[avatarId] = user.avatar_id
            it[securityQuestion] = user.security_question
            it[securityAnswerHash] = user.security_answer_hash
            it[stars] = user.stars
            it[isActive] = user.is_active
            it[createdAt] = now
            it[updatedAt] = now
        }

        val idValue = inserted[UsersTable.id]
        findById(idValue)!!
    }

    override fun findByNickname(nickname: String): User? = dbQuery {
        UsersTable
            .selectAll()
            .where { UsersTable.nickname eq nickname }
            .limit(1)
            .map(::toUser)
            .singleOrNull()
    }

    override fun findById(userId: Long): User? = dbQuery {
        UsersTable
            .selectAll()
            .where { UsersTable.id eq userId }
            .limit(1)
            .map(::toUser)
            .singleOrNull()
    }

    override fun updateAvatar(userId: Long, avatarId: String): User? = dbQuery {
        val now = nowUtc()
        val affected = UsersTable.update({ UsersTable.id eq userId }) {
            it[UsersTable.avatarId] = avatarId
            it[updatedAt] = now
        }
        if (affected == 0) null else findById(userId)
    }

    override fun updatePassword(userId: Long, passwordHash: String): User? = dbQuery {
        val now = nowUtc()
        val affected = UsersTable.update({ UsersTable.id eq userId }) {
            it[UsersTable.passwordHash] = passwordHash
            it[updatedAt] = now
        }
        if (affected == 0) null else findById(userId)
    }

    override fun getSession(): SessionRow = dbQuery {
        SessionTable
            .selectAll()
            .where { SessionTable.id eq 1 }
            .limit(1)
            .map { SessionRow(id = it[SessionTable.id].toInt(), user_id = it[SessionTable.userId]) }
            .singleOrNull() ?: SessionRow(id = 1, user_id = null)
    }

    override fun upsertSession(sessionRow: SessionRow): SessionRow = dbQuery {
        val existing = SessionTable
            .selectAll()
            .where { SessionTable.id eq sessionRow.id.toByte() }
            .limit(1)
            .singleOrNull()

        if (existing == null) {
            SessionTable.insert {
                it[id] = sessionRow.id.toByte()
                it[userId] = sessionRow.user_id
            }
        } else {
            SessionTable.update({ SessionTable.id eq sessionRow.id.toByte() }) {
                it[userId] = sessionRow.user_id
            }
        }

        SessionRow(id = sessionRow.id, user_id = sessionRow.user_id)
    }

    override fun clearSession(): SessionRow = dbQuery {
        val existing = SessionTable
            .selectAll()
            .where { SessionTable.id eq 1 }
            .limit(1)
            .singleOrNull()

        if (existing == null) {
            SessionTable.insert {
                it[id] = 1
                it[userId] = null
            }
        } else {
            SessionTable.update({ SessionTable.id eq 1 }) {
                it[userId] = null
            }
        }

        SessionRow(id = 1, user_id = null)
    }

    override fun getEnglishLevels(): List<EnglishLevel> = dbQuery {
        EnglishLevelsTable
            .selectAll()
            .orderBy(EnglishLevelsTable.displayOrder to SortOrder.ASC)
            .map(::toEnglishLevel)
    }

    override fun getEnglishActivities(level: Int): List<EnglishActivity> = dbQuery {
        EnglishActivitiesTable
            .selectAll()
            .where { EnglishActivitiesTable.level eq level }
            .orderBy(EnglishActivitiesTable.activityIndex to SortOrder.ASC)
            .map(::toEnglishActivity)
    }

    override fun getUserLevelProgress(userId: Long): List<UserLevelProgress> = dbQuery {
        EnglishLevelProgressTable
            .selectAll()
            .where { EnglishLevelProgressTable.userId eq userId }
            .orderBy(EnglishLevelProgressTable.level to SortOrder.ASC)
            .map(::toUserLevelProgress)
    }

    override fun getUserLevelProgress(userId: Long, level: Int): UserLevelProgress? = dbQuery {
        EnglishLevelProgressTable
            .selectAll()
            .where { (EnglishLevelProgressTable.userId eq userId) and (EnglishLevelProgressTable.level eq level) }
            .limit(1)
            .map(::toUserLevelProgress)
            .singleOrNull()
    }

    override fun upsertUserLevelProgress(progress: UserLevelProgress): UserLevelProgress = dbQuery {
        val firstCompleted = parseNullableIsoUtc(progress.first_completed_at)
        val lastPlayed = parseNullableIsoUtc(progress.last_played_at)

        val exists = EnglishLevelProgressTable
            .selectAll()
            .where { (EnglishLevelProgressTable.userId eq progress.user_id) and (EnglishLevelProgressTable.level eq progress.level) }
            .limit(1)
            .singleOrNull()

        if (exists == null) {
            EnglishLevelProgressTable.insert {
                it[userId] = progress.user_id
                it[level] = progress.level
                it[isUnlocked] = progress.is_unlocked
                it[isCompleted] = progress.is_completed
                it[bestStars] = progress.best_stars
                it[firstCompletedAt] = firstCompleted
                it[lastPlayedAt] = lastPlayed
            }
        } else {
            EnglishLevelProgressTable.update({
                (EnglishLevelProgressTable.userId eq progress.user_id) and
                    (EnglishLevelProgressTable.level eq progress.level)
            }) {
                it[isUnlocked] = progress.is_unlocked
                it[isCompleted] = progress.is_completed
                it[bestStars] = progress.best_stars
                it[firstCompletedAt] = firstCompleted
                it[lastPlayedAt] = lastPlayed
            }
        }

        getUserLevelProgress(progress.user_id, progress.level)!!
    }

    override fun getUserActivityProgress(userId: Long, level: Int): List<UserActivityProgress> = dbQuery {
        EnglishActivityProgressTable
            .selectAll()
            .where { (EnglishActivityProgressTable.userId eq userId) and (EnglishActivityProgressTable.level eq level) }
            .orderBy(EnglishActivityProgressTable.activityIndex to SortOrder.ASC)
            .map(::toUserActivityProgress)
    }

    override fun upsertUserActivityProgress(progress: UserActivityProgress): UserActivityProgress = dbQuery {
        val lastPlayed = parseNullableIsoUtc(progress.last_played_at)

        val exists = EnglishActivityProgressTable
            .selectAll()
            .where {
                (EnglishActivityProgressTable.userId eq progress.user_id) and
                    (EnglishActivityProgressTable.level eq progress.level) and
                    (EnglishActivityProgressTable.activityIndex eq progress.activity_index)
            }
            .limit(1)
            .singleOrNull()

        if (exists == null) {
            EnglishActivityProgressTable.insert {
                it[userId] = progress.user_id
                it[level] = progress.level
                it[activityIndex] = progress.activity_index
                it[stars] = progress.stars
                it[attempts] = progress.attempts
                it[successes] = progress.successes
                it[lastResult] = progress.last_result
                it[lastPlayedAt] = lastPlayed
            }
        } else {
            EnglishActivityProgressTable.update({
                (EnglishActivityProgressTable.userId eq progress.user_id) and
                    (EnglishActivityProgressTable.level eq progress.level) and
                    (EnglishActivityProgressTable.activityIndex eq progress.activity_index)
            }) {
                it[stars] = progress.stars
                it[attempts] = progress.attempts
                it[successes] = progress.successes
                it[lastResult] = progress.last_result
                it[lastPlayedAt] = lastPlayed
            }
        }

        EnglishActivityProgressTable
            .selectAll()
            .where {
                (EnglishActivityProgressTable.userId eq progress.user_id) and
                    (EnglishActivityProgressTable.level eq progress.level) and
                    (EnglishActivityProgressTable.activityIndex eq progress.activity_index)
            }
            .limit(1)
            .map(::toUserActivityProgress)
            .single()
    }

    override fun createProgressEvent(progressEvent: ProgressEvent): ProgressEvent = dbQuery {
        val now = nowUtc()
        val inserted = ProgressEventsTable.insert {
            it[userId] = progressEvent.user_id
            it[level] = progressEvent.level
            it[activityIndex] = progressEvent.activity_index
            it[eventType] = progressEvent.event_type
            it[starsDelta] = progressEvent.stars_delta
            it[payloadJson] = progressEvent.payload_json
            it[createdAt] = now
        }

        val eventId = inserted[ProgressEventsTable.id]
        ProgressEventsTable
            .selectAll()
            .where { ProgressEventsTable.id eq eventId }
            .limit(1)
            .map(::toProgressEvent)
            .single()
    }

    override fun getProgressEvents(userId: Long): List<ProgressEvent> = dbQuery {
        ProgressEventsTable
            .selectAll()
            .where { ProgressEventsTable.userId eq userId }
            .orderBy(ProgressEventsTable.createdAt to SortOrder.DESC)
            .map(::toProgressEvent)
    }

    override fun getUserProgressSummary(userId: Long): UserProgressSummary? = dbQuery {
        UserProgressSummaryTable
            .selectAll()
            .where { UserProgressSummaryTable.userId eq userId }
            .limit(1)
            .map(::toUserProgressSummary)
            .singleOrNull()
    }

    private fun toUser(row: ResultRow): User = User(
        id = row[UsersTable.id],
        name = row[UsersTable.name],
        age = row[UsersTable.age],
        nickname = row[UsersTable.nickname],
        password_hash = row[UsersTable.passwordHash],
        avatar_id = row[UsersTable.avatarId],
        security_question = row[UsersTable.securityQuestion],
        security_answer_hash = row[UsersTable.securityAnswerHash],
        stars = row[UsersTable.stars],
        is_active = row[UsersTable.isActive],
        created_at = row[UsersTable.createdAt].toIsoUtcString(),
        updated_at = row[UsersTable.updatedAt].toIsoUtcString()
    )

    private fun toEnglishLevel(row: ResultRow): EnglishLevel = EnglishLevel(
        level = row[EnglishLevelsTable.level],
        title = row[EnglishLevelsTable.title],
        description = row[EnglishLevelsTable.description],
        pass_stars = row[EnglishLevelsTable.passStars],
        display_order = row[EnglishLevelsTable.displayOrder],
        is_active = row[EnglishLevelsTable.isActive]
    )

    private fun toEnglishActivity(row: ResultRow): EnglishActivity = EnglishActivity(
        id = row[EnglishActivitiesTable.id],
        level = row[EnglishActivitiesTable.level],
        activity_index = row[EnglishActivitiesTable.activityIndex],
        activity_key = row[EnglishActivitiesTable.activityKey],
        title = row[EnglishActivitiesTable.title],
        is_active = row[EnglishActivitiesTable.isActive]
    )

    private fun toUserLevelProgress(row: ResultRow): UserLevelProgress = UserLevelProgress(
        user_id = row[EnglishLevelProgressTable.userId],
        level = row[EnglishLevelProgressTable.level],
        is_unlocked = row[EnglishLevelProgressTable.isUnlocked],
        is_completed = row[EnglishLevelProgressTable.isCompleted],
        best_stars = row[EnglishLevelProgressTable.bestStars],
        first_completed_at = row[EnglishLevelProgressTable.firstCompletedAt]?.toIsoUtcString(),
        last_played_at = row[EnglishLevelProgressTable.lastPlayedAt]?.toIsoUtcString()
    )

    private fun toUserActivityProgress(row: ResultRow): UserActivityProgress = UserActivityProgress(
        user_id = row[EnglishActivityProgressTable.userId],
        level = row[EnglishActivityProgressTable.level],
        activity_index = row[EnglishActivityProgressTable.activityIndex],
        stars = row[EnglishActivityProgressTable.stars],
        attempts = row[EnglishActivityProgressTable.attempts],
        successes = row[EnglishActivityProgressTable.successes],
        last_result = row[EnglishActivityProgressTable.lastResult],
        last_played_at = row[EnglishActivityProgressTable.lastPlayedAt]?.toIsoUtcString()
    )

    private fun toProgressEvent(row: ResultRow): ProgressEvent = ProgressEvent(
        id = row[ProgressEventsTable.id],
        user_id = row[ProgressEventsTable.userId],
        level = row[ProgressEventsTable.level],
        activity_index = row[ProgressEventsTable.activityIndex],
        event_type = row[ProgressEventsTable.eventType],
        stars_delta = row[ProgressEventsTable.starsDelta],
        payload_json = row[ProgressEventsTable.payloadJson],
        created_at = row[ProgressEventsTable.createdAt].toIsoUtcString()
    )

    private fun toUserProgressSummary(row: ResultRow): UserProgressSummary = UserProgressSummary(
        user_id = row[UserProgressSummaryTable.userId],
        total_stars = row[UserProgressSummaryTable.totalStars],
        activities_completed = row[UserProgressSummaryTable.activitiesCompleted],
        current_level = row[UserProgressSummaryTable.currentLevel],
        levels_unlocked = row[UserProgressSummaryTable.levelsUnlocked],
        updated_at = row[UserProgressSummaryTable.updatedAt].toIsoUtcString()
    )

    private fun parseNullableIsoUtc(value: String?): LocalDateTime? {
        if (value.isNullOrBlank()) return null
        val instant = Instant.parse(value)
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
    }
}
