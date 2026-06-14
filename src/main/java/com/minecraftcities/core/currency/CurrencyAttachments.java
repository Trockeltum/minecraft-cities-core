package com.minecraftcities.core.currency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import com.minecraftcities.core.MinecraftCitiesCore;

import java.util.function.Supplier;

public class CurrencyAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MinecraftCitiesCore.MOD_ID);

    // Codec for persistence (saved to disk)
    private static final Codec<PlayerCurrencyData> CURRENCY_CODEC =
        RecordCodecBuilder.create(inst -> inst.group(
            Codec.LONG.fieldOf("gold").forGetter(d -> d.get(Currency.GOLD)),
            Codec.LONG.fieldOf("city").forGetter(d -> d.get(Currency.CITY)),
            Codec.LONG.fieldOf("premium").forGetter(d -> d.get(Currency.PREMIUM))
        ).apply(inst, (gold, city, premium) -> {
            PlayerCurrencyData data = new PlayerCurrencyData();
            data.set(Currency.GOLD, gold);
            data.set(Currency.CITY, city);
            data.set(Currency.PREMIUM, premium);
            return data;
        }));

    // StreamCodec for network sync (client only gets balances, not history)
    private static final StreamCodec<ByteBuf, PlayerCurrencyData> CURRENCY_STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, d -> d.get(Currency.GOLD),
            ByteBufCodecs.VAR_LONG, d -> d.get(Currency.CITY),
            ByteBufCodecs.VAR_LONG, d -> d.get(Currency.PREMIUM),
            (gold, city, premium) -> {
                PlayerCurrencyData data = new PlayerCurrencyData();
                data.set(Currency.GOLD, gold);
                data.set(Currency.CITY, city);
                data.set(Currency.PREMIUM, premium);
                return data;
            });

    public static final Supplier<AttachmentType<PlayerCurrencyData>> CURRENCY_DATA =
        ATTACHMENT_TYPES.register("currency_data", () ->
            AttachmentType.builder(PlayerCurrencyData::new)
                .serialize(CURRENCY_CODEC)
                .sync((holder, to) -> holder == to, CURRENCY_STREAM_CODEC)
                .build());
}
