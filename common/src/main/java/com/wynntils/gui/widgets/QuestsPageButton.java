/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.screens.WynntilsQuestBookScreen;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.network.chat.Component;

public class QuestsPageButton extends WynntilsButton {
    public QuestsPageButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Quests Page Button"));
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.QUESTS_BUTTON.resource(),
                this.getX(),
                this.getY(),
                this.width,
                this.height,
                Texture.QUESTS_BUTTON.width(),
                Texture.QUESTS_BUTTON.height());
    }

    @Override
    public void onPress() {
        McUtils.mc().setScreen(WynntilsQuestBookScreen.create());
    }
}
