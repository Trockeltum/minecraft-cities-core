package com.minecraftcities.core.command;

import com.minecraftcities.core.MinecraftCitiesCore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = MinecraftCitiesCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CommandRegistrar {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(PayCommand.register());
        event.getDispatcher().register(CurrencyCommand.register());
    }
}
