/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.guides;

import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.objects.EmeraldSymbols;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

public class GuideEmeraldPouchItemStack extends GuideItemStack {
    private final int tier;

    private final List<Component> generatedTooltip;

    public GuideEmeraldPouchItemStack(int tier) {
        super(new ItemStack(Items.DIAMOND_AXE));
        this.setDamageValue(97);

        this.tier = tier;

        CompoundTag tag = this.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);

        generatedTooltip = generatePouchLore(tier);
    }

    private static List<Component> generatePouchLore(int tier) {
        int upTo = (tier - 1) / 3;
        int rows = 0;
        String totalString = "";

        switch (tier % 3) {
            case 0 -> {
                rows = 6;
                totalString = "54";
            }
            case 1 -> {
                rows = 1;
                totalString = "9";
            }
            case 2 -> {
                rows = 3;
                totalString = "27";
            }
        }

        if (tier >= 10) {
            rows = 6;
        }

        if (tier >= 7) {
            totalString = tier - 6 + "stx";
        } else if (tier >= 4) {
            totalString += EmeraldSymbols.L_STRING + EmeraldSymbols.E_STRING;
        } else {
            totalString += EmeraldSymbols.E_STRING + EmeraldSymbols.B_STRING;
        }

        List<Component> itemLore = new ArrayList<>();

        itemLore.add(Component.empty());
        itemLore.add(Component.literal("Emerald Pouches allows the wearer to easily ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("store ").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("and ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("convert ").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("picked emeralds without spending extra inventory slots.")
                        .withStyle(ChatFormatting.GRAY)));
        itemLore.add(Component.empty());
        itemLore.add(Component.literal(" - " + rows + " Rows ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("(" + totalString + " Total)"))
                .withStyle(ChatFormatting.DARK_GRAY));

        switch (upTo) {
            case 0 -> itemLore.add(Component.literal("No Auto-Conversions").withStyle(ChatFormatting.GRAY));
            case 1 -> itemLore.add(Component.literal("Converts up to")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" Emerald Blocks").withStyle(ChatFormatting.WHITE)));
            default -> itemLore.add(Component.literal("Converts up to")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" Liquid Emeralds").withStyle(ChatFormatting.WHITE)));
        }

        return itemLore;
    }

    @Override
    public Component getHoverName() {
        return Component.literal("Emerald Pouch ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal("[Tier " + MathUtils.toRoman(tier) + "]")
                        .withStyle(ChatFormatting.DARK_GREEN));
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag flag) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());
        tooltip.addAll(generatedTooltip);

        return tooltip;
    }

    public int getTier() {
        return tier;
    }
}
