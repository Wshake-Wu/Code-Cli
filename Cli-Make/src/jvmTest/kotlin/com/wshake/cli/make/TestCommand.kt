package com.wshake.cli.make

import com.wshake.cli.make.domain.TemplateFile
import com.wshake.cli.make.utils.FileUtil
import freemarker.template.Configuration
import freemarker.template.Template
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.io.File
import java.io.StringReader
import java.io.StringWriter
import java.math.BigDecimal
import java.sql.Types
import javax.sql.rowset.RowSetMetaDataImpl

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-07
 */
class TestCommand {
    // <#assign[\s]+name[\s]*=[\s]*"([a-zA-Z]+[0-9_]*)"[\s]*/*\>
    // 正则表达式：\$\<assign[\s]+name[\s]*=[\s]*"([a-zA-Z]+[0-9_]*)"[\s]*/\>
    fun parseFTL(ftlContent: String): MutableMap<String, Any> {
        val configuration = Configuration(Configuration.VERSION_2_3_31)
        val template = Template("template", StringReader(ftlContent), configuration)

        val dataModel = mutableMapOf<String, Any>()
        val map = mutableMapOf<String, Any>()
        map["title"] = "FreeMarker"
        dataModel["share"] = map
        dataModel["header"] = "Welcome to FreeMarker"
        dataModel["content"] = "This is ${dataModel["header"]}!"
        val stringWriter = StringWriter()
        val env = template.createProcessingEnvironment(dataModel, stringWriter)

        template.process(dataModel, stringWriter)
        println("stringWriter: $stringWriter")

        // 返回解析后的数据模型
        return dataModel
    }

    @Test
    fun freemarkerTest() {
        val ftlTemplate = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>${'$'}{share.title}</title>
        </head>
        <body>
            <h1>${'$'}{header}</h1>
            <p>${'$'}{content}</p>
        </body>
        </html>
        """.trimIndent()

        val parsedData = parseFTL(ftlTemplate)

        println("Title: ${parsedData}")
        println("Header: ${parsedData["header"]}")
        // 如果有其他插槽，也可以在这里添加相应的输出

    }

    @Test
    fun concatPathTest() {
        val concatPath = FileUtil.concatPath("E:\\", "\\test")
        println("concatPath: $concatPath")
    }

    @Test
    fun jsonTest() {
        val json = """
            {
              "filePath":"E:\\Project\\Demo\\Wshake\\.\\test4\\common\\test.java.ftl"

            }
        """.trimIndent()

        @OptIn(ExperimentalSerializationApi::class)
        val kJson = Json {
            prettyPrint = true       // 格式化输出
            ignoreUnknownKeys = true // JSON和数据模型字段可以不匹配
            coerceInputValues = true // 将不正确的 JSON 值强制转换为默认属性值
            explicitNulls = true     // 如果数据模型字段是Null则输出Null
        }
        val map = kJson.decodeFromString<TemplateFile>(json)

        println("map: $map")
    }

    @Test
    fun existFile() {
        val file = File("E:\\Project\\Demo\\Wshake\\..\\Wshake")
        println("filePath.exists(): ${file.listFiles()}")
    }

    @Test
    fun rangeTest() {
        // val readText = "<#assign _sad= \"test2\">"
        // 正则
        // val regex = Regex("<#assign\\s+[a-zA-Z_\$][a-zA-Z0-9_\$]*\\s*=\\s*\"([a-zA-Z_\$][a-zA-Z0-9_\$]*)\"\\s*/*>")
        val readText = "\${  asd   }"
        val regex = Regex("\\\$\\{\\s*([a-zA-Z_\$][a-zA-Z0-9_\$]*)\\s*}")
        val find = regex.findAll(readText)
        println("find: ${find.iterator().next().groupValues}")
    }

    @Test
    fun sqlTest() {
        Database.connect(
            "jdbc:mysql://localhost:3306/order",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "123456"
        )
        transaction {
            // SchemaUtils.listDatabases().forEach {
            //     Terminal().println("1table: $it")
            // }
            // connection.metadata {
            //     //从metadata中获取列名元数据
            //     this.columns()
            //
            //
            connection.metadata {
                // 从metadata中获取列名元数据
                this.columns(Table("orders")).forEach {
                    // 将it.value[0].type转换为kotlin类型
                    val typeInt = it.value[0].type

                }
                // 获取columns注释

            }
        }
    }

    @Test
    fun mysqlTest() {
        RowSetMetaDataImpl().getColumnTypeName(Types.BIGINT).apply {
            println("type: $this")
        }
    }

    @Test
    fun classNameTest() {
        println("${BigDecimal::class}")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true       // 格式化输出
        ignoreUnknownKeys = true // JSON和数据模型字段可以不匹配
        coerceInputValues = true // 将不正确的 JSON 值强制转换为默认属性值
        allowTrailingComma = true // 允许JSON中的尾随逗号
        explicitNulls = true     // 如果数据模型字段是Null则输出Null
        encodeDefaults = true    // 不忽略null值
    }

    @Test
    fun DbTest() {

    }

    @Test
    fun regexTest() {
        val regex = "\\$\\{([a-zA-Z_$][a-zA-Z0-9_$]*(?:\\.[a-zA-Z_$][a-zA-Z0-9_$]*)*)}".toRegex()
        // \$\{\s*(([a-zA-Z_$][a-zA-Z0-9_$]*).?)*\s*}
        val find = regex.findAll("\${test1.test2.test3.test4}")
        println("find: $find")
        find.iterator().forEach { matchResult ->
            val groupValues = matchResult.groupValues
            println("groupValues: $groupValues")
            // val value = groupValues[1]
        }
    }
}