package com.wshake.cli.make.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.wshake.cli.make.domain.Meta
import com.wshake.cli.make.utils.FileUtil
import com.wshake.cli.make.utils.makeProgress
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Path


/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-08
 */
class RmCommand : CliktCommand() {
    private val dest: Path by argument().path(canBeFile = false).default(Path.of(""))
    private val json = Json {
        prettyPrint = true       // 格式化输出
        ignoreUnknownKeys = true // JSON和数据模型字段可以不匹配
        coerceInputValues = true // 如果JSON字段是Null则使用默认值
    }
    override fun run() {
        val currentPath = FileUtil.concatPath(System.getProperty("user.dir"), dest.toString())
        val templateFiles = mutableListOf<String>()
        File(currentPath + File.separator + "meta.json").apply {
            if (!this.exists()) {
                echo("${(TextColors.red.plus(TextStyles.bold))("ERROR")} ${this.absolutePath}文件不存在")
                return
            }
            json.decodeFromString<Meta>(this.readText()).apply {
                this.templateFiles?.forEach {
                    templateFiles.add(it.filePath)
                }
                this.db?.forEach {
                    templateFiles.add(currentPath + File.separator+"${it.lowercase()}.json")
                }
            }
        }
        makeProgress(templateFiles.size) { progress, progressInfo ->
            templateFiles.forEach {
                try {
                    if (File(it).exists()) {
                        File(it).deleteOnExit()
                        echo("${(TextColors.green.plus(TextStyles.bold))("SUCCESS")} $it")
                    }
                } catch (e: Throwable) {
                    echo("${(TextColors.red.plus(TextStyles.bold))("ERROR")} $it")
                } finally {
                    progressInfo.progressNumber++
                    repeat(20) {
                        progress.update(progressInfo.progressNumber)
                        Thread.sleep(20)
                    }
                }
            }


        }
    }
}