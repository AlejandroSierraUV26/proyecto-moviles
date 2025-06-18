package com.backtor.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseFactory {
    private val dotenv = dotenv {
        ignoreIfMissing = true // para que no truene si usas variables de entorno reales en prod
    }
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = dotenv["JDBC_DATABASE_URL"]
                ?: System.getenv("JDBC_DATABASE_URL")
                        ?: error("JDBC_DATABASE_URL no definida en .env o entorno")
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