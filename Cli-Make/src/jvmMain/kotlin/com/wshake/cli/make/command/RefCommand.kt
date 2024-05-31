package com.wshake.cli.make.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.wshake.cli.make.domain.*
import com.wshake.cli.make.utils.FileUtil
import com.wshake.cli.make.utils.isFalse
import com.wshake.cli.make.utils.isNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.File
import java.nio.file.Path

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-08
 */
class RefCommand : CliktCommand() {
    private val rootInfo = RootInfo()
    private var templatePath: String = FileUtil.getCurrentJarPath() + File.separator + "templates"
    private val dest: Path by argument().path(canBeFile = false).default(Path.of(""))

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true       // 格式化输出
        ignoreUnknownKeys = true // JSON和数据模型字段可以不匹配
        coerceInputValues = true // 将不正确的 JSON 值强制转换为默认属性值
        allowTrailingComma = true // 允许JSON中的尾随逗号
        explicitNulls = true     // 如果数据模型字段是Null则输出Null
        encodeDefaults = true    // 不忽略null值
    }

    override fun run() {
        val currentPath = FileUtil.concatPath(System.getProperty("user.dir"), dest.toString())
        val dbList = mutableListOf<DbEnum>()
        File(currentPath + File.separator + "meta.json").apply {
            if (!exists()) {
                echo("${(TextColors.red.plus(TextStyles.bold))("ERROR")} meta.json文件不存在")
                return
            }
            rootInfo.meta = json.decodeFromString<Meta>(this.readText())
            rootInfo.meta.db?.forEach {
                dbList.add(DbEnum.valueOf(it.uppercase()))
            }
        }
        val dbMetaData: MutableMap<String, DbInfoMap> = mutableMapOf()
        if (!currentPath.startsWith(templatePath)) {
            dbMetaData.putAll(DbEnum.getDataSources(dbList))
        }

        rootInfo.meta.templateFiles.isNull {
            rootInfo.meta.templateFiles = mutableListOf()
            FileUtil.loopDirectory(File(currentPath)) {
                if (it.isFile && it.name.endsWith(".ftl")) {
                    rootInfo.meta.templateFiles?.add(TemplateFile(filePath = it.absolutePath))
                }
            }
        }
        val regexMap = mutableMapOf<String, JsonElement?>()
        rootInfo.meta.templateFiles?.forEach {
            val readText = File(it.filePath).readText()
            // 正则
            val regex =
                Regex("\\\$\\{\\s*([a-zA-Z_\$][a-zA-Z0-9_\$]*)\\s*}")
            val find = regex.findAll(readText)
            find.iterator().forEach { matchResult ->
                val groupValues = matchResult.groupValues
                val value = groupValues[1]
                regexMap[value] = null
            }
        }

        regexMap.entries.forEach {
            if (rootInfo.meta.share == null) {
                rootInfo.meta.share = mutableMapOf()
            }
            rootInfo.meta.share!!.containsKey(it.key).isFalse {
                rootInfo.meta.share!![it.key] = null
            }
        }

        // 重新覆盖写入meta.json
        File(currentPath + File.separator + "meta.json").apply {
            val metaJson = json.encodeToString(rootInfo.meta)
            this.writeText(metaJson)
            echo("${(TextColors.green.plus(TextStyles.bold))("SUCCESS")} meta.json已刷新")
        }
        // 将dbMetaData写入db.json
        dbMetaData.forEach { (k, v) ->
            File(currentPath + File.separator + "$k.json").apply {
                val dbJson = json.encodeToString(v)
                this.writeText(dbJson)
                echo("${(TextColors.green.plus(TextStyles.bold))("SUCCESS")} ${k}.json已刷新")
            }
        }
    }
}