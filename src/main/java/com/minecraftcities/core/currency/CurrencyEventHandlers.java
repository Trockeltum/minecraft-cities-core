package com.minecraftcities.core.currency;

import com.minecraftcities.core.MinecraftCitiesCore;
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
    }
}
