package com.minecraftcities.core.earn;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EarnActivityTracker {

    private static final Map<UUID, Long> blocksMined = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> mobsKilled = new ConcurrentHashMap<>();
    private static final Map<UUID, double[]> lastPosition = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> distanceWalked = new ConcurrentHashMap<>();

    // Rolling earn history: each entry is [timestampMs, goldEarned]
    private static final Map<UUID, Deque<long[]>> earnHistory = new ConcurrentHashMap<>();
    private static final long ONE_HOUR_MS = 60 * 60 * 1000L;

    public static void recordEarn(UUID playerId, long goldEarned) {
        earnHistory.computeIfAbsent(playerId, k -> new ArrayDeque<>())
                .addLast(new long[]{System.currentTimeMillis(), goldEarned});
    }

    /** Returns the rolling average gold earned per minute over the last 60 minutes. */
    public static long computeRatePerMinute(UUID playerId) {
        Deque<long[]> history = earnHistory.get(playerId);
        if (history == null || history.isEmpty()) return 0;
        long cutoff = System.currentTimeMillis() - ONE_HOUR_MS;
        history.removeIf(e -> e[0] < cutoff);
        if (history.isEmpty()) return 0;
        long total = 0;
        for (long[] e : history) total += e[1];
        return total / 60;
    }

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
        earnHistory.remove(playerId);
    }
}
