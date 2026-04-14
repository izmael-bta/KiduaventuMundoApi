package com.ismael.kiduaventumundo.service

import com.ismael.kiduaventumundo.dto.AvatarUpdateRequest
import com.ismael.kiduaventumundo.dto.LoginRequest
import com.ismael.kiduaventumundo.dto.LoginResponse
import com.ismael.kiduaventumundo.dto.PasswordUpdateRequest
import com.ismael.kiduaventumundo.errors.BadRequestException
import com.ismael.kiduaventumundo.errors.ConflictException
import com.ismael.kiduaventumundo.errors.NotFoundException
import com.ismael.kiduaventumundo.model.*
import com.ismael.kiduaventumundo.repository.UserRepository

class UserService(private val userRepository: UserRepository) {
    fun register(rawUser: User): User {
        validateRegisterInput(rawUser)
        val normalizedNickname = rawUser.nickname.trim()

        val existing = userRepository.findByNickname(normalizedNickname)
        if (existing != null) {
            throw ConflictException("nickname already exists")
        }

        val toCreate = rawUser.copy(
            id = 0,
            nickname = normalizedNickname,
            stars = 0,
            created_at = null,
            updated_at = null
        )
        return userRepository.create(toCreate)
    }

    fun getByNickname(nicknameRaw: String): User {
        val nickname = nicknameRaw.trim()
        if (nickname.isEmpty()) throw BadRequestException("nickname is required")
        return userRepository.findByNickname(nickname) ?: throw NotFoundException("user not found")
    }

    fun getById(userId: Long): User {
        if (userId <= 0) throw BadRequestException("user_id must be greater than 0")
        return userRepository.findById(userId) ?: throw NotFoundException("user not found")
    }

    fun login(loginRequest: LoginRequest): LoginResponse {
        val nickname = loginRequest.nickname.trim()
        if (nickname.isEmpty() || loginRequest.password_hash.isBlank()) {
            throw BadRequestException("nickname and password_hash are required")
        }

        val user = userRepository.findByNickname(nickname)
            ?: return LoginResponse(success = false, user = null, message = "invalid credentials")

        if (!user.is_active) {
            return LoginResponse(success = false, user = null, message = "user is inactive")
        }

        if (user.password_hash != loginRequest.password_hash) {
            return LoginResponse(success = false, user = null, message = "invalid credentials")
        }

        return LoginResponse(success = true, user = user, message = null)
    }

    fun updateAvatar(userId: Long, request: AvatarUpdateRequest): User {
        if (request.avatar_id.isBlank()) throw BadRequestException("avatar_id is required")
        return userRepository.updateAvatar(userId, request.avatar_id)
            ?: throw NotFoundException("user not found")
    }

    fun updatePassword(userId: Long, request: PasswordUpdateRequest): User {
        if (request.password_hash.isBlank()) throw BadRequestException("password_hash is required")
        return userRepository.updatePassword(userId, request.password_hash)
            ?: throw NotFoundException("user not found")
    }

    fun getSession(): SessionRow = userRepository.getSession()

    fun updateSession(sessionRow: SessionRow): SessionRow {
        if (sessionRow.id != 1) throw BadRequestException("session id must be 1")
        sessionRow.user_id?.let {
            ensureValidUserId(it)
            userRepository.findById(it) ?: throw NotFoundException("user not found")
        }
        return userRepository.upsertSession(sessionRow)
    }

    fun clearSession(): SessionRow = userRepository.clearSession()

    fun getEnglishLevels(): List<EnglishLevel> = userRepository.getEnglishLevels()

    fun getEnglishActivities(level: Int): List<EnglishActivity> {
        if (level <= 0) throw BadRequestException("level must be greater than 0")
        return userRepository.getEnglishActivities(level)
    }

    fun getLevelProgress(userId: Long): List<UserLevelProgress> {
        ensureValidUserId(userId)
        return userRepository.getUserLevelProgress(userId)
    }

