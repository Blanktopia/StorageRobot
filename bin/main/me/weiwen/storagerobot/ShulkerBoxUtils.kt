package me.weiwen.storagerobot

import com.destroystokyo.paper.MaterialTags
import io.papermc.paper.datacomponent.DataComponentTypes
import me.weiwen.storagerobot.hooks.ShulkerPacksHook.isShulkerBoxOpen
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

fun moveFromShulkerBox(shulkerBoxItem: ItemStack, item: ItemStack): ItemStack {
    if (!MaterialTags.SHULKER_BOXES.isTagged(shulkerBoxItem)) return ItemStack.empty()

    val blockStateMeta = shulkerBoxItem.itemMeta as? BlockStateMeta ?: return ItemStack.empty()
    val shulkerBox = blockStateMeta.blockState as? ShulkerBox ?: return ItemStack.empty()
    if (isShulkerBoxOpen(shulkerBoxItem)) return ItemStack.empty()

    val itemToRemove = if (item.isEmpty) {
        shulkerBox.inventory.first { it != null && !it.isEmpty }
    } else {
        item
    }
    val originalAmount = itemToRemove.amount
    val couldntRemove = shulkerBox.inventory.removeItem(itemToRemove)[0]
    val remainder = couldntRemove?.amount ?: 0

    blockStateMeta.blockState = shulkerBox
    shulkerBoxItem.itemMeta = blockStateMeta

    itemToRemove.amount = originalAmount - remainder
    return itemToRemove
}

fun moveIntoShulkerBox(shulkerBoxItem: ItemStack, item: ItemStack): ItemStack {
    if (!MaterialTags.SHULKER_BOXES.isTagged(shulkerBoxItem)) return item
    if (item.isEmpty) return item

    val blockStateMeta = shulkerBoxItem.itemMeta as? BlockStateMeta ?: return item
    val shulkerBox = blockStateMeta.blockState as? ShulkerBox ?: return item
    if (isShulkerBoxOpen(shulkerBoxItem)) return item

    val couldntMove = shulkerBox.inventory.addItem(item)[0]
    val remainder = couldntMove?.amount ?: 0

    blockStateMeta.blockState = shulkerBox
    shulkerBoxItem.itemMeta = blockStateMeta

    item.amount = remainder
    return item
}

fun Inventory.firstShulkerBoxContaining(itemStack: ItemStack): ItemStack? {
    for (item in storageContents.filterNotNull()) {
        if (!MaterialTags.SHULKER_BOXES.isTagged(item)) continue

        val blockStateMeta = item.itemMeta as? BlockStateMeta ?: continue
        val shulkerBox = blockStateMeta.blockState as? ShulkerBox ?: continue
        if (isShulkerBoxOpen(item)) continue

        if (!shulkerBox.inventory.filterNotNull().any { it.matches(itemStack, true) }) {
            continue
        }

        return item
    }
    return null
}

fun ItemStack.matches(other: ItemStack, ignoreAmount: Boolean): Boolean {
    if (type != other.type) return false
    if (!ignoreAmount && amount != other.amount) return false

    if (getData(DataComponentTypes.ITEM_NAME) != other.getData(DataComponentTypes.ITEM_NAME)) return false
    if (getData(DataComponentTypes.LORE) != other.getData(DataComponentTypes.LORE)) return false
    if (getData(DataComponentTypes.CUSTOM_NAME) != other.getData(DataComponentTypes.CUSTOM_NAME)) return false
    if (getData(DataComponentTypes.CUSTOM_MODEL_DATA) != other.getData(DataComponentTypes.CUSTOM_MODEL_DATA)) return false
    if (getData(DataComponentTypes.ITEM_MODEL) != other.getData(DataComponentTypes.ITEM_MODEL)) return false
    if (getData(DataComponentTypes.CONTAINER) != other.getData(DataComponentTypes.CONTAINER)) return false
    if (getData(DataComponentTypes.BUNDLE_CONTENTS) != other.getData(DataComponentTypes.BUNDLE_CONTENTS)) return false

    return true
}