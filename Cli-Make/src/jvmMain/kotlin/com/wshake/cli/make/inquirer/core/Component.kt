package com.wshake.cli.make.inquirer.core

interface Component<T> {
    fun value(): T
    fun isInteracting(): Boolean
    fun onEvent(event: KInquirerEvent)
    fun render(): String
}
