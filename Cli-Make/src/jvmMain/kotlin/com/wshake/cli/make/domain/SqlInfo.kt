package com.wshake.cli.make.domain

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-09
 */
data class SqlInfo(
    var ip: String="",
    var port: String="",
    var username: String="",
    var password: String="",
    val defaultMaxPoolSize: Int = 10,
)