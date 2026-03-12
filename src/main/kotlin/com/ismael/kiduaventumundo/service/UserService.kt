package com.ismael.kiduaventumundo.service

import com.ismael.kiduaventumundo.dto.AvatarUpdateRequest
import com.ismael.kiduaventumundo.dto.LoginRequest
import com.ismael.kiduaventumundo.dto.LoginResponse
import com.ismael.kiduaventumundo.dto.PasswordUpdateRequest
import com.ismael.kiduaventumundo.errors.BadRequestException
import com.ismael.kiduaventumundo.errors.ConflictException
import com.ismael.kiduaventumundo.errors.NotFoundException
import com.ismael.kiduaventumundo.errors.UnauthorizedException
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
            created_at = "",
            updated_at = ""
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
            ?: throw UnauthorizedException("invalid credentials")

        if (user.password_hash != loginRequest.password_hash) {
            throw UnauthorizedException("invalid credentials")
        }

        return LoginResponse(success = true, user = user)
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
        return userRepository.upsertSession(sessionRow)
    }

    fun clearSession(): SessionRow = userRepository.clearSession()

    fun getEnglishLevels(): List<EnglishLevel> = userRepository.getEnglishLevels()

    fun getEnglishActivities(level: Int): List<EnglishActivity> {
        if (level <= 0) throw BadRequestException("level must be greater than 0")
        return userRepository.getEnglishActivities(level)
    }

    fun getLevelProgress(userId: Long): List<UserLevelProgress> = userRepository.getUserLevelProgress(userId)

    fun getLevelProgress(userId: Long, level: Int): UserLevelProgress {
        if (level <= 0) throw BadRequestException("level must be greater than 0")
        return userRepository.getUserLevelProgress(userId, level)
            ?: throw NotFoundException("level progress not found")
    }

    fun upsertLevelProgress(userId: Long, level: Int, body: UserLevelProgress): UserLevelProgress {
        if (userId != body.user_id || level != body.level) {
            throw BadRequestException("path and body identifiers must match")
        }
        return userRepository.upsertUserLevelProgress(body)
    }

    fun getActivityProgress(userId: Long, level: Int): List<UserActivityProgress> {
        if (level <= 0) throw BadRequestException("level must be greater than 0")
        return userRepository.getUserActivityProgress(userId, level)
    }

    fun upsertActivityProgress(
        userId: Long,
        level: Int,
        activityIndex: Int,
        body: UserActivityProgress
    ): UserActivityProgress {
        if (userId != body.user_id || level != body.level || activityIndex != body.activity_index) {
            throw BadRequestException("path and body identifiers must match")
        }
        return userRepository.upsertUserActivityProgress(body)
    }

    fun createProgressEvent(userId: Long, body: ProgressEvent): ProgressEvent {
        if (userId != body.user_id) throw BadRequestException("path and body user_id must match")
        if (body.event_type.isBlank()) throw BadRequestException("event_type is required")
        return userRepository.createProgressEvent(body)
    }

    fun getProgressEvents(userId: Long): List<ProgressEvent> = userRepository.getProgressEvents(userId)

    fun getProgressSummary(userId: Long): UserProgressSummary {
        return userRepository.getUserProgressSummary(userId)
            ?: throw NotFoundException("progress summary not found")
    }

    private fun validateRegisterInput(user: User) {
        if (user.name.isBlank()) throw BadRequestException("name must not be empty")
        if (user.nickname.isBlank()) throw BadRequestException("nickname must not be empty")
        if (user.password_hash.isBlank()) throw BadRequestException("password_hash must not be empty")
        if (user.security_answer_hash.isBlank()) throw BadRequestException("security_answer_hash must not be empty")
        if (user.avatar_id.isBlank()) throw BadRequestException("avatar_id must not be empty")
        if (user.age !in 3..120) throw BadRequestException("age must be between 3 and 120")
    }
}
