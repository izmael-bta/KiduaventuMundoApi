package com.ismael.kiduaventumundo.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UsersTable : Table("users") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 120)
    val age = integer("age")
    val nickname = varchar("nickname", 80).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val avatarId = varchar("avatar_id", 64)
    val securityQuestion = varchar("security_question", 255)
    val securityAnswerHash = varchar("security_answer_hash", 255)
    val stars = integer("stars")
    val isActive = bool("is_active")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object SessionTable : Table("session") {
    val id = byte("id")
    val userId = long("user_id").nullable()

    override val primaryKey = PrimaryKey(id)
}

object EnglishLevelsTable : Table("english_levels") {
    val level = integer("level")
    val title = varchar("title", 120)
    val description = varchar("description", 255)
    val passStars = integer("pass_stars")
    val displayOrder = integer("display_order")
    val isActive = bool("is_active")

    override val primaryKey = PrimaryKey(level)
}

object EnglishActivitiesTable : Table("english_activities") {
    val id = long("id").autoIncrement()
    val level = integer("level")
    val activityIndex = integer("activity_index")
    val activityKey = varchar("activity_key", 100)
    val title = varchar("title", 150)
    val isActive = bool("is_active")

    override val primaryKey = PrimaryKey(id)
}

object EnglishLevelProgressTable : Table("english_level_progress") {
    val userId = long("user_id")
    val level = integer("level")
    val isUnlocked = bool("is_unlocked")
    val isCompleted = bool("is_completed")
    val bestStars = integer("best_stars")
    val firstCompletedAt = datetime("first_completed_at").nullable()
    val lastPlayedAt = datetime("last_played_at").nullable()

    override val primaryKey = PrimaryKey(userId, level)
}

object EnglishActivityProgressTable : Table("english_activity_progress") {
    val userId = long("user_id")
    val level = integer("level")
    val activityIndex = integer("activity_index")
    val stars = integer("stars")
    val attempts = integer("attempts")
    val successes = integer("successes")
    val lastResult = bool("last_result").nullable()
    val lastPlayedAt = datetime("last_played_at").nullable()

    override val primaryKey = PrimaryKey(userId, level, activityIndex)
}

object ProgressEventsTable : Table("progress_events") {
    val id = long("id").autoIncrement()
    val userId = long("user_id")
    val level = integer("level")
    val activityIndex = integer("activity_index").nullable()
    val eventType = varchar("event_type", 50)
    val starsDelta = integer("stars_delta")
    val payloadJson = text("payload_json").nullable()
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

object UserProgressSummaryTable : Table("user_progress_summary") {
    val userId = long("user_id")
    val totalStars = integer("total_stars")
    val activitiesCompleted = integer("activities_completed")
    val currentLevel = integer("current_level")
    val levelsUnlocked = integer("levels_unlocked")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(userId)
}
