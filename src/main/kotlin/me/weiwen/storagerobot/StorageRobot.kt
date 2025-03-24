package me.weiwen.storagerobot

import dev.jorel.commandapi.*
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import me.weiwen.storagerobot.StorageManager.sendMessage
import me.weiwen.storagerobot.config.Config
import me.weiwen.storagerobot.config.parseConfig
import me.weiwen.storagerobot.managers.HighlightManager.clearDisplays
import me.weiwen.storagerobot.managers.HighlightManager.showItemDisplay
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.net.http.WebSocket


class StorageRobot : JavaPlugin(), WebSocket.Listener {
    companion object {
        lateinit var plugin: StorageRobot
            private set
        lateinit var metrics: Metrics
            private set
    }

    lateinit var config: Config

    override fun onLoad() {
        plugin = this

        CommandAPI.onLoad(CommandAPIBukkitConfig(plugin))
    }

    override fun onEnable() {
        config = parseConfig(this)
//        metrics = Metrics(this, 15850)

        CommandAPI.onEnable();
        registerCommands()

        logger.info("StorageRobot is enabled")
    }

    override fun onDisable() {
        logger.info("StorageRobot is disabled")
    }

    fun registerCommands() {
        CommandAPICommand("broadcastmsg")
            .withArguments(GreedyStringArgument("message")) // The arguments
            .withAliases("broadcast", "broadcastmessage") // Command aliases
            .withPermission(CommandPermission.OP) // Required permissions
            .executes(CommandExecutor { sender: CommandSender?, args: CommandArguments ->
                val message = args["message"] as String?
                Bukkit.getServer().broadcastMessage(message!!)
            })
            .register()

        commandTree("store") {
            withPermission("storagerobot.store")
            playerExecutor { sender, _ ->
                storeCommand(sender)
            }
        }

        commandTree("search") {
            withPermission("storagerobot.search")
            playerExecutor { sender, _ ->
                searchCommand(sender)
            }
        }

        logger.info("Registered commands.")
    }

    private fun storeCommand(
        player: Player,
    ) {
        val stored = StorageManager.storeInventory(player)
        sendMessage(player, stored)
        clearDisplays(player)
        for ((block, items) in stored) {
            for (item in items) {
                showItemDisplay(player, item, block)
            }
        }
    }

    private fun searchCommand(
        player: Player,
    ) {
        val item = player.inventory.itemInMainHand
        val found = StorageManager.search(player, item)
        sendMessage(player, found)
        clearDisplays(player)
        for ((block, items) in found) {
            for (item in items) {
                showItemDisplay(player, item, block)
            }
        }
    }
}
