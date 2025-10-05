package me.weiwen.storagerobot.hooks

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object ShulkerPacksHook {
    private val checkIfOpenMethod: Method? by lazy {
        if (Bukkit.getPluginManager().getPlugin("ShulkerPacks") == null) {
            return@lazy null
        }
        val method = Class.forName("me.darkolythe.shulkerpacks.ShulkerListener").getDeclaredMethod(
            "checkIfOpen",
            ItemStack::class.java
        )
        method.isAccessible = true
        method
    }

    fun isShulkerBoxOpen(item: ItemStack?): Boolean {
        return try {
            checkIfOpenMethod?.invoke(null, item) == true
        } catch (ignored: IllegalAccessException) {
            false
        } catch (ignored: InvocationTargetException) {
            false
        }
    }
}