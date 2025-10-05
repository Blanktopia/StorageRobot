package me.weiwen.storagerobot

import me.weiwen.storagerobot.StorageRobot.Companion.plugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object StorageManager {
    private fun containersInRadius(location: Location, radius: Int): List<Container> {
        val minChunkX = Math.floorDiv(location.blockX - radius, 16)
        val maxChunkX = Math.floorDiv(location.blockX + radius, 16)
        val minChunkZ = Math.floorDiv(location.blockZ - radius, 16)
        val maxChunkZ = Math.floorDiv(location.blockZ + radius, 16)

        val containers = mutableListOf<Container>()
        for (x in minChunkX..maxChunkX) {
            for (z in minChunkZ..maxChunkZ) {
                containers.addAll(
                    location.world.getChunkAt(x, z)
                        .tileEntities
                        .filterIsInstance<Container>()
                        .filter { it.location.distanceSquared(location) <= radius * radius }
                )
            }
        }
        containers.sortBy { it.location.distanceSquared(location) }
        return containers
    }

    private fun Container.isTrusted(player: Player): Boolean {
        val event = PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, null, block, BlockFace.UP)
        Bukkit.getPluginManager().callEvent(event);
        return event.useInteractedBlock() != Event.Result.DENY
    }

    private fun mergeItems(items: List<Pair<ItemStack, Block>>): Map<Block, List<ItemStack>> {
        val mergedItems = mutableListOf<Pair<ItemStack, Block>>()
        for ((item, block) in items) {
            val i = mergedItems.indexOfFirst {
                it.first.matches(item, true) && it.second == block
            }
            if (i == -1) {
                mergedItems += item.clone() to block
                continue
            }
            mergedItems[i] = mergedItems[i].first.apply { amount += item.amount } to mergedItems[i].second
        }
        return mergedItems
            .groupBy({ it.second }, { it.first })
    }

    fun search(player: Player, query: String, radius: Int = 20): Map<Block, List<ItemStack>> {
        val containers = containersInRadius(player.location, radius)
            .filter { it.isTrusted(player) }

        val found = mutableListOf<Pair<ItemStack, Block>>()

        val query = query.lowercase()

        for (container in containers) {
            val items = container.inventory
                .filterNotNull()
                .filter { !it.isEmpty && PlainTextComponentSerializer.plainText().serialize(it.effectiveName()).lowercase().contains(query) }
                .map { it to container.block }
            found.addAll(items)
        }

        return mergeItems(found)
    }

    fun search(player: Player, item: ItemStack, radius: Int = 20): Map<Block, List<ItemStack>> {
        val containers = containersInRadius(player.location, radius)
            .filter { it.isTrusted(player) }

        val found = mutableListOf<Pair<ItemStack, Block>>()

        for (container in containers) {
            if (container.inventory.filterNotNull().filter { !it.isEmpty }.any { it.matches(item, true) }) {
                found.add(item to container.block)
                continue
            }
        }

        return mergeItems(found)
    }

    fun storeInventory(player: Player, radius: Int = 20): Map<Block, List<ItemStack>> {
        val containers = containersInRadius(player.location, radius)
            .filter { plugin.config.allowedContainers.contains(it.block.type.key) }
            .filter { it.isTrusted(player) }

        var toStore = player.inventory.storageContents
            .filterNotNull()
            .filter { !it.isEmpty && !plugin.config.blacklistedItems.contains(it.type.key()) }
            .toList()
        val stored = mutableListOf<Pair<ItemStack, Block>>()

        for (container in containers) {
            val nextToStore = mutableListOf<ItemStack>()
            for (item in toStore) {
                if (!container.inventory.filterNotNull().filter { !it.isEmpty }.any { it.matches(item, true) }) {
                    nextToStore.add(item)
                    continue
                }

                val leftovers = container.inventory.addItem(item)
                val leftover = leftovers.getOrDefault(0, ItemStack.empty())
                if (leftovers != item) {
                    val storedItem = item.clone().apply { amount -= leftover.amount }
                    if (!storedItem.isEmpty) {
                        stored.add(storedItem to container.block)
                    }
                }

                if (leftovers.size == 0) continue
                nextToStore.addAll(leftovers.values)
            }
            toStore = nextToStore
        }

        for (item in stored) {
            player.inventory.removeItem(item.first)
        }

        return mergeItems(stored)
    }

    fun sendMessage(player: Player, blocks: Map<Block, List<ItemStack>>) {
        var component = Component.text("")
        for ((block, items) in blocks) {
            component = component.append(MiniMessage.miniMessage().deserialize(
                plugin.config.messages.block,
                Placeholder.component("block", block.type.blockTranslationKey?.let { Component.translatable(it) } ?: Component.text("Block")),
                Placeholder.component("x", Component.text(block.location.blockX)),
                Placeholder.component("y", Component.text(block.location.blockY)),
                Placeholder.component("z", Component.text(block.location.blockZ))
            ))
            for (item in items) {
                component = component.append(MiniMessage.miniMessage().deserialize(
                    plugin.config.messages.item,
                    Placeholder.component("item", item.effectiveName()),
                    Placeholder.component("amount", Component.text(item.amount)),
                ))
            }
        }
        player.sendMessage(component)
    }
}