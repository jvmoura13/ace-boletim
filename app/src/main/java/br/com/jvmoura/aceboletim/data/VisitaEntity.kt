package br.com.jvmoura.aceboletim.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visitas")
data class VisitaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val data: String,
    val ano: Int,
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

    // DEPÓSITOS TRATADOS
    val a1: Int,
    val a2: Int,
    val b: Int,
    val c: Int,
    val d1: Int,
    val d2: Int,
    val e: Int,

    // DEPÓSITOS ELIMINADOS
    val eliminadosA1: Int,
    val eliminadosA2: Int,
    val eliminadosB: Int,
    val eliminadosC: Int,
    val eliminadosD1: Int,
    val eliminadosD2: Int,
    val eliminadosE: Int,

    // INDICA SE HOUVE TRATAMENTO NESTA VISITA
    val tratados: Boolean,

    val larvicidaGramas: Double
)