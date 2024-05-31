package com.wshake.cli.make.domain

import kotlinx.serialization.Serializable

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-09
 */
typealias DbInfoMap = MutableMap<String, DbInfo>
@Serializable
data class DbInfo(
    val dbName: String="",
    val lowCamelName: String="",
    val upCamelName: String="",
    val tables: MutableMap<String, TableInfo> = mutableMapOf()
): MutableMap<String, Any?> by mutableMapOf(
    "dbName" to dbName,
    "lowCamelName" to lowCamelName,
    "upCamelName" to upCamelName,
    "tables" to tables
)

@Serializable
data class TableInfo(
    val tableName: String,
    val lowCamelName: String,
    val upCamelName: String,
    val comment: String?=null,
    val columns: MutableList<ColumnInfo> = mutableListOf()
): MutableMap<String, Any?> by mutableMapOf(
    "tableName" to tableName,
    "lowCamelName" to lowCamelName,
    "upCamelName" to upCamelName,
    "comment" to comment,
    "columns" to columns
)

@Serializable
data class ColumnInfo(
    val columnName: String,
    val lowCamelName: String,
    val upCamelName: String,
    val typeInt: Int,
    val typeName: String,
    val typeJavaName: String,
    val typeKotlinName: String,
    val typeKotlinClassName: String?,
    val typeJavaClassName: String?,
    val nullable: Boolean,
    val size: Int,
    val isPrimaryKey: Boolean,
    val isAutoIncrement: Boolean,
    val defaultValue: String?,
    val comment: String?=null
)