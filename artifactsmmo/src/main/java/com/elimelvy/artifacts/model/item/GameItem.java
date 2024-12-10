package com.elimelvy.artifacts.model.item;

import java.util.List;

public record GameItem(
        String name,
        String code,
        int level,
        String type,
        String subtype,
        String description,
        List<Effect> effects,
        Recipe craft,
        boolean tradeable) {
}
