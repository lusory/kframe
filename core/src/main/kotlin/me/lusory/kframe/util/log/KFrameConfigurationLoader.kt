package me.lusory.kframe.util.log

import org.tinylog.configuration.PropertiesConfigurationLoader

internal class KFrameConfigurationLoader : PropertiesConfigurationLoader() {
    companion object {
        // order sensitive, first has more priority
        val CONFIG_FILES: Array<String> = arrayOf(
            "tinylog-dev.properties",
            "tinylog-test.properties",
            "tinylog.properties",
            "tinylog-internal.properties"
        )
    }

    override fun getConfigurationFiles(): Array<String> = CONFIG_FILES
}