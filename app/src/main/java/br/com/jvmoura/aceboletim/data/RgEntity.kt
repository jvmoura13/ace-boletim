package br.com.jvmoura.aceboletim.data

import androidx.room.Entity

@Entity(
    tableName = "rgs",
    primaryKeys = ["ano", "ciclo", "quarteirao"]
)
data class RgEntity(
    val ano: Int,
    val ciclo: String,
    val quarteirao: String,
    val finalizado: Boolean = false
)