package br.com.jvmoura.aceboletim.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface VisitaDao {

    @Insert
    suspend fun inserir(visita: VisitaEntity)

    @Insert
    suspend fun inserirTodas(visitas: List<VisitaEntity>)

    @Query("SELECT * FROM visitas")
    suspend fun listarTodas(): List<VisitaEntity>

    @Query("SELECT * FROM visitas WHERE ciclo = :ciclo")
    suspend fun listarPorCiclo(ciclo: String): List<VisitaEntity>

    @Query("DELETE FROM visitas")
    suspend fun limparTudo()
}