/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.wynn.handleditems.properties.QualityTierItemProperty;
import com.wynntils.wynn.objects.profiles.ingredient.IngredientProfile;

public class IngredientItem extends GameItem implements QualityTierItemProperty {
    private final IngredientProfile ingredientProfile;

    public IngredientItem(IngredientProfile ingredientProfile) {
        this.ingredientProfile = ingredientProfile;
    }

    public IngredientProfile getIngredientProfile() {
        return ingredientProfile;
    }

    public int getQualityTier() {
        return ingredientProfile.getTier().getTierInt();
    }

    @Override
    public String toString() {
        return "IngredientItem{" + "ingredientProfile=" + ingredientProfile + '}';
    }
}
