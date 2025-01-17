/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Models;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.handleditems.items.game.HorseItem;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class HorseManager extends Manager {
    public HorseManager() {
        super(List.of());
    }

    public HorseItem getHorse() {
        int horseSlot = findHorseSlotNum();
        if (horseSlot == -1) return null;

        return getHorseItem(McUtils.inventory().getItem(horseSlot));
    }

    public int findHorseSlotNum() {
        Inventory inventory = McUtils.inventory();
        for (int slotNum = 0; slotNum <= 44; slotNum++) {
            ItemStack stack = inventory.getItem(slotNum);
            if (getHorseItem(stack) != null) {
                return slotNum;
            }
        }
        return -1;
    }

    public HorseItem getHorseItem(ItemStack itemStack) {
        Optional<HorseItem> horseOpt = Models.Item.asWynnItem(itemStack, HorseItem.class);
        if (horseOpt.isEmpty()) return null;

        return horseOpt.get();
    }

    public AbstractHorse searchForHorseNearby(int searchRadius) {
        Player player = McUtils.player();
        List<AbstractHorse> horses = McUtils.mc()
                .level
                .getEntitiesOfClass(
                        AbstractHorse.class,
                        new AABB(
                                player.getX() - searchRadius,
                                player.getY() - searchRadius,
                                player.getZ() - searchRadius,
                                player.getX() + searchRadius,
                                player.getY() + searchRadius,
                                player.getZ() + searchRadius));

        return horses.stream()
                .filter(horse -> isPlayersHorse(horse, player))
                .findFirst()
                .orElse(null);
    }

    private boolean isPlayersHorse(AbstractHorse horse, Player player) {
        if (horse == null) return false;
        Component horseName = horse.getCustomName();
        if (horseName == null) return false;

        String playerName = player.getName().getString();
        String defaultName = "§f" + playerName + "§7" + "'s horse";
        String customNameSuffix = "§7" + " [" + playerName + "]";
        return defaultName.equals(horseName.getString())
                || horseName.getString().endsWith(customNameSuffix);
    }
}