    fun getLevelProgress(userId: Long, level: Int): UserLevelProgress {
        ensureValidUserId(userId)
        if (level <= 0) throw BadRequestException("level must be greater than 0")
        return userRepository.getUserLevelProgress(userId, level)
            ?: throw NotFoundException("level progress not found")
    }

    fun upsertLevelProgress(userId: Long, level: Int, body: UserLevelProgress): UserLevelProgress {
        ensureValidUserId(userId)
        validateLevelProgress(body)
        if (userId != body.user_id || level != body.level) {
            throw BadRequestException("path and body identifiers must match")
        }
        return userRepository.upsertUserLevelProgress(body)
    }

    fun getActivityProgress(userId: Long, level: Int): List<UserActivityProgress> {
        ensureValidUserId(userId)
        if (level <= 0) throw BadRequestException("level must be greater than 0")
        return userRepository.getUserActivityProgress(userId, level)
    }

    fun upsertActivityProgress(
        userId: Long,
        level: Int,
        activityIndex: Int,
        body: UserActivityProgress
    ): UserActivityProgress {
        ensureValidUserId(userId)
        validateActivityProgress(body)
        if (userId != body.user_id || level != body.level || activityIndex != body.activity_index) {
            throw BadRequestException("path and body identifiers must match")
        }
        return userRepository.upsertUserActivityProgress(body)
    }

    fun createProgressEvent(userId: Long, body: ProgressEvent): ProgressEvent {
        ensureValidUserId(userId)
        if (userId != body.user_id) throw BadRequestException("path and body user_id must match")
        if (body.level <= 0) throw BadRequestException("level must be greater than 0")
        if (body.event_type.isBlank()) throw BadRequestException("event_type is required")
        return userRepository.createProgressEvent(body)
    }

    fun getProgressEvents(userId: Long): List<ProgressEvent> {
        ensureValidUserId(userId)
        return userRepository.getProgressEvents(userId)
    }

    fun getProgressSummary(userId: Long): UserProgressSummary {
        ensureValidUserId(userId)
        return userRepository.recalculateUserProgressSummary(userId)
    }

    private fun validateRegisterInput(user: User) {
        if (user.name.isBlank()) throw BadRequestException("name must not be empty")
        if (user.nickname.isBlank()) throw BadRequestException("nickname must not be empty")
        if (user.password_hash.isBlank()) throw BadRequestException("password_hash must not be empty")
        if (user.security_question.isBlank()) throw BadRequestException("security_question must not be empty")
        if (user.security_answer_hash.isBlank()) throw BadRequestException("security_answer_hash must not be empty")
        if (user.avatar_id.isBlank()) throw BadRequestException("avatar_id must not be empty")
        if (user.age !in 3..120) throw BadRequestException("age must be between 3 and 120")
    }

    private fun validateLevelProgress(progress: UserLevelProgress) {
        if (progress.level <= 0) throw BadRequestException("level must be greater than 0")
        if (progress.best_stars < 0) throw BadRequestException("best_stars must be greater than or equal to 0")
    }

    private fun validateActivityProgress(progress: UserActivityProgress) {
        if (progress.level <= 0) throw BadRequestException("level must be greater than 0")
        if (progress.activity_index <= 0) throw BadRequestException("activity_index must be greater than 0")
        if (progress.stars < 0) throw BadRequestException("stars must be greater than or equal to 0")
        if (progress.attempts < 0) throw BadRequestException("attempts must be greater than or equal to 0")
        if (progress.successes < 0) throw BadRequestException("successes must be greater than or equal to 0")
        if (progress.successes > progress.attempts) {
            throw BadRequestException("successes must be less than or equal to attempts")
        }
    }

    private fun ensureValidUserId(userId: Long) {
        if (userId <= 0) throw BadRequestException("user_id must be greater than 0")
    }
}
