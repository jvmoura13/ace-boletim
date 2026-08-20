package br.com.jvmoura.aceboletim.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RgDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserir(rg: RgEntity)

    @Query("""
        SELECT * FROM rgs
        WHERE ano = :ano
        AND ciclo = :ciclo
        AND quarteirao = :quarteirao
        LIMIT 1
    """)
    suspend fun buscar(
        ano: Int,
        ciclo: String,
        quarteirao: String
    ): RgEntity?

    @Query("""
        SELECT finalizado FROM rgs
        WHERE ano = :ano
        AND ciclo = :ciclo
        AND quarteirao = :quarteirao
        LIMIT 1
    """)
    suspend fun estaFinalizado(
        ano: Int,
        ciclo: String,
        quarteirao: String
    ): Boolean?

    @Query("""
        UPDATE rgs
        SET finalizado = 1
        WHERE ano = :ano
        AND ciclo = :ciclo
        AND quarteirao = :quarteirao
    """)
    suspend fun finalizar(
        ano: Int,
        ciclo: String,
        quarteirao: String
    )
}