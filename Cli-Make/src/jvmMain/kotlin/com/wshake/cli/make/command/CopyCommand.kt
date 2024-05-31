package com.wshake.cli.make.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.wshake.cli.make.domain.FileEnum
import com.wshake.cli.make.inquirer.KInquirer
import com.wshake.cli.make.inquirer.components.promptList
import com.wshake.cli.make.utils.CommandUtil
import com.wshake.cli.make.utils.FileUtil
import com.wshake.cli.make.utils.listDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.system.exitProcess

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-13
 */
class CopyCommand : CliktCommand() {
    private val dest: Path by argument().path(canBeFile = false).default(Path.of(""))
    private var templatePath: String = FileUtil.getCurrentJarPath() + File.separator + "templates"
    override fun run() {
        val currentPath = FileUtil.concatPath(System.getProperty("user.dir"), dest.toString())
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
                echo("${(TextColors.red.plus(TextStyles.bold))("ERROR")} 找不到正确的拷贝目录，请检查目录")
                exitProcess(1)
            }
            if (File(pathDir.absolutePath + File.separator + FileEnum.COPY.fileName).exists()) {
                break
            }
            queue.addLast(pathDir)
        }
        val sourceDir = File(templatePath)
        val targetDir = File(currentPath + File.separator + sourceDir.name)

        if(!CommandUtil.promptFileCover(targetDir, false)){
            exitProcess(1)
        }
        targetDir.mkdirs()

        Files.walk(sourceDir.toPath()).forEach { source ->
            //如果source的是名称为copy的文件，则不拷贝
            if (source.toFile().name == FileEnum.COPY.fileName) {
                return@forEach
            }
            val target = targetDir.toPath().resolve(sourceDir.toPath().relativize(source))
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}