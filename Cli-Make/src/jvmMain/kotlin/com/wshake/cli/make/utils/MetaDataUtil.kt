package com.wshake.cli.make.utils

import com.wshake.cli.make.domain.ColumnInfo
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.Types
import java.util.*
import kotlin.reflect.KClass

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-10
 */
object MetaDataUtil {
    private fun typeCastKotlin(typeInt: Int): String {
        return when (typeInt) {
            Types.BIGINT -> "Long"
            Types.BINARY, Types.BLOB, Types.LONGVARBINARY, Types.VARBINARY -> "ByteArray"
            Types.BIT, Types.BOOLEAN, Types.TINYINT -> "Boolean"
            Types.CHAR, Types.CLOB, Types.DATALINK, Types.DISTINCT, Types.LONGNVARCHAR, Types.LONGVARCHAR,
            Types.NCHAR, Types.NCLOB, Types.NVARCHAR, Types.REF, Types.ROWID, Types.SQLXML, Types.STRUCT, Types.VARCHAR -> "String"
            Types.DATE, Types.TIME, Types.TIMESTAMP -> "Date"
            Types.DECIMAL, Types.NUMERIC -> "BigDecimal"
            Types.DOUBLE -> "Double"
            Types.FLOAT, Types.REAL -> "Float"
            Types.INTEGER -> "Int"
            Types.SMALLINT -> "Short"
            Types.JAVA_OBJECT, Types.NULL, Types.OTHER -> "Any"
            else -> "Any"
        }
    }

    private fun typeCastKotlinClass(typeInt: Int): KClass<out Any>? {
        return when (typeInt) {
            Types.DATE, Types.TIME, Types.TIMESTAMP -> Date::class
            Types.DECIMAL, Types.NUMERIC -> BigDecimal::class
            else -> null
        }
    }

    private fun typeCastJava(typeInt: Int): String {
        return when (typeInt) {
            Types.BIGINT -> "Long"
            Types.BINARY, Types.BLOB, Types.LONGVARBINARY, Types.VARBINARY -> "byte[]"
            Types.BIT, Types.BOOLEAN, Types.TINYINT -> "Boolean"
            Types.CHAR, Types.CLOB, Types.DATALINK, Types.DISTINCT, Types.LONGNVARCHAR, Types.LONGVARCHAR,
            Types.NCHAR, Types.NCLOB, Types.NVARCHAR, Types.REF, Types.ROWID, Types.SQLXML, Types.STRUCT, Types.VARCHAR -> "String"
            Types.DATE, Types.TIME, Types.TIMESTAMP -> "Date"
            Types.DECIMAL, Types.NUMERIC -> "BigDecimal"
            Types.DOUBLE -> "Double"
            Types.FLOAT, Types.REAL -> "Float"
            Types.INTEGER -> "Integer"
            Types.SMALLINT -> "Short"
            Types.JAVA_OBJECT, Types.NULL, Types.OTHER -> "Object"
            else -> "Object"
        }
    }
    private fun typeCastJavaClass(typeInt: Int): KClass<out Any>? {
        return when (typeInt) {
            Types.DATE, Types.TIME, Types.TIMESTAMP -> Date::class
            Types.DECIMAL, Types.NUMERIC -> BigDecimal::class
            else -> null
        }
    }

    fun getColumnMetaData(columns: ResultSet, keyList: List<String>): MutableList<ColumnInfo> {
        val list = mutableListOf<ColumnInfo>()
        while (columns.next()) {
            val columnName = columns.getString("COLUMN_NAME")
            val columnTypeInt = columns.getInt("DATA_TYPE")
            val columnTypeName = columns.getString("TYPE_NAME")
            val columnRemarks = columns.getString("REMARKS")
            val columnSize = columns.getInt("COLUMN_SIZE")
            val columnAutoIncrement = columns.getString("IS_AUTOINCREMENT") == "YES"
            val columnDefaultDbValue = columns.getString("COLUMN_DEF")
            val columnNullable = columns.getBoolean("NULLABLE")
            val columnTypeJavaName = typeCastJava(columnTypeInt)
            val columnTypeKotlinName = typeCastKotlin(columnTypeInt)
            val columnTypeJavaClass = typeCastJavaClass(columnTypeInt)
            val columnTypeKotlinClass = typeCastKotlinClass(columnTypeInt)
            val columnTypeJavaClassName = columnTypeJavaClass?.qualifiedName
            val columnTypeKotlinClassName = columnTypeKotlinClass?.qualifiedName
            val columnInfo = ColumnInfo(
                columnName = columnName,
                lowCamelName = columnName.camelCase(),
                upCamelName = columnName.camelCase().replaceFirstChar(Char::uppercase),
                typeInt = columnTypeInt,
                typeName = columnTypeName,
                typeJavaName = columnTypeJavaName,
                typeKotlinName = columnTypeKotlinName,
                typeKotlinClassName = columnTypeKotlinClassName,
                typeJavaClassName = columnTypeJavaClassName,
                nullable = columnNullable,
                size = columnSize,
                isPrimaryKey = keyList.contains(columnName),
                isAutoIncrement = columnAutoIncrement,
                defaultValue = columnDefaultDbValue,
                comment = columnRemarks
            )
            list.add(columnInfo)
        }
        return list
    }
}