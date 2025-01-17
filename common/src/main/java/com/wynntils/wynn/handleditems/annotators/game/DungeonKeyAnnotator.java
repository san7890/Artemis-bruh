/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wynn.handleditems.items.game.DungeonKeyItem;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class DungeonKeyAnnotator implements ItemAnnotator {
    private static final Pattern DUNGEON_KEY_PATTERN = Pattern.compile("(?:§.)*(?:Broken )?(?:Corrupted )?(.+) Key");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        Matcher keyMatcher = DUNGEON_KEY_PATTERN.matcher(name);
        if (!keyMatcher.matches()) return null;

        if (!verifyDungeonKey(itemStack)) return null;

        String dungeonName = keyMatcher.group();

        String dungeon = Arrays.stream(keyMatcher.group(1).split(" ", 2))
                .map(s -> s.substring(0, 1))
                .collect(Collectors.joining());

        boolean corrupted = dungeonName.contains("Corrupted") || dungeonName.contains("Broken");

        return new DungeonKeyItem(dungeon, corrupted);
    }

    private boolean verifyDungeonKey(ItemStack itemStack) {
        for (Component line : ItemUtils.getTooltipLines(itemStack)) {
            // check lore to avoid matching misc. key items
            if (line.getString().contains("Dungeon Info")) return true;
            if (line.getString().contains("Corrupted Dungeon Key")) return true;
        }
        return false;
    }
}
