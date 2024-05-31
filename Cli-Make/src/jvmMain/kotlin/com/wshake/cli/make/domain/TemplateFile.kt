package com.wshake.cli.make.domain

import kotlinx.serialization.Serializable

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-10
 */
@Serializable
data class TemplateFile(
    val tips: String? = null,
    var filePath: String,
    val foreach: String? = null,
    val fileName: String? = null,
)