package com.minecraftcities.core.currency;

import com.minecraftcities.core.MinecraftCitiesCore;
import com.minecraftcities.core.config.CoreConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = MinecraftCitiesCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CurrencyEventHandlers {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        PlayerCurrencyData original = event.getOriginal().getData(CurrencyAttachments.CURRENCY_DATA.get());
        PlayerCurrencyData newData = event.getEntity().getData(CurrencyAttachments.CURRENCY_DATA.get());
        newData.set(Currency.GOLD, original.get(Currency.GOLD));
        newData.set(Currency.CITY, original.get(Currency.CITY));
        newData.set(Currency.PREMIUM, original.get(Currency.PREMIUM));
        // Transaction history intentionally not copied — it's a view, not a balance
        event.getEntity().setData(CurrencyAttachments.CURRENCY_DATA.get(), newData);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!CoreConfig.STARTER_CITY_TOKEN_ENABLED.get()) return;

        PlayerCurrencyData data = player.getData(CurrencyAttachments.CURRENCY_DATA.get());
        if (data.isStarterGranted()) return;

        data.markStarterGranted();
        data.set(Currency.CITY, data.get(Currency.CITY) + CoreConfig.STARTER_CITY_TOKEN_AMOUNT.get());
        player.setData(CurrencyAttachments.CURRENCY_DATA.get(), data);
    }
}
