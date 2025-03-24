package me.weiwen.storagerobot.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Messages(
    val block: String = "<newline><bold><gold><block></gold></bold>   <gray><bold>X:</bold> <white><x></white> <bold>Y:</bold> <white><y></white> <bold>Z:</bold> <white><z></white></gray><newline>",
    val item: String = "<gray>  - <amount> <gold><item></gold></gray><newline>"
)