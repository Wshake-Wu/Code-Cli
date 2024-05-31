package com.wshake.cli.make.utils

import java.util.*

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-10
 */

fun String.camelCase(): String {
    val regex = Regex("_(.)")
    return this.replace(regex) { it.groupValues[1].uppercase(Locale.getDefault()) }
}