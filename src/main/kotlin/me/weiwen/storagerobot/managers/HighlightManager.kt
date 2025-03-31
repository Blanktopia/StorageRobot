package me.weiwen.storagerobot.managers

import me.weiwen.storagerobot.StorageRobot.Companion.plugin
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import kotlin.math.absoluteValue

object HighlightManager {
    val displays: MutableMap<Player, MutableMap<Location, ArrayDeque<Display>>> = mutableMapOf()

    fun clearDisplays(player: Player) {
        val displayLocations = displays[player] ?: return
        for ((_, displayList) in displayLocations) {
            for (display in displayList) {
                if (display.isValid) {
                    display.remove()
                }
            }
        }
        displayLocations.clear()
    }

    fun showItemDisplay(player: Player, item: ItemStack, block: Block) {
        val deque = displays.getOrPut(player) { mutableMapOf() }.getOrPut(block.location) { ArrayDeque() }
        val facing = if (player.pitch > 45.0) BlockFace.DOWN else if (player.pitch < -45.0) BlockFace.UP else player.facing
        val location = (deque.lastOrNull()?.location?.add(facing.direction.multiply(-0.1)) ?: block.location
            .add(0.5, 0.5, 0.5)
            .add(facing.direction.multiply(-0.51)))

        val display = block.world.spawnEntity(
            location,
            EntityType.ITEM_DISPLAY
        ) as ItemDisplay
        display.apply {
            isVisibleByDefault = false
            isPersistent = false
            billboard = Display.Billboard.FIXED
            brightness = Display.Brightness(15, 15)
            setItemStack(item)
            isGlowing = true
            itemDisplayTransform = ItemDisplay.ItemDisplayTransform.NONE
            transformation = Transformation(
                transformation.translation,
                transformation.leftRotation,
                Vector(1.0, 1.0, 1.0).subtract(
                    Vector(
                        facing.direction.x.absoluteValue,
                        facing.direction.y.absoluteValue,
                        facing.direction.z.absoluteValue,
                    )
                ).multiply(0.5).toVector3f(),
                transformation.rightRotation,
            )
        }
        player.showEntity(plugin, display)
        deque.add(display)

        plugin.server.scheduler.runTaskLater(plugin, { ->
            displays[player]?.get(block.location)?.remove(display)
            if (display.isValid) {
                display.remove()
            }
        }, 10 * 20)
    }
}