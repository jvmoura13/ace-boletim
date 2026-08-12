package br.com.jvmoura.aceboletim

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
private val Context.dataStore by preferencesDataStore(name = "rascunho_boletim")
object RascunhoStore { private val KEY = stringPreferencesKey("boletim_atual")
    private val gson = Gson()
    suspend fun salvar(context: Context, rascunho: BoletimRascunho) { val json = gson.toJson(rascunho)
        context.dataStore.edit { prefs ->
            prefs[KEY] = json
        }
    }
    suspend fun carregar(context: Context): BoletimRascunho?
    { val prefs = context.dataStore.data.first()
        val json = prefs[KEY] ?: return null
        return gson.fromJson(json, BoletimRascunho::class.java)
    }
    suspend fun limpar(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY)
        }
    }
}