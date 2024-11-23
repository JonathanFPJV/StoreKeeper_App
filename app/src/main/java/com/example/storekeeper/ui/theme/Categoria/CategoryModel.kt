package com.example.storekeeper.ui.theme.Categoria

import com.google.gson.annotations.SerializedName

data class CategoryModel(
    @SerializedName("id")
    val id: Int, // Asumo que Django asigna un ID por defecto
    @SerializedName("name")
    val name: String,
    @SerializedName("img")
    val img: String? // Puede ser nulo
)