package com.wshake.cli.make.utils

import freemarker.cache.FileTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter


/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-08
 */
object FreeMarkerUtil {
    fun processTemplate(file: File, map: Map<String, Any?>) {

        // 初始化 FreeMarker 配置
        val configuration = Configuration(Configuration.VERSION_2_3_31)
        configuration.templateLoader = FileTemplateLoader(file.parentFile)
        configuration.defaultEncoding = "UTF-8"
        configuration.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        // 获取模板对象
        val template: Template = configuration.getTemplate(file.name)

        BufferedWriter(OutputStreamWriter(file.outputStream(), "UTF-8")).use {
            template.process(map, it)
        }
    }
}