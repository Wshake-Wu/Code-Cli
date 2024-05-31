package com.wshake.cli.make

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.wshake.cli.make.domain.FileEnum
import com.wshake.cli.make.domain.Meta
import com.wshake.cli.make.domain.RootInfo
import com.wshake.cli.make.domain.TemplateFile
import com.wshake.cli.make.inquirer.KInquirer
import com.wshake.cli.make.inquirer.components.promptConfirm
import com.wshake.cli.make.inquirer.components.promptInput
import com.wshake.cli.make.inquirer.components.promptList
import com.wshake.cli.make.utils.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.collections.set
import kotlin.system.exitProcess

class Main : CliktCommand(invokeWithoutSubcommand = true) {
    private var templatePath: String = FileUtil.getCurrentJarPath() + File.separator + "templates"
    private val rootInfo = RootInfo()
    private val dest: Path by argument().path(canBeFile = false).default(Path.of(""))
    private var isCover = false

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
        if (currentContext.invokedSubcommand != null) {
            return
        }
        // key:老路径 value:拷贝后的新路径
        val copyFileMap = mutableMapOf<File, File>()
        FileUtil.concatPath(System.getProperty("user.dir"), dest.toString()).apply {
            rootInfo.rootMakePath = this
        }

        val queue: ArrayDeque<File> = ArrayDeque()
        queue.add(File(templatePath))
        while (queue.isNotEmpty()) {
            val templateDirList = mutableListOf<String>()
            queue.removeFirst().apply {
                templateDirList.addAll(FileUtil.getFilesInDirectory(this.absolutePath, 1))
            }
            val path = KInquirer.promptList("请选择模板", templateDirList)

            val pathDir = File(templatePath + File.separator + path)
            templatePath = pathDir.absolutePath
            // 判断dir目录里是否是空目录
            if (pathDir.listDir().isEmpty()) {
                echo("${(TextColors.red.plus(TextStyles.bold))("ERROR")} 找不到正确的模板，请检查模板目录")
                exitProcess(1)
            }
            if (File(pathDir.absolutePath + File.separator + FileEnum.META.fileName).exists()) {
                break
            }
            queue.addLast(pathDir)
        }

        KInquirer.promptConfirm(message = "是否在当前路径生成模板", default = true).isFalse {
            rootInfo.rootMakePath = FileUtil.concatPath(
                rootInfo.rootMakePath, KInquirer.promptInput(message = "输入生成路径(格式:xxx\\xxx)")
            )
        }

        echo("生成路径:${rootInfo.rootMakePath},可以使用CTRL+C退出")

        KInquirer.promptConfirm(message = "是否开启全局覆盖生成", default = false).isTrue {
            isCover = true
        }

        val metaFile = File(templatePath + File.separator + FileEnum.META.fileName)
        rootInfo.meta = json.decodeFromString<Meta>(metaFile.readText())
        rootInfo.meta.templateFiles.isNull {
            rootInfo.meta.templateFiles = mutableListOf()
        }
        val makeMetaFile = File(rootInfo.rootMakePath + File.separator + FileEnum.META.fileName)


        FileUtil.loopDirectory(File(templatePath)) loop@{
            it.isDirectory.isTrue {
                File(it.absolutePath + File.separator + "tag").exists().isTrue {
                    rootInfo.metaDir[it.absolutePath] = FileUtil.concatPath(
                        rootInfo.rootMakePath,
                        KInquirer.promptInput(message = "输入${it.name}文件夹的生成路径(格式:xxx\\xxx)")
                    )
                }
            }
            it.isFile.isTrue {
                FileUtil.fileEndsWith(it, ".ftl") {
                    var makeFile = rootInfo.rootMakePath
                    rootInfo.metaDir.containsKey(it.parentFile.absolutePath).isTrue {
                        makeFile = rootInfo.metaDir[it.parentFile.absolutePath].toString()
                    }
                    makeFile += it.absolutePath.replace(templatePath, "")
                    File(makeFile).apply {
                        rootInfo.meta.templateFiles?.forEach { templateFile ->
                            if (File(templateFile.filePath).absolutePath == it.absolutePath) {
                                templateFile.filePath = this.absolutePath
                                if (isCover || CommandUtil.promptFileCover(this)) {
                                    copyFileMap[it] = this
                                }
                                return@loop
                            }
                        }

                        rootInfo.meta.templateFiles?.add(TemplateFile(filePath = this.absolutePath))
                        if (isCover || CommandUtil.promptFileCover(this)) {
                            copyFileMap[it] = this
                        }
                    }
                }
            }
        }

        if (copyFileMap.isEmpty()) {
            echo("${(TextColors.red.plus(TextStyles.bold))("ERROR")} 没有需要生成的模板文件")
            exitProcess(1)
        }
        metaFile.writeText(json.encodeToString(Meta.serializer(), rootInfo.meta))
        if (isCover || CommandUtil.promptFileCover(makeMetaFile)) {
            copyFileMap[metaFile] = makeMetaFile
        }
        makeProgress(copyFileMap.size) { progress, progressInfo ->
            echo((TextColors.gray)("###############################################"))
            copyFileMap.entries.forEach {
                try {
                    it.value.parentFile.mkdirs()
                    Files.copy(it.key.inputStream(), it.value.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    echo("${(TextColors.green.plus(TextStyles.bold))("SUCCESS")} ${it.value.absolutePath}")
                } catch (e: Throwable) {
                    echo("${(TextColors.red.plus(TextStyles.bold))("ERROR")} ${it.value.absolutePath}")
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
        rootInfo.meta.tips.isNullOrBlank().isFalse {
            echo((TextColors.gray)("###############################################"))
            echo((TextColors.blue.plus(TextStyles.bold))(rootInfo.meta.tips!!))
        }
    }
}


fun main(args: Array<String>) {
    // val start = Main()
    // start.subcommands(RefCommand(), RmCommand(), GenCommand(), CopyCommand())
    // try {
    //     start.main(args)
    // } catch (e: Throwable) {
    //     exitProcess(1)
    // }
    test()
}

fun test() {

}

private fun setTerminalToCBreak() {
    val os = System.getProperty("os.name").toLowerCase()

    val sttyCommand = when {
        os.contains("nix") || os.contains("nux") || os.contains("mac") -> arrayOf(
            "/bin/sh",
            "-c",
            "stty -echo raw < /dev/tty"
        )

        os.contains("win") -> arrayOf("cmd", "/c", "stty -echo raw < /dev/tty")
        else -> throw UnsupportedOperationException("Unsupported operating system")
    }

    Runtime.getRuntime().exec(sttyCommand).waitFor()
}

private fun restoreTerminal() {
    val os = System.getProperty("os.name").toLowerCase()

    val sttyCommand = when {
        os.contains("nix") || os.contains("nux") || os.contains("mac") -> arrayOf(
            "/bin/sh",
            "-c",
            "stty echo cooked < /dev/tty"
        )

        os.contains("win") -> arrayOf("cmd", "/c", "stty echo cooked < /dev/tty")
        else -> throw UnsupportedOperationException("Unsupported operating system")
    }

    Runtime.getRuntime().exec(sttyCommand).waitFor()
}
