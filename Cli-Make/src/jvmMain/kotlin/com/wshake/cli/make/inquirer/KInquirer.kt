package com.wshake.cli.make.inquirer

import com.wshake.cli.make.inquirer.core.AnsiOutput
import com.wshake.cli.make.inquirer.core.Component
import com.wshake.cli.make.inquirer.core.KInquirerReaderHandler
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import java.io.Reader
import kotlin.system.exitProcess

object KInquirer {

    fun <T> prompt(component: Component<T>): T {
        runTerminal { reader ->
            val readerHandler = KInquirerReaderHandler.getInstance()
            AnsiOutput.display(component.render())
            while (component.isInteracting()) {
                val event = readerHandler.handleInteraction(reader)
                component.onEvent(event)
                AnsiOutput.display(component.render())
            }
        }
        return component.value()
    }

    private fun runTerminal(func: (reader: Reader) -> Unit) {
        val terminal: Terminal = TerminalBuilder.builder()
            .jna(true)
            .streams(System.`in`, System.out)
            .system(true)
            .signalHandler {
                if (it.name == "INT") {
                    com.github.ajalt.mordant.terminal.Terminal().println("Terminate batch job")
                    exitProcess(1)
                }
            }
            .build()
        terminal.enterRawMode()
        val reader = terminal.reader()
        func(reader)
        reader.close()
        terminal.close()
        // func(System.`in`.reader())
    }
}
