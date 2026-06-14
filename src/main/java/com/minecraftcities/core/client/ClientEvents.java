package com.minecraftcities.core.client;

import com.minecraftcities.core.MinecraftCitiesCore;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = MinecraftCitiesCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onInventoryInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        // The recipe book button in vanilla is at guiLeft - 22, guiTop + 46
        // We place our wallet button just below it
        int x = screen.getGuiLeft() - 22;
        int y = screen.getGuiTop() + 66; // 20px below recipe book

        Button walletButton = Button.builder(
                Component.literal("💰"),
                b -> screen.getMinecraft().setScreen(new WalletScreen())
        )
        .bounds(x, y, 20, 20)
        .build();

        event.addListener(walletButton);
    }
}
