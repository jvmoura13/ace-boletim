package br.com.jvmoura.aceboletim.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visitas")
data class VisitaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val data: String,
    val ciclo: String,
    val quarteirao: String,
    val rua: String,
    val numero: String,
    val sequencia: String,
    val complemento: String,
    val tipoImovel: String,
    val inspecionado: Boolean,

    val pendencia: String,

    val foco: Boolean,

    val a1: Int,
    val a2: Int,
    val b: Int,
    val c: Int,
    val d1: Int,
    val d2: Int,
    val e: Int,

    val eliminados: Int,
    val tratados: Int,

    val larvicidaGramas: Double
)