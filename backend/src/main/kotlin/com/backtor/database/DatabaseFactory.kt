package com.backtor.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:postgresql://ep-dawn-heart-a5ovsr1c-pooler.us-east-2.aws.neon.tech/neondb?user=neondb_owner&password=npg_ZHw3QYlfa5Kg&sslmode=require"
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
    }
}
