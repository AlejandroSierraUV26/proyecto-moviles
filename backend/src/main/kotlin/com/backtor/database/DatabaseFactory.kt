package com.backtor.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv("JDBC_DATABASE_URL")
                ?: "jdbc:postgresql://ep-flat-cloud-a4yq0awp-pooler.us-east-1.aws.neon.tech/neondb?sslmode=require"
            driverClassName = "org.postgresql.Driver"
            username = System.getenv("DB_USER") ?: "neondb_owner"       // Agrega usuario
            password = System.getenv("DB_PASSWORD") ?: "npg_w3hPvt2EHbof"  // Agrega contraseña
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
    }
}

