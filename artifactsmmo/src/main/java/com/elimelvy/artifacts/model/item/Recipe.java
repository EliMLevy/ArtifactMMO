package com.elimelvy.artifacts.model.item;

import java.util.List;


public record Recipe (String skill,int level,List<RecipeIngredient> items) {}
