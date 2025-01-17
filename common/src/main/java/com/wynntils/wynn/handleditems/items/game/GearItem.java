/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.wynn.handleditems.properties.GearTierItemProperty;
import com.wynntils.wynn.objects.ItemIdentificationContainer;
import com.wynntils.wynn.objects.Powder;
import com.wynntils.wynn.objects.profiles.item.GearIdentification;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.objects.profiles.item.ItemTier;
import java.util.List;
import net.minecraft.network.chat.Component;

public class GearItem extends GameItem implements GearTierItemProperty {
    private final ItemProfile itemProfile;
    private final List<GearIdentification> identifications;
    private final List<ItemIdentificationContainer> idContainers;
    private final List<Powder> powders;
    private final int rerolls;
    private final List<Component> setBonus;

    public GearItem(
            ItemProfile itemProfile,
            List<GearIdentification> identifications,
            List<ItemIdentificationContainer> idContainers,
            List<Powder> powders,
            int rerolls,
            List<Component> setBonus) {
        this.itemProfile = itemProfile;
        this.identifications = identifications;
        this.idContainers = idContainers;
        this.powders = powders;
        this.rerolls = rerolls;
        this.setBonus = setBonus;
    }

    public ItemProfile getItemProfile() {
        return itemProfile;
    }

    public List<GearIdentification> getIdentifications() {
        return identifications;
    }

    public List<ItemIdentificationContainer> getIdContainers() {
        return idContainers;
    }

    public List<Powder> getPowders() {
        return powders;
    }

    public int getRerolls() {
        return rerolls;
    }

    public List<Component> getSetBonus() {
        return setBonus;
    }

    @Override
    public ItemTier getGearTier() {
        return itemProfile.getTier();
    }

    @Override
    public String toString() {
        return "GearItem{" + "itemProfile="
                + itemProfile + ", identifications="
                + identifications + ", powders="
                + powders + ", rerolls="
                + rerolls + ", setBonus="
                + setBonus + '}';
    }
}
