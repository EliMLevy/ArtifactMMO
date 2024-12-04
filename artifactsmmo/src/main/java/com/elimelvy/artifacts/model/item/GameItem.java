package com.elimelvy.artifacts.model.item;

import java.util.List;

public record GameItem(
        String code,
        int level,
        String type,
        List<Effect> effects,
        Recipe recipe) {

}
