/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.commands.CommandBase;
import com.wynntils.core.managers.UpdateManager;
import com.wynntils.mc.utils.McUtils;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class UpdateCommand extends CommandBase {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("update").executes(this::update);
    }

    private int update(CommandContext<CommandSourceStack> context) {
        if (WynntilsMod.isDevelopmentEnvironment()) {
            context.getSource()
                    .sendFailure(new TextComponent("Development environment detected, cannot update!")
                            .withStyle(ChatFormatting.DARK_RED));
            WynntilsMod.error("Development environment detected, cannot update!");
            return 0;
        }

        CompletableFuture.runAsync(() -> {
            WynntilsMod.info("Attempting to fetch Wynntils update.");
            CompletableFuture<UpdateManager.UpdateResult> completableFuture = UpdateManager.tryUpdate();

            completableFuture.whenComplete((result, throwable) -> {
                switch (result) {
                    case SUCCESSFUL:
                        McUtils.sendMessageToClient(new TextComponent(
                                        "Successfully downloaded Wynntils/Artemis update. It will apply on shutdown.")
                                .withStyle(ChatFormatting.DARK_GREEN));
                        break;
                    case ERROR:
                        McUtils.sendMessageToClient(new TextComponent("Error applying Wynntils/Artemis update.")
                                .withStyle(ChatFormatting.DARK_RED));
                        break;
                    case ALREADY_ON_LATEST:
                        McUtils.sendMessageToClient(new TextComponent("Wynntils/Artemis is already on latest version.")
                                .withStyle(ChatFormatting.YELLOW));
                        return;
                }
            });

            context.getSource()
                    .sendSuccess(new TextComponent("Downloading update!").withStyle(ChatFormatting.GREEN), false);
        });

        return 1;
    }
}
