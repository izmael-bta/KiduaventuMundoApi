package com.ismael.kiduaventumundo.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long = 0,
    val name: String = "",
    val age: Int = 0,
    val nickname: String = "",
    val password_hash: String = "",
    val avatar_id: String = "avatar_1",
    val security_question: String = "",
    val security_answer_hash: String = "",
    val stars: Int = 0,
    val is_active: Boolean = true,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class SessionRow(
    val id: Int = 1,
    val user_id: Long? = null
)

@Serializable
data class EnglishLevel(
    val level: Int,
    val title: String,
    val description: String,
    val pass_stars: Int,
    val display_order: Int,
    val is_active: Boolean
)

@Serializable
data class EnglishActivity(
    val id: Long,
    val level: Int,
    val activity_index: Int,
    val activity_key: String,
    val title: String,
    val is_active: Boolean
)

@Serializable
data class UserLevelProgress(
    val user_id: Long,
    val level: Int,
    val is_unlocked: Boolean,
    val is_completed: Boolean,
    val best_stars: Int,
    val first_completed_at: String? = null,
    val last_played_at: String? = null
)

@Serializable
data class UserActivityProgress(
    val user_id: Long,
    val level: Int,
    val activity_index: Int,
    val stars: Int,
    val attempts: Int,
    val successes: Int,
    val last_result: Boolean? = null,
    val last_played_at: String? = null
)

@Serializable
data class ProgressEvent(
    val id: Long = 0,
    val user_id: Long = 0,
    val level: Int,
    val activity_index: Int? = null,
    val event_type: String,
    val stars_delta: Int = 0,
    val payload_json: String? = null,
    val created_at: String? = null
)

@Serializable
data class UserProgressSummary(
    val user_id: Long,
    val total_stars: Int,
    val activities_completed: Int,
    val current_level: Int,
    val levels_unlocked: Int,
    val updated_at: String? = null
)
