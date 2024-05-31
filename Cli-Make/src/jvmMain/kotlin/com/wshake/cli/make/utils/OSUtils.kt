package com.wshake.cli.make.utils

import java.io.File
import java.util.*

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-14
 */
object OSUtils {
    val IS_WINDOWS: Boolean = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")

    val IS_CYGWIN: Boolean =
        IS_WINDOWS && System.getenv("PWD") != null && System.getenv("PWD").startsWith("/")

    @Deprecated("")
    val IS_MINGW: Boolean =
        IS_WINDOWS && System.getenv("MSYSTEM") != null && System.getenv("MSYSTEM").startsWith("MINGW")

    val IS_MSYSTEM: Boolean = IS_WINDOWS && System.getenv("MSYSTEM") != null && (System.getenv(
        "MSYSTEM"
    ).startsWith("MINGW") || System.getenv("MSYSTEM") == "MSYS")

    val IS_CONEMU: Boolean = (IS_WINDOWS
            && System.getenv("ConEmuPID") != null)

    val IS_OSX: Boolean = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("mac")

    var TTY_COMMAND: String?
    var STTY_COMMAND: String?
    var STTY_F_OPTION: String?
    var INFOCMP_COMMAND: String?

    init {
        var tty: String
        var stty: String
        var sttyfopt: String?
        var infocmp: String
        if (IS_CYGWIN || IS_MSYSTEM) {
            tty = "tty.exe"
            stty = "stty.exe"
            sttyfopt = null
            infocmp = "infocmp.exe"
            val path = System.getenv("PATH")
            if (path != null) {
                val paths = path.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (p in paths) {
                    if (tty == null && File(p, "tty.exe").exists()) {
                        tty = File(p, "tty.exe").absolutePath
                    }
                    if (stty == null && File(p, "stty.exe").exists()) {
                        stty = File(p, "stty.exe").absolutePath
                    }
                    if (infocmp == null && File(p, "infocmp.exe").exists()) {
                        infocmp = File(p, "infocmp.exe").absolutePath
                    }
                }
            }
        } else {
            tty = "tty"
            stty = "stty"
            sttyfopt = "-F"
            infocmp = "infocmp"
            if (IS_OSX) {
                stty = "/bin/stty"
                sttyfopt = "-f"
            }
        }
        TTY_COMMAND = tty
        STTY_COMMAND = stty
        STTY_F_OPTION = sttyfopt
        INFOCMP_COMMAND = infocmp

        println("################init")
    }
}