package com.minecraftcities.core.earn;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EarnActivityTracker {

    private static final Map<UUID, Long> blocksMined = new HashMap<>();
    private static final Map<UUID, Long> mobsKilled = new HashMap<>();
    private static final Map<UUID, double[]> lastPosition = new HashMap<>();
    private static final Map<UUID, Double> distanceWalked = new HashMap<>();

    public static void onBlockMined(UUID playerId) {
        blocksMined.merge(playerId, 1L, Long::sum);
    }

    public static void onMobKilled(UUID playerId) {
        mobsKilled.merge(playerId, 1L, Long::sum);
    }

    public static void onPlayerTick(ServerPlayer player) {
        UUID id = player.getUUID();
        double[] last = lastPosition.get(id);
        double[] curr = {player.getX(), player.getZ()};
        if (last != null) {
            double dx = curr[0] - last[0];
            double dz = curr[1] - last[1];
            double dist = Math.sqrt(dx * dx + dz * dz);
            distanceWalked.merge(id, dist, Double::sum);
        }
        lastPosition.put(id, curr);
    }

    public static long consumeBlocksMined(UUID playerId) {
        Long v = blocksMined.remove(playerId);
        return v != null ? v : 0L;
    }

    public static long consumeMobsKilled(UUID playerId) {
        Long v = mobsKilled.remove(playerId);
        return v != null ? v : 0L;
    }

    public static double consumeDistanceWalked(UUID playerId) {
        Double d = distanceWalked.remove(playerId);
        return d != null ? d : 0.0;
    }

    public static void forget(UUID playerId) {
        blocksMined.remove(playerId);
        mobsKilled.remove(playerId);
        lastPosition.remove(playerId);
        distanceWalked.remove(playerId);
    }
}
