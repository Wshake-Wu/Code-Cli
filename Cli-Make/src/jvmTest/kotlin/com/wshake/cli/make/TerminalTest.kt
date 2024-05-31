package com.wshake.cli.make

import com.github.ajalt.mordant.animation.textAnimation
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import org.junit.jupiter.api.Test

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-13
 */
class TerminalTest {
    @Test
    fun animationTest(){
        val t = Terminal()
        val a = t.textAnimation<Int> { frame ->
            (1..50).joinToString("") {
                val hue = (frame + it) * 3 % 360
                TextColors.hsv(hue, 1, 1)("━")
            }
        }

        t.cursor.hide(showOnExit = true)
        repeat(120) {
            a.update(it)
            Thread.sleep(25)
        }
    }
    @Test
    fun textStyleTest(){
        // val reader = System.console().reader()
        //一直读取输入指定回车
        while (true) {
            // val read = reader.read()
            val readln = readlnOrNull()
            println(readln)
            break
        }
    }
}