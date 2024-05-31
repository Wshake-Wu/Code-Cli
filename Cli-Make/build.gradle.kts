plugins {
    kotlin("multiplatform")
    id("convention.publication")
}

mainClassPath = "com.wshake.cli.make.MainKt"
archivesBaseName = "ws"
version = ""

toolJvmInit()
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                project.dependencies.platform(libs.bom.kotlin)
                implementation(libs.bundles.tools)
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.freemarker)
                implementation(libs.mysql)
                implementation(libs.exposedCore)
                implementation(libs.exposedJdbc)
                implementation("com.github.ajalt.clikt:clikt:4.2.1")
                implementation("org.fusesource.jansi:jansi:2.4.0")
                implementation("org.jline:jline:3.25.0")

            }
        }
        jvmTest{
            dependencies{
                implementation(libs.test.kotlin)
            }
        }
    }
}
graalvmNativeInit()
tasks{
    named("installShadowDist") {
        doLast {
            copy {
                from("src/jvmMain/resources/templates")
                into("build/install/Cli-Make-shadow/lib/templates")
            }
        }
    }
}