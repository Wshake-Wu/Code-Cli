package com.wshake.cli.make.domain

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2023-12-28
 */
data class RootInfo(
    /**
     * 是否在当前路径生成
     */
    var isMakeRootDir: Boolean?,
    /**
     * 生成代码的路径
     */
    var rootMakePath: String,
    /**
     * package目录信息 key:模板原来的相对路径,value:替换后的相对路径
     */
    val metaDir: MutableMap<String,String>,

    var meta: Meta,

    val dbMapList: MutableMap<String, DbInfoMap> = mutableMapOf()

    ) {
    constructor() : this(false, System.getProperty("user.dir"), mutableMapOf(), Meta(
        tips = null,
        share =mutableMapOf(),
        templateFiles = mutableListOf(),
        db = listOf()
    ))
}