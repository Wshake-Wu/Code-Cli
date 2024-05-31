rootInit()
val allLibs=libs
allprojects{
    buildscript {
        dependencies {
            classpath(allLibs.bundles.plugin)
        }
    }
}