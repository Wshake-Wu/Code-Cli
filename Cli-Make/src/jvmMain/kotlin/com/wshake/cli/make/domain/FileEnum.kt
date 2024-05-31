package com.wshake.cli.make.domain

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-11
 */
enum class FileEnum(val fileName: String) {
    META("meta.json"),
    MYSQL("mysql.json"),
    TAG("tag"),
    COPY("copy")
    ;
}