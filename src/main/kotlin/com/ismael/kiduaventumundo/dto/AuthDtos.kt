package com.ismael.kiduaventumundo.dto

import com.ismael.kiduaventumundo.model.User
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val nickname: String = "",
    val password_hash: String = ""
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val user: User? = null,
    val message: String? = null
)

@Serializable
data class AvatarUpdateRequest(
    val avatar_id: String = ""
)

@Serializable
data class PasswordUpdateRequest(
    val password_hash: String = ""
)

@Serializable
data class HealthResponse(
    val status: String = "ok"
)

@Serializable
data class MessageResponse(
    val message: String
)
