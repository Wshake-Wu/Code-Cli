package com.wshake.cli.make.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.wshake.cli.make.domain.DbInfoMap
import com.wshake.cli.make.domain.Meta
import com.wshake.cli.make.domain.RootInfo
import com.wshake.cli.make.inquirer.KInquirer
import com.wshake.cli.make.inquirer.components.promptConfirm
import com.wshake.cli.make.utils.*
import com.wshake.common.exception.BusinessException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.system.exitProcess

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2023-12-28
 */
class GenCommand : CliktCommand(invokeWithoutSubcommand = true) {
    private val rootInfo = RootInfo()

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true       // 格式化输出
        ignoreUnknownKeys = true // JSON和数据模型字段可以不匹配
        coerceInputValues = true // 将不正确的 JSON 值强制转换为默认属性值
        allowTrailingComma = true // 允许JSON中的尾随逗号
        explicitNulls = true     // 如果数据模型字段是Null则输出Null
        encodeDefaults = true    // 不忽略null值
    }
    private var isCover = false
    private val dest: Path by argument().path(canBeFile = false).default(Path.of(""))
    override fun run() {
        FileUtil.concatPath(System.getProperty("user.dir"), dest.toString()).apply {
            rootInfo.rootMakePath = this
        }
        File(rootInfo.rootMakePath + File.separator + "meta.json").apply {
            if (!exists() || isDirectory) {
                echo("${(TextColors.red.plus(TextStyles.bold))("ERROR")} 当前路径不存在meta.json,请重试")
                exitProcess(1)
            }
            rootInfo.meta = json.decodeFromString<Meta>(readText())
            rootInfo.meta.db?.forEach {
                val dbFile = File(this.parent + File.separator + "${it.lowercase()}.json")
                if (!dbFile.exists()) {
                    echo("${(TextColors.red.plus(TextStyles.bold))("ERROR")} ${dbFile.absolutePath}文件不存在")
                    exitProcess(1)
                }
                rootInfo.dbMapList[it.lowercase()] = json.decodeFromString<DbInfoMap>(dbFile.readText())
            }
        }

        KInquirer.promptConfirm(message = "是否开启全局覆盖生成", default = false).isTrue {
            isCover = true
        }
        KInquirer.promptConfirm("确认生成", default = true).isFalse {
            echo("${(TextColors.green.plus(TextStyles.bold))("SUCCESS")} 已取消")
            exitProcess(0)
        }


        val fileMap = mutableMapOf<File, File>()

        rootInfo.meta.templateFiles?.forEach {
            File(it.filePath).apply {
                if (!exists() || !isFile) {
                    echo("${(TextColors.yellow.plus(TextStyles.bold))("WARN")} ${this.absolutePath}路径出现问题,已跳过")
                    return@forEach
                }
                if (!this.absolutePath.endsWith(".ftl")) {
                    echo("${(TextColors.yellow.plus(TextStyles.bold))("WARN")} ${this.absolutePath}不是ftl文件,已跳过")
                    return@forEach
                }

                File(this.absolutePath.removeSuffix(".ftl")).apply make@{
                    if (!isCover) {
                        CommandUtil.promptFileCover(this@make).isFalse {
                            return@forEach
                        }
                    }
                    fileMap[this@apply] = this@make
                }
            }
        }
        if (fileMap.isEmpty()) {
            echo("${(TextColors.red.plus(TextStyles.bold))("ERROR")} 生成文件为空")
            exitProcess(1)
        }

        makeProgress(fileMap.size) { progress, progressInfo ->
            echo((TextColors.gray)("###############################################"))
            fileMap.forEach file@{ fileMap ->
                try {
                    val map = mutableMapOf<String, Any?>()
                    var success = false
                    rootInfo.meta.share?.forEach { (k, v) ->
                        map[k] = v
                    }
                    rootInfo.dbMapList.forEach { (k, v) ->
                        map[k] = v
                    }
                    rootInfo.meta.templateFiles?.forEach { templateFile ->
                        if (File(templateFile.filePath).absolutePath == fileMap.key.absolutePath && templateFile.foreach != null) {
                            success=true
                            ParameterUtil.loopParameter(map, templateFile.foreach) {
                                when (it) {
                                    is Map<*, *> -> {
                                        it.forEach dbForeachMap@{ (k, v) ->
                                            val regex = "\\\$\\{\\s*([a-zA-Z_\$][a-zA-Z0-9_\$]*(?:\\.[a-zA-Z_\$][a-zA-Z0-9_\$]*)*)\\s*}".toRegex()
                                            val find = regex.findAll(templateFile.fileName!!)
                                            val splitFileName= mutableListOf<String>()
                                            find.iterator().forEach { matchResult ->
                                                val groupValues = matchResult.groupValues
                                                splitFileName.addAll(groupValues[1].split("."))
                                            }
                                            when(v){
                                                is Map<*,*>->{
                                                    if (splitFileName.isNotEmpty() && splitFileName.size==2 &&(v.containsKey(splitFileName[1]))){
                                                        map["foreach"]=v
                                                        val fileName=regex.replace(templateFile.fileName ,v[splitFileName[1]]!!.toString())
                                                        val makeFile = File(fileMap.value.parent + File.separator + fileName)
                                                        if(!isCover){
                                                            CommandUtil.promptFileCover(makeFile).isFalse {
                                                                echo("${(TextColors.yellow.plus(TextStyles.bold))("WARN")} ${makeFile.absolutePath}已存在已跳过")
                                                                return@dbForeachMap
                                                            }
                                                        }
                                                        Files.copy(fileMap.key.toPath(), makeFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                                                        FreeMarkerUtil.processTemplate(makeFile, map)
                                                        echo("${(TextColors.green.plus(TextStyles.bold))("SUCCESS")} ${makeFile.absolutePath}")
                                                    }else{
                                                        throw BusinessException("filename参数${templateFile.fileName}错误")
                                                    }
                                                }
                                                else->throw BusinessException("foreach参数${templateFile.foreach}错误")
                                            }
                                        }
                                    }
                                    else -> throw BusinessException("foreach参数${templateFile.foreach}错误")
                                }
                            }
                        }
                    }
                    if (success) {
                        return@file
                    }
                    if(!isCover){
                        CommandUtil.promptFileCover(fileMap.value).isFalse {
                            echo("${(TextColors.yellow.plus(TextStyles.bold))("WARN")} ${fileMap.value.absolutePath}已存在已跳过")
                            return@file
                        }
                    }
                    Files.copy(fileMap.key.toPath(), fileMap.value.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    FreeMarkerUtil.processTemplate(fileMap.value, map)
                    echo("${(TextColors.green.plus(TextStyles.bold))("SUCCESS")} ${fileMap.value.absolutePath}")
                } catch (e: BusinessException) {
                    echo("${(TextColors.red.plus(TextStyles.bold))("ERROR")} ${fileMap.value.absolutePath} ${e.msg}")
                } catch (e: Throwable) {
                    echo("${(TextColors.red.plus(TextStyles.bold))("ERROR")} ${fileMap.value.absolutePath}")
                    e.printStackTrace()
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