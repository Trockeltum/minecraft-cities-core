package com.minecraftcities.core.command;

import com.minecraftcities.core.config.CoreConfig;
import com.minecraftcities.core.currency.Currency;
import com.minecraftcities.core.currency.CurrencyManager;
import com.mojang.brigadier.arguments.LongArgumentType;
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
                    .executes(ctx -> {
                        ServerPlayer sender = ctx.getSource().getPlayerOrException();
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        long amount = LongArgumentType.getLong(ctx, "amount");
                        boolean isAdmin = ctx.getSource().hasPermission(CoreConfig.ADMIN_PERMISSION_LEVEL.get());

                        if (!isAdmin && sender.getUUID().equals(target.getUUID())) {
                            ctx.getSource().sendFailure(Component.translatable("minecraftcitiescore.pay.self"));
                            return 0;
                        }

                        boolean ok = CurrencyManager.transfer(sender, target, Currency.GOLD, amount);
                        if (!ok) {
                            ctx.getSource().sendFailure(Component.translatable("minecraftcitiescore.pay.insufficient"));
                            return 0;
                        }

                        sender.sendSystemMessage(Component.translatable("minecraftcitiescore.pay.sent", amount, target.getName()));
                        target.sendSystemMessage(Component.translatable("minecraftcitiescore.pay.received", amount, sender.getName()));
                        return 1;
                    })
                )
            );
    }
}
