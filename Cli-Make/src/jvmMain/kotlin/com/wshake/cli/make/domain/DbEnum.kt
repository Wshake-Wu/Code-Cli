package com.wshake.cli.make.domain

import com.github.ajalt.mordant.terminal.Terminal
import com.wshake.cli.make.inquirer.KInquirer
import com.wshake.cli.make.inquirer.components.promptCheckbox
import com.wshake.cli.make.inquirer.components.promptInput
import com.wshake.cli.make.utils.MetaDataUtil
import com.wshake.cli.make.utils.camelCase
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.sql.Connection
import java.sql.DriverManager

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-08
 */
enum class DbEnum {
    MYSQL
    ;

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        private val json = Json {
            prettyPrint = true       // 格式化输出
            ignoreUnknownKeys = true // JSON和数据模型字段可以不匹配
            coerceInputValues = true // 将不正确的 JSON 值强制转换为默认属性值
            allowTrailingComma = true // 允许JSON中的尾随逗号
            explicitNulls = true     // 如果数据模型字段是Null则输出Null
            encodeDefaults = true    // 不忽略null值
        }
        fun getDataSources(sqlEnumList: List<DbEnum>): MutableMap<String, DbInfoMap> {
            val map = mutableMapOf<String,DbInfoMap>()
            sqlEnumList.forEach {
                when (it) {
                    MYSQL -> {
                        Terminal().println("请依次输入MySQL信息")
                        val sqlInfo = getSqlInfo()
                        try {
                            DriverManager.getConnection(
                                "jdbc:mysql://${sqlInfo.ip}:${sqlInfo.port}",
                                sqlInfo.username,
                                sqlInfo.password
                            ).apply {
                                map[MYSQL.name.lowercase()] = getMysqlMetaData(this)
                            }
                        } catch (e: Exception) {
                            Terminal().println("MySQL连接异常")
                        }

                    }
                }
            }
            return map
        }

        private fun getSqlInfo(): SqlInfo {
            val sqlInfo = SqlInfo()
            sqlInfo.ip = KInquirer.promptInput("请输入IP")
            sqlInfo.port = KInquirer.promptInput("请输入Port")
            sqlInfo.username = KInquirer.promptInput("请输入username")
            sqlInfo.password = KInquirer.promptInput("请输入password")
            return sqlInfo
        }

        private fun getMysqlMetaData(connection: Connection): DbInfoMap {
            val metaData = connection.metaData
            // 获取数据库列表
            val catalogs = metaData.catalogs
            val catalogList = mutableListOf<String>()
            while (catalogs.next()) {
                catalogList.add(catalogs.getString(1))
            }
            val dataBase = KInquirer.promptCheckbox("请选择数据库", catalogList)
            val dbInfoMap:DbInfoMap = mutableMapOf()
            dataBase.forEach {
                val dbInfo = DbInfo(
                    dbName = it,
                    lowCamelName = it.camelCase(),
                    upCamelName = it.camelCase().replaceFirstChar(Char::uppercase),
                )
                metaData.getTables(it, null, null, arrayOf("TABLE", "VIEW")).use { tables ->
                    while (tables.next()) {
                        val tableName = tables.getString("TABLE_NAME")
                        val tableRemarks = tables.getString("REMARKS")
                        val primaryKeys = metaData.getPrimaryKeys(it, null, tableName)
                        val keyList = mutableListOf<String>()
                        while (primaryKeys.next()) {
                            val columnKey = primaryKeys.getString("COLUMN_NAME")
                            keyList.add(columnKey)
                        }
                        val tableInfo = TableInfo(
                            tableName = tableName,
                            lowCamelName = tableName.camelCase(),
                            upCamelName = tableName.camelCase().replaceFirstChar(Char::uppercase),
                            comment = tableRemarks
                        )
                        metaData.getColumns(it, null, tableName, null).use { columns ->
                            tableInfo.columns.addAll(MetaDataUtil.getColumnMetaData(columns, keyList))
                        }
                        dbInfo.tables[tableName] = tableInfo
                    }
                }
                dbInfoMap[it] = dbInfo
            }
            return dbInfoMap
        }
    }

}

