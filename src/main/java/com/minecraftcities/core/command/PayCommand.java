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

public class PayCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("pay")
            .requires(CommandSourceStack::isPlayer)
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                    // Admin-only branch: /pay <player> <amount> <type>
                    .then(Commands.argument("type", StringArgumentType.word())
                        .requires(src -> src.hasPermission(CoreConfig.ADMIN_PERMISSION_LEVEL.get()))
                        .suggests((ctx, builder) -> {
                            for (Currency c : Currency.values()) builder.suggest(c.name().toLowerCase());
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            ServerPlayer sender = ctx.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            long amount = LongArgumentType.getLong(ctx, "amount");
                            Currency currency = parseCurrency(StringArgumentType.getString(ctx, "type"));
                            if (currency == null) {
                                ctx.getSource().sendFailure(Component.translatable("minecraftcitiescore.currency.unknown"));
                                return 0;
                            }
                            return executePay(ctx.getSource(), sender, target, amount, currency, true);
                        })
                    )
                    // Default branch: /pay <player> <amount>  (Gold, self-pay blocked for non-admins)
                    .executes(ctx -> {
                        ServerPlayer sender = ctx.getSource().getPlayerOrException();
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        long amount = LongArgumentType.getLong(ctx, "amount");
                        boolean isAdmin = ctx.getSource().hasPermission(CoreConfig.ADMIN_PERMISSION_LEVEL.get());
                        return executePay(ctx.getSource(), sender, target, amount, Currency.GOLD, isAdmin);
                    })
                )
            );
    }

    private static int executePay(CommandSourceStack src, ServerPlayer sender, ServerPlayer target,
                                  long amount, Currency currency, boolean allowSelf) {
        if (!allowSelf && sender.getUUID().equals(target.getUUID())) {
            src.sendFailure(Component.translatable("minecraftcitiescore.pay.self"));
            return 0;
        }

        boolean ok = CurrencyManager.transfer(sender, target, currency, amount);
        if (!ok) {
            src.sendFailure(Component.translatable("minecraftcitiescore.pay.insufficient"));
            return 0;
        }

        sender.sendSystemMessage(Component.translatable("minecraftcitiescore.pay.sent", amount, target.getName()));
        target.sendSystemMessage(Component.translatable("minecraftcitiescore.pay.received", amount, sender.getName()));
        return 1;
    }

    private static Currency parseCurrency(String name) {
        try {
            return Currency.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
