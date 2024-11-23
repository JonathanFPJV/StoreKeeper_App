package com.example.storekeeper.ui.theme.RFID

import com.google.gson.annotations.SerializedName

data class RFIDModel(
    @SerializedName("id")
    val id: Int, // ID de la etiqueta RFID
    @SerializedName("id_tag")
    val idTag: String,
    @SerializedName("id_esp32")
    val idEsp32: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("fecha_llegada")
    val fechaLlegada: String?, // Fecha como String (recomendable usar LocalDate si lo deseas)
    @SerializedName("fecha_asignado")
    val fechaAsignado: String?
)