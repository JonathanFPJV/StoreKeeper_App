package com.example.storekeeper.ui.theme.Productos

import com.example.storekeeper.ui.theme.Categoria.CategoryModel
import com.example.storekeeper.ui.theme.RFID.RFIDModel
import com.google.gson.annotations.SerializedName

data class ProductModel(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("img")
    val img: String?,
    @SerializedName("price")
    val price: Double,
    @SerializedName("description")
    val description: String,
    @SerializedName("stock")
    val stock: Int,
    @SerializedName("category")
    val category: Int, // Ahora es un Int, no un objeto CategoryModel
    @SerializedName("idNFC")
    val idNFC: Int? // Ahora es un Int (puede ser nulo)
)
