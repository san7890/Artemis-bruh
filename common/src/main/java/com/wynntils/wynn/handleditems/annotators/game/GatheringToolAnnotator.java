/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.handleditems.items.game.GatheringToolItem;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.objects.profiles.ToolProfile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class GatheringToolAnnotator implements ItemAnnotator {
    private static final Pattern GATHERING_TOOL_PATTERN =
            Pattern.compile("[ⒸⒷⓀⒿ] Gathering (Axe|Rod|Scythe|Pickaxe) T(\\d+)");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        Matcher matcher = GATHERING_TOOL_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        String toolType = matcher.group(1);
        int tier = Integer.parseInt(matcher.group(2));

        ToolProfile toolProfile = ToolProfile.fromString(toolType, tier);
        if (toolProfile == null) return null;

        CappedValue durability = WynnItemMatchers.getDurability(itemStack);

        return new GatheringToolItem(toolProfile, durability);
    }
}
