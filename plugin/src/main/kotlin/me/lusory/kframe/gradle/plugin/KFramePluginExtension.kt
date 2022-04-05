package me.lusory.kframe.gradle.plugin

abstract class KFramePluginExtension {
    var mainFQClassName: String
        get() = "${mainPackageName}.${mainClassName}"
        set(value) {
            mainPackageName = value.substringBeforeLast('.')
            mainClassName = value.substringAfterLast('.')
        }
    var mainPackageName: String = "kframe"
    var mainClassName: String = "Main"
}