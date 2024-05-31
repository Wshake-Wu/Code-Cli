package com.wshake.cli.make.utils

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.animation.ProgressAnimation
import com.github.ajalt.mordant.animation.progressAnimation
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.widgets.Spinner
import com.wshake.cli.make.inquirer.KInquirer
import com.wshake.cli.make.inquirer.components.promptConfirm
import java.io.File

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2023-12-30
 */

object CommandUtil {
    fun promptFileCover(file: File,isFile: Boolean=true):Boolean{
        if (file.exists() && file.isFile==isFile) {
            KInquirer.promptConfirm(message = "${file}已存在，是否覆盖", default = false).isFalse {
                return false
            }
        }
        return true
    }
}
class ProgressInfo{
    var progressNumber = 0
}


fun CliktCommand.makeProgress(size:Int,func: (progressAnimation: ProgressAnimation,progressInfo:ProgressInfo) -> Unit){
    val progress = terminal.progressAnimation {
        spinner(Spinner.Dots(TextColors.brightBlue))
        percentage()
        progressBar(width = 50,)
        timeRemaining(prefix = "time ")
    }
    progress.start()
    val progressInfo = ProgressInfo()
    progress.updateTotal(size.toLong())
    func(progress,progressInfo)
    progress.stop()
}

