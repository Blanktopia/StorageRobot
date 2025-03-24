@file:UseSerializers(KeySerializer::class)

package me.weiwen.storagerobot.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.*
import me.weiwen.storagerobot.StorageRobot
import me.weiwen.storagerobot.serializers.KeySerializer
import net.kyori.adventure.key.Key
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Level

const val CONFIG_VERSION = "1.0.0"

@Serializable
data class Config(
    @SerialName("config-version")
    var configVersion: String = "1.0.0",

    @SerialName("allowed-containers")
    var allowedContainers: Set<Key> = setOf(
        Key.key("chest"),
        Key.key("trapped_chest"),
        Key.key("barrel"),
        Key.key("shulker_box"),
        Key.key("white_shulker_box"),
        Key.key("light_gray_shulker_box"),
        Key.key("gray_shulker_box"),
        Key.key("black_shulker_box"),
        Key.key("brown_shulker_box"),
        Key.key("red_shulker_box"),
        Key.key("orange_shulker_box"),
        Key.key("yellow_shulker_box"),
        Key.key("lime_shulker_box"),
        Key.key("green_shulker_box"),
        Key.key("cyan_shulker_box"),
        Key.key("light_blue_shulker_box"),
        Key.key("blue_shulker_box"),
        Key.key("purple_shulker_box"),
        Key.key("magenta_shulker_box"),
        Key.key("pink_shulker_box"),
    ),

    var messages: Messages = Messages(),
)

fun parseConfig(plugin: StorageRobot): Config {
    val file = File(plugin.dataFolder, "config.yml")

    if (!file.exists()) {
        plugin.logger.log(Level.INFO, "Config file not found, creating default")
        plugin.dataFolder.mkdirs()
        file.createNewFile()
        file.writeText(Yaml().encodeToString(Config()))
    }

    val config = try {
        Yaml().decodeFromString(file.readText())
    } catch (e: Exception) {
        plugin.logger.log(Level.SEVERE, e.message)
        Config()
    }

    if (config.configVersion != CONFIG_VERSION) {
        config.configVersion = CONFIG_VERSION
        plugin.logger.log(Level.INFO, "Updating config")
        file.writeText(Yaml().encodeToString(plugin.config))
    }

    return config
}
