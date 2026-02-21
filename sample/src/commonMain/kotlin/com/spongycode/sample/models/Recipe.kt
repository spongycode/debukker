package com.spongycode.sample.models

import kotlinx.serialization.Serializable

@Serializable
data class RecipeResponse(
    val recipes: List<Recipe>,
    val total: Int,
    val skip: Int,
    val limit: Int
)

@Serializable
data class Recipe(
    val id: Int,
    val name: String,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val cuisine: String,
    val caloriesPerServing: Int,
    val tags: List<String> = emptyList(),
    val image: String = "",
    val rating: Double,
    val reviewCount: Int,
    val mealType: List<String> = emptyList()
)
