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

    @Query("""
    SELECT * FROM visitas
    WHERE ciclo = :ciclo
    AND ano = :ano
""")
    suspend fun listarPorCiclo(
        ciclo: String,
        ano: Int
    ): List<VisitaEntity>

    @Query("""
    SELECT * FROM visitas
    ORDER BY id DESC
""")
    suspend fun listarTodasParaRg(): List<VisitaEntity>

    @Query("DELETE FROM visitas")
    suspend fun limparTudo()
}