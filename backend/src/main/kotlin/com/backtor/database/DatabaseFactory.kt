package com.backtor.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:postgresql://ep-dawn-heart-a5ovsr1c-pooler.us-east-2.aws.neon.tech/neondb?user=neondb_owner&password=npg_ZHw3QYlfa5Kg&sslmode=require"
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 20 // Aumenta si tienes más carga
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            connectionTimeout = 10000 // 10 segundos
            idleTimeout = 60000 // 1 minuto
            maxLifetime = 1800000 // 30 minutos
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
    }
    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }

}