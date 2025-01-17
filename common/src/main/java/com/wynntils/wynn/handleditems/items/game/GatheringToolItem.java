/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.handleditems.properties.DurableItemProperty;
import com.wynntils.wynn.handleditems.properties.NumberedTierItemProperty;
import com.wynntils.wynn.objects.profiles.ToolProfile;

public class GatheringToolItem extends GameItem implements NumberedTierItemProperty, DurableItemProperty {
    private final ToolProfile toolProfile;
    private final CappedValue durability;

    public GatheringToolItem(ToolProfile toolProfile, CappedValue durability) {
        this.toolProfile = toolProfile;
        this.durability = durability;
    }

    public ToolProfile getToolProfile() {
        return toolProfile;
    }

    public CappedValue getDurability() {
        return durability;
    }

    public int getTier() {
        return toolProfile.getTier();
    }

    @Override
    public String toString() {
        return "GatheringToolItem{" + "toolProfile=" + toolProfile + ", durability=" + durability + '}';
    }
}
