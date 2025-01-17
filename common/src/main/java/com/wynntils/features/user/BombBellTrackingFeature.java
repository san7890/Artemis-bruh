/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.features.UserFeature;
import java.util.List;

public class BombBellTrackingFeature extends UserFeature {
    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.BombBell);
    }
}
