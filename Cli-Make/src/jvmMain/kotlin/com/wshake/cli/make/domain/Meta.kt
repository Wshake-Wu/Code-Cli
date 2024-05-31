package com.wshake.cli.make.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-08
 */
@Serializable
data class Meta(
    val tips: String? = null,
    /**
     * 支持的数据源
     */
    var db: List<String>? = null,
    /**
     * 共享数据
     */
    var share: MutableMap<String, JsonElement?>? = null,
    /**
     * 生成的模板集合
     */
    var templateFiles: MutableList<TemplateFile>? = null,
)