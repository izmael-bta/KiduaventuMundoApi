package com.ismael.kiduaventumundo.repository

import com.ismael.kiduaventumundo.model.*

interface UserRepository {
    fun create(user: User): User
    fun findByNickname(nickname: String): User?
    fun findById(userId: Long): User?
    fun updateAvatar(userId: Long, avatarId: String): User?
    fun updatePassword(userId: Long, passwordHash: String): User?

    fun getSession(): SessionRow
    fun upsertSession(sessionRow: SessionRow): SessionRow
    fun clearSession(): SessionRow

    fun getEnglishLevels(): List<EnglishLevel>
    fun getEnglishActivities(level: Int): List<EnglishActivity>

    fun getUserLevelProgress(userId: Long): List<UserLevelProgress>
    fun getUserLevelProgress(userId: Long, level: Int): UserLevelProgress?
    fun upsertUserLevelProgress(progress: UserLevelProgress): UserLevelProgress

    fun getUserActivityProgress(userId: Long, level: Int): List<UserActivityProgress>
    fun upsertUserActivityProgress(progress: UserActivityProgress): UserActivityProgress

    fun createProgressEvent(progressEvent: ProgressEvent): ProgressEvent
    fun getProgressEvents(userId: Long): List<ProgressEvent>

    fun getUserProgressSummary(userId: Long): UserProgressSummary?
    fun recalculateUserProgressSummary(userId: Long): UserProgressSummary
}
