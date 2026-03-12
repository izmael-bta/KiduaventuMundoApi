package com.ismael.kiduaventumundo.config

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.ApplicationConfig

data class DatabaseSettings(
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String,
    val encrypt: Boolean,
    val trustServerCertificate: Boolean
) {
    companion object {
        fun from(config: ApplicationConfig): DatabaseSettings {
            return DatabaseSettings(
                host = envOrConfig("DB_HOST", config, "app.db.host", "localhost"),
                port = envOrConfig("DB_PORT", config, "app.db.port", "1433").toInt(),
                database = envOrConfig("DB_NAME", config, "app.db.name", "kidu_aventumundo"),
                user = envOrConfig("DB_USER", config, "app.db.user", "sa"),
                password = envOrConfig("DB_PASSWORD", config, "app.db.password", "", trimConfig = false, trimEnv = false),
                encrypt = envOrConfig("DB_ENCRYPT", config, "app.db.encrypt", "true").toBooleanStrictOrNull() ?: true,
                trustServerCertificate = envOrConfig(
                    "DB_TRUST_SERVER_CERTIFICATE",
                    config,
                    "app.db.trustServerCertificate",
                    "true"
                ).toBooleanStrictOrNull() ?: true
            )
        }

        private fun envOrConfig(
            envKey: String,
            config: ApplicationConfig,
            configPath: String,
            default: String,
            trimConfig: Boolean = true,
            trimEnv: Boolean = true
        ): String {
            val configValue = readConfigValue(config, configPath, trimConfig)
            if (!configValue.isNullOrEmpty()) return configValue

            val env = System.getenv(envKey)?.let { if (trimEnv) it.trim() else it }
            if (!env.isNullOrEmpty()) return env

            return default
        }

        private fun readConfigValue(config: ApplicationConfig, configPath: String, trim: Boolean): String? {
            val fromKtorConfig = runCatching { config.property(configPath).getString() }.getOrNull()
            if (fromKtorConfig != null) return if (trim) fromKtorConfig.trim() else fromKtorConfig

            val fromLoadedConfig = runCatching {
                val loaded = ConfigFactory.load()
                if (loaded.hasPath(configPath)) loaded.getString(configPath) else null
            }.getOrNull()

            return when {
                fromLoadedConfig == null -> null
                trim -> fromLoadedConfig.trim()
                else -> fromLoadedConfig
            }
        }
    }
}
