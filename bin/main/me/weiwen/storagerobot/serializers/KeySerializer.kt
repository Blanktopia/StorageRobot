package me.weiwen.storagerobot.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.key.Key

class KeySerializer : KSerializer<Key> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Key", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Key {
        val string = decoder.decodeString()
        return Key.key(string)
    }

    override fun serialize(encoder: Encoder, value: Key) {
        val string = value.asMinimalString()
        return encoder.encodeString(string)
    }
}
