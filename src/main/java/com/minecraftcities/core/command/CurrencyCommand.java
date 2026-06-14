package com.minecraftcities.core.command;

import com.minecraftcities.core.config.CoreConfig;
import com.minecraftcities.core.currency.Currency;
import com.minecraftcities.core.currency.CurrencyManager;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CurrencyCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("currency")
            .requires(src -> src.hasPermission(CoreConfig.ADMIN_PERMISSION_LEVEL.get()))
            .then(buildGive())
            .then(buildTake())
            .then(buildSet())
            .then(buildCheck());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildGive() {
        return Commands.literal("give")
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("currency", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        for (Currency c : Currency.values()) builder.suggest(c.name().toLowerCase());
                        return builder.buildFuture();
                    })
                    .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            Currency currency = parseCurrency(StringArgumentType.getString(ctx, "currency"));
                            if (currency == null) {
                                ctx.getSource().sendFailure(Component.translatable("minecraftcitiescore.currency.unknown"));
                                return 0;
                            }
                            long amount = LongArgumentType.getLong(ctx, "amount");
                            CurrencyManager.add(target, currency, amount);
                            ctx.getSource().sendSuccess(() -> Component.translatable("minecraftcitiescore.currency.give.success", amount, currency.name(), target.getName()), true);
                            return 1;
                        })
                    )
                )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildTake() {
        return Commands.literal("take")
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("currency", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        for (Currency c : Currency.values()) builder.suggest(c.name().toLowerCase());
                        return builder.buildFuture();
                    })
                    .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            Currency currency = parseCurrency(StringArgumentType.getString(ctx, "currency"));
                            if (currency == null) {
                                ctx.getSource().sendFailure(Component.translatable("minecraftcitiescore.currency.unknown"));
                                return 0;
                            }
                            long amount = LongArgumentType.getLong(ctx, "amount");
                            CurrencyManager.subtract(target, currency, amount);
                            ctx.getSource().sendSuccess(() -> Component.translatable("minecraftcitiescore.currency.take.success", amount, currency.name(), target.getName()), true);
                            return 1;
                        })
                    )
                )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildSet() {
        return Commands.literal("set")
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("currency", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        for (Currency c : Currency.values()) builder.suggest(c.name().toLowerCase());
                        return builder.buildFuture();
                    })
                    .then(Commands.argument("amount", LongArgumentType.longArg(0))
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            Currency currency = parseCurrency(StringArgumentType.getString(ctx, "currency"));
                            if (currency == null) {
                                ctx.getSource().sendFailure(Component.translatable("minecraftcitiescore.currency.unknown"));
                                return 0;
                            }
                            long amount = LongArgumentType.getLong(ctx, "amount");
                            CurrencyManager.set(target, currency, amount);
                            ctx.getSource().sendSuccess(() -> Component.translatable("minecraftcitiescore.currency.set.success", target.getName(), currency.name(), amount), true);
                            return 1;
                        })
                    )
                )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCheck() {
        return Commands.literal("check")
            .then(Commands.argument("player", EntityArgument.player())
                .executes(ctx -> {
                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                    ctx.getSource().sendSuccess(() -> Component.translatable("minecraftcitiescore.currency.check.header", target.getName()), false);
                    for (Currency c : Currency.values()) {
                        final Currency fc = c;
                        final long bal = CurrencyManager.getBalance(target, c);
                        ctx.getSource().sendSuccess(() -> Component.translatable("minecraftcitiescore.currency.check.entry", fc.name(), bal), false);
                    }
                    return 1;
                })
            );
    }

    private static Currency parseCurrency(String name) {
        try {
            return Currency.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
