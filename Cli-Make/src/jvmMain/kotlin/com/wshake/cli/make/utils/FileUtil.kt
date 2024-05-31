package com.wshake.cli.make.utils

import com.github.ajalt.mordant.terminal.Terminal
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.pathString
import kotlin.system.exitProcess

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2023-12-28
 */
object FileUtil {
    /**
     * 获取当前jar包所在目录
     */
    fun getCurrentJarPath(): String {
        val jarPath = object {}.javaClass.protectionDomain.codeSource.location.toURI().path
        return File(jarPath).parent
    }

    /**
     * 获取文件夹下的文件
     * @param directoryPath 文件夹路径
     * @param code 1时返回文件夹，为0时返回文件和文件夹，为-1时返回文件
     */
    fun getFilesInDirectory(directoryPath: String, code: Int? = 0): List<String> {
        val directory = File(directoryPath)
        directory.listFiles()
        // code为1时返回文件夹，为0时返回文件和文件夹，为-1时返回文件
        val files = directory.listFiles()
        val fileList = mutableListOf<String>()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory && code!! >= 0) {
                    fileList.add(file.name)
                } else if (file.isFile && code!! <= 0) {
                    fileList.add(file.name)
                }
            }
        }
        return fileList
    }


    /**
     * 根据文件后缀获取文件集合
     */
    inline fun fileEndsWith(file: File, suffix: String, func: () -> Unit) {
        if (file.isFile && file.name.endsWith(suffix)) func()
    }

    inline fun loopDirectory(dirFile: File, fileFunc: (dirFile: File) -> Unit) {
        val queue: ArrayDeque<File> = ArrayDeque()
        queue.add(dirFile)
        while (queue.isNotEmpty()) {
            queue.removeFirst().apply {
                listFiles()?.forEach {
                    if (it.isDirectory) {
                        queue.add(it)
                    }
                    fileFunc(it)
                }
            }
        }
    }

    inline fun loopFileExist(dirFile: File, fileName: String,func: () -> Unit) {
        val queue: ArrayDeque<File> = ArrayDeque()
        queue.add(dirFile)
        while (queue.isNotEmpty()) {
            queue.removeFirst().apply {
                listFiles()?.forEach {
                    if (it.isDirectory) {
                        queue.add(it)
                    }
                    if (it.name == fileName) {
                        func()
                    }
                }
            }
        }
    }

    /**
     * 判断路径是绝对路径还是相对路径
     */
    fun concatPath(oldPath: String, newPath: String): String {
        try {
            val path = Paths.get(newPath)
            if (path.isAbsolute) {
                return path.pathString
            }

            return oldPath + File.separator + path.pathString
        } catch (e: Exception) {
            Terminal().println("路径格式有误")
            exitProcess(1)
        }
    }
}

fun File.listDir(): List<File> {
    val list = mutableListOf<File>()
    this.listFiles()?.forEach {
        if (it.isDirectory) {
            list.add(it)
        }
    }
    return list
}