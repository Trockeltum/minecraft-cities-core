package com.minecraftcities.core;

import com.minecraftcities.core.config.CoreConfig;
import com.minecraftcities.core.currency.CurrencyAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;

@Mod(MinecraftCitiesCore.MOD_ID)
public class MinecraftCitiesCore {

    public static final String MOD_ID = "minecraftcitiescore";

    public MinecraftCitiesCore(IEventBus modBus, ModContainer container) {
        CurrencyAttachments.ATTACHMENT_TYPES.register(modBus);
        container.registerConfig(ModConfig.Type.COMMON, CoreConfig.SPEC);
    }
}
