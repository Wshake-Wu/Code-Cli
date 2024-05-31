import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.graalvm.buildtools.gradle.dsl.GraalVMExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-13
 */
fun Project.shadowJarInit() {
    apply(plugin = "com.github.johnrengelman.shadow")
    tasks.withType<ShadowJar> {
        // archiveBaseName-archiveVersion-archiveClassifier.jar
        archiveBaseName.set(archivesBaseName)
        archiveVersion.set("$version")
        archiveClassifier.set("")
        manifest {
            attributes["Main-Class"] = mainClassPath
        }
    }
}

fun Project.kaptInit() {
    apply(plugin = "org.jetbrains.kotlin.kapt")
    extensions.configure<KaptExtension> {
        arguments {
            // Set Mapstruct Configuration options here
            // https://kotlinlang.org/docs/reference/kapt.html#annotation-processor-arguments
            // https://mapstruct.org/documentation/stable/reference/html/#configuration-options
            // 注入spring 容器
            arg("mapstruct.defaultComponentModel", "spring")
        }
        keepJavacAnnotationProcessors = true
        correctErrorTypes = true
    }
}

fun Project.graalvmNativeInit() {
    apply(plugin = "org.graalvm.buildtools.native")
    extensions.configure<GraalVMExtension> {
        // 当安装了GraalVM时，自动检测工具链，如果没有安装GraalVM，会报错，所以这里关闭检测
        toolchainDetection.set(false)
        binaries {
            // 主程序入口
            named("main") {
                // 指定程序的主函数入口，SpringBoot可以不用写
                // mainClass = 'com.wshake.club.subject.SubjectApplicationKt'
                // 自动检测资源
                imageName = archivesBaseName

                resources.autodetect()
                mainClass = mainClassPath
                verbose = true
                fallback = true
                useFatJar = true
                //--initialize-at-build-time修复Log4j无法找到的问题，如果编的时候有其它Class找不到，但正常运行jar包又没问题，可以尝试把报错
                // 的Class加到这里，多个Class用逗号分隔，-H:+ReportExceptionStackTraces表示出错了打印堆栈
                buildArgs.addAll(
                    "--initialize-at-build-time=org.slf4j.LoggerFactory" +
                            ",ch.qos.logback.classic.Logger" +
                            ",ch.qos.logback.core.status.InfoStatus" +
                            ",ch.qos.logback.classic.Level" +
                            ",ch.qos.logback.core.util.Loader" +
                            ",ch.qos.logback.core.util.StatusPrinter" +
                            ",org.springframework.util.ConcurrentReferenceHashMap",

                    "--trace-object-instantiation=java.util.jar.JarFile" +
                            ",org.springframework.util.ConcurrentReferenceHashMap",
                    "-H:+ReportExceptionStackTraces",
                    "--no-fallback",

                    "--trace-class-initialization=org.slf4j.LoggerFactory" +
                            ",ch.qos.logback.classic.Logger" +
                            ",org.apache.commons.logging.LogAdapter" +
                            ",java.net.Inet4Address",

                    "--initialize-at-run-time=org.apache.commons.logging.LogAdapter"
                )
                // 关闭工具链检测，减少不必要的检测
                toolchainDetection = false

            }
            named("test") {
                buildArgs.add("-O0")
            }
        }
        binaries.all {
            buildArgs.add("--verbose")
        }
    }
}

// 代码测试覆盖率需要Jacoco插件
fun Project.jacocoInit() {
    apply(plugin = "jacoco")
    tasks {
        register<JacocoReport>("codeCoverageReport") {
            dependsOn(Test::class)

            executionData.setFrom(fileTree(project.rootDir.absolutePath) {
                include("**/build/jacoco/*.exec")
            })

            reports {
                xml.required.set(true)
                xml.outputLocation.set(file("${layout.buildDirectory}/reports/jacoco/report.xml"))
                html.required.set(false)
                csv.required.set(false)
            }
        }
    }
}

fun Project.springInit() {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
}

fun Project.dokkaInit() {
    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        apply(plugin = "org.jetbrains.dokka")

        tasks.named<DokkaTask>("dokkaHtml") {
            outputDirectory.set(rootProject.rootDir.resolve("docs/api"))
            val rootPath = rootProject.rootDir.toPath()
            val logoCss = rootPath.resolve("docs/css/logo-styles.css").toString().replace('\\', '/')
            val paletteSvg = rootPath.resolve("docs/img/wordmark_small_dark.svg").toString()
                .replace('\\', '/')
            pluginsMapConfiguration.set(
                mapOf(
                    "org.jetbrains.dokka.base.DokkaBase" to """{
                "customStyleSheets": ["$logoCss"],
                "customAssets": ["$paletteSvg"],
                "footerMessage": "Copyright &copy; 2021 AJ Alt"
            }"""
                )
            )
            dokkaSourceSets.configureEach {
                reportUndocumented.set(false)
                skipDeprecated.set(true)
            }
        }
    }
}