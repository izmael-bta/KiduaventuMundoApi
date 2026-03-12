package com.ismael.kiduaventumundo.db

import com.ismael.kiduaventumundo.config.DatabaseSettings
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object DatabaseFactory {
    fun init(settings: DatabaseSettings) {
        val hikariConfig = HikariConfig().apply {
            driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            jdbcUrl = buildJdbcUrl(settings)
            username = settings.user
            password = settings.password
            maximumPoolSize = 10
            minimumIdle = 1
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        transaction {
            // Create missing tables only; do not attempt ALTERs on existing SQL Server schema.
            SchemaUtils.create(
                UsersTable,
                SessionTable,
                EnglishLevelsTable,
                EnglishActivitiesTable,
                EnglishLevelProgressTable,
                EnglishActivityProgressTable,
                ProgressEventsTable,
                UserProgressSummaryTable
            )
        }
    }

    private fun buildJdbcUrl(settings: DatabaseSettings): String {
        return "jdbc:sqlserver://${settings.host}:${settings.port};databaseName=${settings.database};encrypt=${settings.encrypt};trustServerCertificate=${settings.trustServerCertificate};"
    }
}

fun <T> dbQuery(block: Transaction.() -> T): T = transaction {
    TransactionManager.manager.defaultIsolationLevel = java.sql.Connection.TRANSACTION_REPEATABLE_READ
    block()
}

fun nowUtc(): LocalDateTime = LocalDateTime.now(java.time.Clock.systemUTC())

fun LocalDateTime.toIsoUtcString(): String = this.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
