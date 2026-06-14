package com.minecraftcities.core.earn;

import com.minecraftcities.core.MinecraftCitiesCore;
import com.minecraftcities.core.config.CoreConfig;
import com.minecraftcities.core.currency.Currency;
import com.minecraftcities.core.currency.CurrencyAttachments;
import com.minecraftcities.core.currency.CurrencyManager;
import com.minecraftcities.core.currency.PlayerCurrencyData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = MinecraftCitiesCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveEarnScheduler {

    private static long tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!CoreConfig.EARN_ENABLED.get()) return;
        tickCounter++;
        long intervalTicks = CoreConfig.EARN_TICK_INTERVAL_SECONDS.get() * 20L;
        if (intervalTicks <= 0) return;
        if (tickCounter % intervalTicks != 0) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            long goldEarned = 0;

            if (CoreConfig.EARN_MINING_ENABLED.get()) {
                long blocks = EarnActivityTracker.consumeBlocksMined(player.getUUID());
                goldEarned += blocks * CoreConfig.EARN_MINING_GOLD_PER_BLOCK.get();
            }
            if (CoreConfig.EARN_MOB_KILL_ENABLED.get()) {
                long kills = EarnActivityTracker.consumeMobsKilled(player.getUUID());
                goldEarned += kills * CoreConfig.EARN_MOB_KILL_GOLD_PER_KILL.get();
            }
            if (CoreConfig.EARN_WALKING_ENABLED.get()) {
                double dist = EarnActivityTracker.consumeDistanceWalked(player.getUUID());
                goldEarned += (long)(dist / 100.0) * CoreConfig.EARN_WALKING_GOLD_PER_100_BLOCKS.get();
            }

            if (goldEarned > 0) {
                CurrencyManager.add(player, Currency.GOLD, goldEarned);
                EarnActivityTracker.recordEarn(player.getUUID(), goldEarned);
            }

            // Update synced earn rate regardless (rate decays as old entries age out)
            long rate = EarnActivityTracker.computeRatePerMinute(player.getUUID());
            PlayerCurrencyData data = player.getData(CurrencyAttachments.CURRENCY_DATA.get());
            data.setEarnRatePerMinute(rate);
            player.setData(CurrencyAttachments.CURRENCY_DATA.get(), data);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer sp) {
            EarnActivityTracker.onBlockMined(sp.getUUID());
        }
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer sp) {
            EarnActivityTracker.onMobKilled(sp.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            EarnActivityTracker.onPlayerTick(sp);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        EarnActivityTracker.forget(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        tickCounter = 0;
    }
}
