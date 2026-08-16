package br.com.jvmoura.aceboletim

import br.com.jvmoura.aceboletim.data.AppDatabase
import br.com.jvmoura.aceboletim.data.VisitaEntity
import kotlinx.coroutines.Dispatchers
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.itemsIndexed
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.Context
import android.widget.Toast
import androidx.compose.material3.Icon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.produceState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.exifinterface.media.ExifInterface
import android.graphics.Matrix
import android.graphics.Bitmap
import android.widget.Space
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.unit.sp
import java.util.Locale
import java.time.DayOfWeek
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager


data class CabecalhoBoletim(
    val nome: String,
    val supervisor: String,
    val data: String,
    val localidade: String,
    val quarteirao: String,
    val atividade: String,
    val categoria: String,
    val ciclo: String
)

data class Tratamento(
    val a1: Int,
    val a2: Int,
    val b: Int,
    val c: Int,
    val d1: Int,
    val d2: Int,
    val e: Int,
    val gramas: Double
) {
    val total: Int
        get() = a1 + a2 + b + c + d1 + d2 + e
}

data class Visita(
    val cicloAno: String,
    val quarteirao: String,
    val rua: String,
    val numero: String,
    val sequencia: String,
    val complemento: String,
    val tipoImovel: String,
    val inspecionado: Boolean,
    val pendencia: String,
    val foco: Boolean,
    val depositosEliminados: Int,
    val tratamento: Tratamento?,
    val observacao: String
)

fun gerarBoletimPdf(
    context: android.content.Context,
    cabecalho: CabecalhoBoletim,
    visitas: List<Visita>
) {
    val pdf = PdfDocument()
    val paint = Paint()
    val pageInfo = PdfDocument.PageInfo.Builder(1800, 1200, 1).create()
    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas
    var y = 40
    // ===== TÍTULO =====
    paint.textSize = 28f
    canvas.drawText("BOLETIM DE VISITAS", 760f, y.toFloat(), paint)
    y += 60
    // ===== CABEÇALHO EM 2 BLOCOS =====
    paint.textSize = 20f
    val esquerda = 90f
    val direita = 1450f
    val linha = 36
    // Linha 1
    canvas.drawText("Nome: ${cabecalho.nome}", esquerda, y.toFloat(), paint)
    canvas.drawText("Categoria: ${cabecalho.categoria}", direita, y.toFloat(), paint)
    y += linha
    // Linha 2
    canvas.drawText("Supervisor: ${cabecalho.supervisor}", esquerda, y.toFloat(), paint)
    canvas.drawText("Localidade: ${cabecalho.localidade}", direita, y.toFloat(), paint)
    y += linha
    // Linha 3
    canvas.drawText("Data: ${cabecalho.data}", esquerda, y.toFloat(), paint)
    canvas.drawText("Quarteirão: ${cabecalho.quarteirao}", direita, y.toFloat(), paint)
    y += linha
    // Linha 4
    canvas.drawText("Atividade: ${cabecalho.atividade}", esquerda, y.toFloat(), paint)
    canvas.drawText("Ciclo: ${cabecalho.ciclo}", direita, y.toFloat(), paint)
    y += 50
    // Linha separadora
    canvas.drawLine(20f, y.toFloat(), 1780f, y.toFloat(), paint)
    y += 30

    paint.textSize = 18f
    val colQ = 20f
    val colRua = 80f
    val colNum = 500f
    val colSeq = 590f
    val colComp = 680f
    val colTipo = 840f
    val colInsp = 920f
    val colPend = 1000f
    val colFoco = 1080f
    val colA1 = 1160f
    val colA2 = 1230f
    val colB = 1300f
    val colC = 1370f
    val colD1 = 1440f
    val colD2 = 1510f
    val colE = 1580f
    val colGram = 1650f
    val colTrat = 1730f

    canvas.drawText("Q", colQ, y.toFloat(), paint)
    canvas.drawText("Rua", colRua, y.toFloat(), paint)
    canvas.drawText("Nº", colNum, y.toFloat(), paint)
    canvas.drawText("Seq", colSeq, y.toFloat(), paint)
    canvas.drawText("Comp", colComp, y.toFloat(), paint)
    canvas.drawText("Tipo", colTipo, y.toFloat(), paint)
    canvas.drawText("Insp", colInsp, y.toFloat(), paint)
    canvas.drawText("Pend", colPend, y.toFloat(), paint)
    canvas.drawText("Foco", colFoco, y.toFloat(), paint)
    canvas.drawText("A1", colA1, y.toFloat(), paint)
    canvas.drawText("A2", colA2, y.toFloat(), paint)
    canvas.drawText("B", colB, y.toFloat(), paint)
    canvas.drawText("C", colC, y.toFloat(), paint)
    canvas.drawText("D1", colD1, y.toFloat(), paint)
    canvas.drawText("D2", colD2, y.toFloat(), paint)
    canvas.drawText("E", colE, y.toFloat(), paint)
    canvas.drawText("g", colGram, y.toFloat(), paint)
    canvas.drawText("T", colTrat, y.toFloat(), paint)
    y += 15
    canvas.drawLine(20f, y.toFloat(), 1780f, y.toFloat(), paint)
    y += 28
    // Linhas das visitas
    paint.textSize = 17f
    var paginaAtual = page
    var canvasAtual = canvas
    visitas.forEachIndexed { index, visita ->
        if (y > 1120) {
            pdf.finishPage(paginaAtual)
            val novaInfo = PdfDocument.PageInfo.Builder(1800, 1200, index + 2).create()
            paginaAtual = pdf.startPage(novaInfo)
            canvasAtual = paginaAtual.canvas
            paint.textSize = 16f
            canvasAtual.drawText("BOLETIM DE VISITAS (continuação)", 620f, 50f, paint)
            paint.textSize = 11f
            canvasAtual.drawText("Q", colQ, 90f, paint)
            canvasAtual.drawText("Rua", colRua, 90f, paint)
            canvasAtual.drawText("Nº", colNum, 90f, paint)
            canvasAtual.drawText("Seq", colSeq, 90f, paint)
            canvasAtual.drawText("Comp", colComp, 90f, paint)
            canvasAtual.drawText("Tipo", colTipo, 90f, paint)
            canvasAtual.drawText("Insp", colInsp, 90f, paint)
            canvasAtual.drawText("Pend", colPend, 90f, paint)
            canvasAtual.drawText("Foco", colFoco, 90f, paint)
            canvasAtual.drawText("A1", colA1, 90f, paint)
            canvasAtual.drawText("A2", colA2, 90f, paint)
            canvasAtual.drawText("B", colB, 90f, paint)
            canvasAtual.drawText("C", colC, 90f, paint)
            canvasAtual.drawText("D1", colD1, 90f, paint)
            canvasAtual.drawText("D2", colD2, 90f, paint)
            canvasAtual.drawText("E", colE, 90f, paint)
            canvasAtual.drawText("g", colGram, 90f, paint)
            canvasAtual.drawText("T", colTrat, 90f, paint)
            canvasAtual.drawLine(20f, 105f, 1780f, 105f, paint)
            y = 140
        }
        val t = visita.tratamento
        val ruaCurta =
            if (visita.rua.length > 28) visita.rua.take(28) + "…"
            else visita.rua
        val tipo = when (visita.tipoImovel) {
            "Residência" -> "R"
            "Terreno" -> "TB"
            "Comércio" -> "C"
            else -> "O"
        }
        canvasAtual.drawText(visita.quarteirao, colQ, y.toFloat(), paint)
        canvasAtual.drawText(ruaCurta, colRua, y.toFloat(), paint)
        canvasAtual.drawText(visita.numero, colNum, y.toFloat(), paint)
        canvasAtual.drawText(visita.sequencia, colSeq, y.toFloat(), paint)
        canvasAtual.drawText(visita.complemento.take(10), colComp, y.toFloat(), paint)
        canvasAtual.drawText(tipo, colTipo, y.toFloat(), paint)
        canvasAtual.drawText(if (visita.inspecionado) "S" else "N", colInsp, y.toFloat(), paint)
        canvasAtual.drawText(
            when (visita.pendencia) {
                "Fechado" -> "F"
                "Recusado" -> "R"
                "Abandonado" -> "A"
                "Outros" -> "O"
                else -> "N"
            },
            colPend,
            y.toFloat(),
            paint
        )
        canvasAtual.drawText(if (visita.foco) "S" else "N", colFoco, y.toFloat(), paint)
        canvasAtual.drawText((t?.a1 ?: 0).toString(), colA1, y.toFloat(), paint)
        canvasAtual.drawText((t?.a2 ?: 0).toString(), colA2, y.toFloat(), paint)
        canvasAtual.drawText((t?.b ?: 0).toString(), colB, y.toFloat(), paint)
        canvasAtual.drawText((t?.c ?: 0).toString(), colC, y.toFloat(), paint)
        canvasAtual.drawText((t?.d1 ?: 0).toString(), colD1, y.toFloat(), paint)
        canvasAtual.drawText((t?.d2 ?: 0).toString(), colD2, y.toFloat(), paint)
        canvasAtual.drawText((t?.e ?: 0).toString(), colE, y.toFloat(), paint)
        canvasAtual.drawText(String.format("%.1f", t?.gramas ?: 0.0), colGram, y.toFloat(), paint)
        canvasAtual.drawText((t?.total ?: 0).toString(), colTrat,
            y.toFloat(), paint)
        // linha separando a visita
        canvasAtual.drawLine(20f, y + 10f, 1780f, y + 10f, paint)
        y += 26

    }

    // ================= RESUMO DO BOLETIM =================

    y += 40
    paint.textSize = 22f
    paint.isFakeBoldText = true
    canvasAtual.drawText("RESUMO DO BOLETIM", 20f, y.toFloat(), paint)
    y += 30
    paint.textSize = 18f
    paint.isFakeBoldText = false

    // ---------- INSPECIONADOS ----------

    val residenciasInspecionadas = visitas.count {
        it.inspecionado && it.tipoImovel == "Residência"
    }

    val comerciosInspecionados = visitas.count {
        it.inspecionado && it.tipoImovel == "Comércio"
    }
    val terrenosInspecionados = visitas.count {
        it.inspecionado && it.tipoImovel == "Terreno"
    }
    val outrosInspecionados = visitas.count {
        it.inspecionado && it.tipoImovel == "Outros"
    }
    val totalInspecionados = visitas.count { it.inspecionado }

    // ---------- PENDÊNCIAS ----------

    val fechados = visitas.count {
        visita -> visita.pendencia.contains("Fechado", ignoreCase = true)
    }
    val recusas = visitas.count {
        visita -> visita.pendencia.contains("Recusa", ignoreCase = true)
    }
    val abandonados = visitas.count {
        visita -> visita.pendencia.contains("Abandon", ignoreCase = true)
    }
    val totalPendencias = visitas.count { it.pendencia.isNotBlank() }

    // ---------- INFORMADOS ----------

    val informados = totalInspecionados + totalPendencias

    // ---------- DEPÓSITOS ----------

    val totalA1 = visitas.sumOf { it.tratamento?.a1 ?: 0 }
    val totalA2 = visitas.sumOf { it.tratamento?.a2 ?: 0 }
    val totalB = visitas.sumOf { it.tratamento?.b ?: 0 }
    val totalC = visitas.sumOf { it.tratamento?.c ?: 0 }
    val totalD1 = visitas.sumOf { it.tratamento?.d1 ?: 0 }
    val totalD2 = visitas.sumOf { it.tratamento?.d2 ?: 0 }
    val totalE = visitas.sumOf { it.tratamento?.e ?: 0 }
    val totalDepositosTratados =
        totalA1 + totalA2 + totalB + totalC + totalD1 + totalD2 + totalE
    val totalDepositosEliminados = visitas.sumOf { it.depositosEliminados }

    // ---------- LARVICIDA ----------

    val totalGramas = visitas.sumOf {
        it.tratamento?.gramas?.toDouble() ?: 0.0 }

    // ---------- FUNÇÃO AUXILIAR ----------

    fun linhaResumo(titulo: String, valor: String) {
        canvasAtual.drawText(titulo, 40f, y.toFloat(), paint)
        canvasAtual.drawText(valor, 900f, y.toFloat(), paint)
        y += 24
    }

    // ---------- IMPRESSÃO ----------

    linhaResumo("Informados", informados.toString())

    y += 10

    linhaResumo("Residências inspecionadas",
        residenciasInspecionadas.toString())
    linhaResumo("Comércios inspecionados",
        comerciosInspecionados.toString())
    linhaResumo("Terrenos inspecionados",
        terrenosInspecionados.toString())
    linhaResumo("Outros inspecionados",
        outrosInspecionados.toString())
    linhaResumo("Total inspecionados",
        totalInspecionados.toString())

    y += 10

    linhaResumo("Fechados",
        fechados.toString())
    linhaResumo("Recusas",
        recusas.toString())
    linhaResumo("Abandonados",
        abandonados.toString())
    linhaResumo("Total pendências",
        totalPendencias.toString())

    y += 10

    linhaResumo("Depósitos A1",
        totalA1.toString())
    linhaResumo("Depósitos A2",
        totalA2.toString())
    linhaResumo("Depósitos B",
        totalB.toString())
    linhaResumo("Depósitos C",
        totalC.toString())
    linhaResumo("Depósitos D1",
        totalD1.toString())
    linhaResumo("Depósitos D2",
        totalD2.toString())
    linhaResumo("Depósitos E",
        totalE.toString())
    linhaResumo("Total depósitos tratados",
        totalDepositosTratados.toString())
    linhaResumo("Total depósitos eliminados",
        totalDepositosEliminados.toString())

    y += 10

    linhaResumo( "Total de larvicida utilizado",
        String.format(java.util.Locale.US, "%.1f g", totalGramas)
    )

    pdf.finishPage(paginaAtual)

    val pasta = android.os.Environment.getExternalStoragePublicDirectory(
        android.os.Environment.DIRECTORY_DOWNLOADS
    )

    val nomeArquivo =
        "Boletim_${cabecalho.localidade}_Q${cabecalho.quarteirao}_${cabecalho.data.replace("/", "-")}.pdf"

    val arquivo = File(pasta, nomeArquivo)

    pdf.writeTo(FileOutputStream(arquivo))
    pdf.close()

    Toast.makeText(
        context,
        "Boletim diário salvo em Downloads",
        Toast.LENGTH_LONG
    ).show()

}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            MaterialTheme {
                AppACE()
            }
        }
    }
}

fun gerarObservacoesPdf(
    context: android.content.Context,
    cabecalho: CabecalhoBoletim,
    visitas: List<Visita>
) {
    val pdf = PdfDocument()
    val paint = Paint()

    val pageInfo = PdfDocument.PageInfo.Builder(1200, 1800, 1).create()
    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas

    var y = 60

    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("RELATÓRIO DE OBSERVAÇÕES", 430f, y.toFloat(), paint)

    y += 50

    paint.textSize = 16f
    paint.isFakeBoldText = false

    canvas.drawText("Agente: ${cabecalho.nome}", 60f, y.toFloat(), paint)
    y += 24
    canvas.drawText("Data: ${cabecalho.data}", 60f, y.toFloat(), paint)
    y += 24
    canvas.drawText("Localidade: ${cabecalho.localidade}", 60f, y.toFloat(), paint)
    y += 24
    canvas.drawText("Total de observações: ${visitas.size}", 60f, y.toFloat(), paint)

    y += 30
    canvas.drawLine(40f, y.toFloat(), 1160f, y.toFloat(), paint)
    y += 30

    visitas.forEach { visita ->
        paint.isFakeBoldText = true
        canvas.drawText(
            "Rua: ${visita.rua} • Q${visita.quarteirao}",
            60f,
            y.toFloat(),
            paint
        )

        y += 24

        paint.isFakeBoldText = false

        val detalhes = buildString {
            append("Imóvel: ${visita.numero}")

            if (visita.sequencia.isNotBlank()) {
                append(" • Seq. ${visita.sequencia}")
            }

            if (visita.complemento.isNotBlank()) {
                append(" • Comp. ${visita.complemento}")
            }
        }

        canvas.drawText(detalhes, 60f, y.toFloat(), paint)

        y += 24

        canvas.drawText("Observação:", 60f, y.toFloat(), paint)
        y += 22

        val obs = visita.observacao.ifBlank { "-" }
        canvas.drawText(obs, 80f, y.toFloat(), paint)

        y += 36

        canvas.drawLine(40f, y.toFloat(), 1160f, y.toFloat(), paint)
        y += 30
    }

    pdf.finishPage(page)

    val fileName = "Observacoes_${cabecalho.data.replace("/", "-")}.pdf"

    val file = java.io.File(
        android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_DOWNLOADS
        ),
        fileName
    )

    pdf.writeTo(java.io.FileOutputStream(file))
    pdf.close()

    android.widget.Toast.makeText(
        context,
        "PDF de observações salvo em Downloads",
        android.widget.Toast.LENGTH_LONG
    ).show()
}

@Composable
fun AppACE() {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val prefs = remember {
        context.getSharedPreferences("ace_config", Context.MODE_PRIVATE)
    }

    var nomeAgente by remember {
        mutableStateOf(prefs.getString("nome_agente", "") ?: "")
    }

    var localidadeAgente by remember {
        mutableStateOf(prefs.getString("localidade_agente", "") ?: "")
    }

    var supervisorAgente by remember {
        mutableStateOf(prefs.getString("supervisor_agente", "") ?: "")
    }
    var cargoAgente by remember { mutableStateOf("Agente de Combate a Endemias") }
    val dataHoje = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    var boletimIniciado by remember { mutableStateOf(false) }
    var atividade by remember { mutableStateOf("Tratamento") }
    var categoria by remember { mutableStateOf("Bairro") }
    var cicloAno by remember { mutableStateOf("") }
    var local by remember { mutableStateOf("") }
    var agente by remember { mutableStateOf("") }
    var supervisor by remember { mutableStateOf("") }
    var visitasSalvas by remember { mutableStateOf(listOf<Visita>()) }
    var telaAtual by remember { mutableStateOf("inicio") }


    var fotoAgenteUri by remember {
        mutableStateOf(
            context.getSharedPreferences("ace_config", Context.MODE_PRIVATE)
                .getString("foto_agente", null)
        )
    }

    val seletorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            fotoAgenteUri = uri.toString()

            context.getSharedPreferences("ace_config", Context.MODE_PRIVATE)
                .edit()
                .putString("foto_agente", uri.toString())
                .apply()

            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(Unit) {
        val rascunho = RascunhoStore.carregar(context)
        if (rascunho != null) {
            atividade = rascunho.cabecalho.atividade
            categoria = rascunho.cabecalho.categoria
            cicloAno = rascunho.cabecalho.ciclo
            local = rascunho.cabecalho.localidade
            agente = rascunho.cabecalho.nome
            supervisor = rascunho.cabecalho.supervisor
            visitasSalvas = rascunho.visitas
            boletimIniciado = true
        }
    }

    BackHandler(enabled = boletimIniciado) {
        boletimIniciado = false
    }

    if (telaAtual == "inicio") {
        HomeScreen(
            visitasHoje = visitasSalvas.size,
            observacoes = visitasSalvas.count {
                it.observacao.isNotBlank() },
            focos = visitasSalvas.count { it.foco },

            nomeAgente = nomeAgente,
            onNomeAgenteChange = {
                nomeAgente = it
                agente = it

                prefs.edit()
                    .putString("nome_agente", it)
                    .apply()
            },
            cargoAgente = cargoAgente,

            localidadeAgente = localidadeAgente,
            onLocalidadeAgenteChange = {
                localidadeAgente = it
                local = it

                prefs.edit()
                    .putString("localidade_agente", it)
                    .apply()
            },

            supervisorAgente = supervisorAgente,
            onSupervisorAgenteChange = {
                supervisorAgente = it
                supervisor = it

                prefs.edit()
                    .putString("supervisor_agente", it)
                    .apply()
            },

            fotoAgenteUri = fotoAgenteUri,
            onFotoAgenteClick = {
                seletorFoto.launch(arrayOf("image/*"))
            },

            dataHoje = dataHoje,
            onVD = { if (visitasSalvas.isNotEmpty()) {
                boletimIniciado = true
                telaAtual = "visitas"
            } else {
                telaAtual = "novo"
            }
           },
            onRG = {
                telaAtual = "rg"
            },

            onResumo = {
                telaAtual = "resumo_semanal"

                       },

            onConfig = {
                Toast.makeText(context, "Configurações em desenvolvimento",
                    Toast.LENGTH_SHORT).show()
                       },

            onRelatorios = {
                Toast.makeText(context, "Abra a pasta Downloads para ver os PDFs",
                    Toast.LENGTH_LONG).show()
            }
        )
    } else if (telaAtual == "novo") {
        TelaNovoBoletim(
            atividade = atividade,
            onAtividadeChange = {
                atividade = it
            },

            categoria = categoria,
            onCategoriaChange = {
                categoria = it
            },

            agente = nomeAgente,
            onAgenteChange = {
                nomeAgente = it
                agente = it
            },

            supervisor = supervisorAgente,
            onSupervisorChange = {
                supervisorAgente = it
                supervisor = it
            },

            local = localidadeAgente,
            onLocalChange = {
                localidadeAgente = it
                local = it
            },

            cicloAno = cicloAno,
            onCicloChange = {
                cicloAno = it
            },

            onIniciar = {
                boletimIniciado = true
                telaAtual = "visitas"
            },

            onVoltar = {
                telaAtual = "inicio"
            }
        )
    } else if (telaAtual == "resumo_semanal") {
            TelaResumoSemanal(
                visitas = visitasSalvas,
                onVoltar = { telaAtual = "inicio" }
            )
        }

    else if (telaAtual == "rg") {
        TelaRG(
            ciclo = cicloAno,
            onVoltar = { telaAtual = "inicio" }
        )
    }

     else {
        TelaVisitas(
            atividade = atividade,
            categoria = categoria,
            agente = nomeAgente,
            supervisor = supervisorAgente,
            localidade = localidadeAgente,
            ciclo = cicloAno,
            visitasIniciais = visitasSalvas,
            onVoltar = {
                telaAtual = "novo"
            }
        )
    }
}


@Composable
fun HomeScreen(



    visitasHoje: Int,
    observacoes: Int,
    focos: Int,
    nomeAgente: String,
    onNomeAgenteChange: (String) -> Unit,
    cargoAgente: String,
    localidadeAgente: String,
    onLocalidadeAgenteChange: (String) -> Unit,
    supervisorAgente: String,
    onSupervisorAgenteChange: (String) -> Unit,
    fotoAgenteUri: String?,
    onFotoAgenteClick: () -> Unit,
    dataHoje: String,
    onVD: () -> Unit,
    onRG: () -> Unit,
    onResumo: () -> Unit,
    onConfig: () -> Unit,
    onRelatorios: () -> Unit,
) {

    val Bg = Color(0xFF0F172A)
    val CardBg = Color(0xFF1E293B)

    val context = LocalContext.current
    var fotoBitmap by remember {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }

    LaunchedEffect(fotoAgenteUri) {
        fotoBitmap = if (fotoAgenteUri != null) {
            try {
                val uri = android.net.Uri.parse(fotoAgenteUri)

                val originalBitmap = context.contentResolver
                    .openInputStream(uri)
                    ?.use { inputStream ->
                        android.graphics.BitmapFactory.decodeStream(inputStream)
                    }

                if (originalBitmap != null) {

                    val orientation = context.contentResolver
                        .openInputStream(uri)
                        ?.use { inputStream ->
                            ExifInterface(inputStream).getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )
                        } ?: ExifInterface.ORIENTATION_NORMAL

                    val matrix = android.graphics.Matrix()

                    when (orientation) {

                        ExifInterface.ORIENTATION_ROTATE_90 -> {
                            matrix.postRotate(90f)
                        }

                        ExifInterface.ORIENTATION_ROTATE_180 -> {
                            matrix.postRotate(180f)
                        }

                        ExifInterface.ORIENTATION_ROTATE_270 -> {
                            matrix.postRotate(270f)
                        }

                        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                            matrix.preScale(-1f, 1f)
                        }

                        ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                            matrix.preScale(1f, -1f)
                        }
                    }

                    android.graphics.Bitmap.createBitmap(
                        originalBitmap,
                        0,
                        0,
                        originalBitmap.width,
                        originalBitmap.height,
                        matrix,
                        true
                    )
                } else {
                    null
                }

            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            //.windowInsetsPadding(WindowInsets.safeDrawing)
            .background(Bg)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
                                    //TITULO TELA DE INICIO

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(26.dp)
                )
                Text(

                    "Olá, ACE!",
                    color = Color(0xFF00BCD4),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp)
                )
            }
        }

        item {
                                    //CARD AGENTE

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CardBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E3A8A))
                            .clickable {
                                onFotoAgenteClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {

                        if (fotoAgenteUri != null) {



                            if (fotoBitmap != null) {
                                Image(
                                    bitmap = fotoBitmap!!.asImageBitmap(),
                                    contentDescription = "Foto do agente",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Adicionar foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                        } else {

                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Adicionar foto",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                    }

                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {

                        Text(
                            cargoAgente,
                            color = Color(0xFFCBD5E1),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(4.dp))

                        OutlinedTextField(
                            value = nomeAgente,
                            onValueChange = onNomeAgenteChange,
                            label = { Text("Nome do agente") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(62.dp),
                            shape = RoundedCornerShape(30.dp),

                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3F7CFF),
                                unfocusedBorderColor = Color(0xFF65708A),
                                focusedLabelColor = Color(0xFF3F7CFF),
                                unfocusedLabelColor = Color(0xFF9AA5BA)
                            )
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = supervisorAgente,
                            onValueChange = onSupervisorAgenteChange,
                            label = { Text("Supervisor") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(62.dp),
                            shape = RoundedCornerShape(30.dp),

                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3F7CFF),
                                unfocusedBorderColor = Color(0xFF65708A),
                                focusedLabelColor = Color(0xFF3F7CFF),
                                unfocusedLabelColor = Color(0xFF9AA5BA)
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = localidadeAgente,
                            onValueChange = onLocalidadeAgenteChange,
                            label = { Text("Localidade") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(62.dp),
                            shape = RoundedCornerShape(30.dp),

                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3F7CFF),
                                unfocusedBorderColor = Color(0xFF65708A),
                                focusedLabelColor = Color(0xFF3F7CFF),
                                unfocusedLabelColor = Color(0xFF9AA5BA)
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier
                                    .size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(dataHoje, color = Color(0xFFCBD5E1))
                        }
                    }
                }
            }
        }

        item {
                                        //ESCOLHA OPÇÃO

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "Que a força esteja com vc!!!",
                    color = Color(0xFFB8C1D9),
                    style = MaterialTheme.typography.bodyLarge
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0xFF2563EB),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        item {
            Text(
                "",
                style = MaterialTheme.typography.titleLarge
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                                      //CARD VD

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(226.dp)
                        .shadow(
                            1.dp,
                            RoundedCornerShape(24.dp),
                            ambientColor = Color(0xFF2563EB),
                            spotColor = Color(0xFF2563EB)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF3F7FD0),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    onClick = onVD,
                    colors = CardDefaults.cardColors(
                        containerColor = Bg
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    color = Color(0xFF0B1730),
                                    shape = CircleShape
                                )
                                .border(
                                    1.dp,
                                    Color(0xFF143A7B),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Assignment,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {



                            Text(
                                "Visita Domiciliar",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF2563EB)
                            )

                            Spacer(Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(2.dp)
                                    .background(Color(0xFF2563EB))
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                "Registrar visitas, imóveis, depósitos e tratamentos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE5E7EB),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                                         //CARD RG
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(226.dp)
                        .shadow(
                            1.dp,
                            RoundedCornerShape(24.dp),
                            ambientColor = Color(0xFF2563EB),
                            spotColor = Color(0xFF2563EB)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF0AAB58),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    onClick = onRG,
                    colors = CardDefaults.cardColors(
                        containerColor = Bg
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    color = Color(0xFF0B1F14),
                                    shape = CircleShape
                                )
                                .border(
                                    1.dp,
                                    Color(0xFF14532D),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Place,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally)
                        {

                            Text(
                                "RG",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF22C55E)
                            )

                            Spacer(Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(2.dp)
                                    .background(Color(0xFF22C55E))
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                "Reconhecimento Geográfico dos quarteirões",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE5E7EB),
                                textAlign = TextAlign.Center
                            )
                        }

                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                                        //CARD RESUMO
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(226.dp)
                        .shadow(
                            1.dp,
                            RoundedCornerShape(24.dp),
                            ambientColor = Color(0xFF2563EB),
                            spotColor = Color(0xFF2563EB)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFFCD34D),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    onClick = onResumo,
                    colors = CardDefaults.cardColors(
                        containerColor = Bg
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    color = Color(0xFF725032),
                                    shape = CircleShape
                                )
                                .border(
                                    1.dp,
                                    Color(0xFFE8DD19),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Assessment,
                                contentDescription = null,
                                tint = Color(0xFFE8DD19),
                                modifier = Modifier
                                    .size(38.dp)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Text(
                                "Resumo Semanal",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFE8DD19),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(2.dp)
                                    .background(Color(0xFFE8DD19))
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Visualizar produção, focos e depósitos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE5E7EB),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                                        //CARD CONFIG.
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(226.dp)
                        .shadow(
                            1.dp,
                            RoundedCornerShape(24.dp),
                            ambientColor = Color(0xFF2563EB),
                            spotColor = Color(0xFF2563EB)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF673AB7),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    onClick = onConfig,
                    colors = CardDefaults.cardColors(
                        containerColor = Bg
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    color = Color(0xFF1B0B2A),
                                    shape = CircleShape
                                )
                                .border(
                                    1.dp,
                                    Color(0xFF5B21B6),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier
                                    .size(38.dp)
                            )

                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Text(
                                "Configurações",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFA855F7),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(2.dp)
                                    .background(Color(0xFFA855F7))
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Ajustes do app, backup, dados e preferências",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE5E7EB),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        item {
                                     //CARD RESUMO RAPIDO
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111827)
                ),
                border = BorderStroke(1.dp, Color(0xFF1F2937)),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Resumo rápido",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                visitasHoje.toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                "Visitas",
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                visitasHoje.toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                "Observações",
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                visitasHoje.toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                "Focos",
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
        item {
                                     // CARD RELATORIOS
            Card(
                onClick = onRelatorios,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111827)
                ),
                border = BorderStroke(1.dp, Color(0xFF1F2937)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Boletins e relatórios",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Acesse seus PDFs gerados",
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

            }
        }
        item {
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
fun TelaResumoSemanal(
    visitas: List<Visita>,
    onVoltar: () -> Unit
) {
    BackHandler {
        onVoltar()
    }

    val Bg = Color(0xFF0F172A)

    val context = LocalContext.current
    var visitasSemana by remember { mutableStateOf<List<VisitaEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        visitasSemana = AppDatabase.get(context).visitaDao().listarTodas()
    }

    // Apenas segunda a sexta da semana atual


    val hoje = LocalDate.now()
    val inicioSemana = hoje.with(DayOfWeek.MONDAY)
    val fimSemana = hoje.with(DayOfWeek.FRIDAY)

    val visitasUteis = visitasSemana.filter {
        val dataVisita = LocalDate.parse(it.data, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        dataVisita in inicioSemana..fimSemana
    }


    val totalInformados = visitasUteis.size

    val residencias = visitasUteis.count { it.tipoImovel == "Residência" }
    val comercios = visitasUteis.count { it.tipoImovel == "Comércio" }
    val terrenos = visitasUteis.count { it.tipoImovel == "Terreno" }
    val outros = visitasUteis.count {
        it.tipoImovel != "Residência" &&
                it.tipoImovel != "Comércio" &&
                it.tipoImovel != "Terreno"
    }

    val totalInspecionados = visitasUteis.count { it.inspecionado }

    val fechadas = visitasUteis.count { it.pendencia.trim().equals("Fechado", ignoreCase = true) }
    val recusadas = visitasUteis.count { it.pendencia.trim().equals("Recusado", ignoreCase = true) }
    val abandonadas = visitasUteis.count { it.pendencia.trim().equals("Abandonado", ignoreCase = true) }

    // OUTRAS PENDÊNCIAS (não confundir com outros tipos de imóvel)

    val outrasPendencias = visitasUteis.count { val p = it.pendencia.trim()
        p.isNotBlank() &&
        !p.equals("Fechado", ignoreCase = true) &&
        !p.equals("Recusado", ignoreCase = true) &&
        !p.equals("Abandonado", ignoreCase = true)
    }

    val totalPendencias = fechadas + recusadas + abandonadas + outrasPendencias

    val imoveisComFoco = visitasUteis.count { it.foco }

    val totalEliminados = visitasUteis.sumOf { it.eliminados }
    val totalTratados = visitasUteis.sumOf { it.tratados }

    val totalA1 = visitasUteis.sumOf { it.a1 }
    val totalA2 = visitasUteis.sumOf { it.a2 }
    val totalB = visitasUteis.sumOf { it.b }
    val totalC = visitasUteis.sumOf { it.c }
    val totalD1 = visitasUteis.sumOf { it.d1 }
    val totalD2 = visitasUteis.sumOf { it.d2 }
    val totalE = visitasUteis.sumOf { it.e }

    val totalGramas = visitasUteis.sumOf { it.larvicidaGramas }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {


        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoltar) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color(0xFF00BCD4)
                )
            }

            Text(
                text = "Resumo Semanal",
                color = Color(0xFF00BCD4),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF66666B)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E293B))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
            ) {

                Text(
                    "Período da semana",
                    color = Color(0xFF00BCD4),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    "${inicioSemana.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}" +
                            " a ${fimSemana.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    color = Color(0xFFFFFFFF),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(20.dp))

                LinhaResumo("Informados", totalInformados.toString())
                LinhaResumo("Inspecionados", totalInspecionados.toString())
                LinhaResumo("Com foco", imoveisComFoco.toString())
                LinhaResumo("Total pendências", totalPendencias.toString())
                LinhaResumo("• Fechadas", fechadas.toString())
                LinhaResumo("• Recusadas", recusadas.toString())
                LinhaResumo("• Abandonadas", abandonadas.toString())
                LinhaResumo("• Outras pendências", outrasPendencias.toString())

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFF26304F)
                )

                LinhaResumo("Depósitos tratados", totalTratados.toString())
                LinhaResumo("• A1", totalA1.toString())
                LinhaResumo("• A2", totalA2.toString())
                LinhaResumo("• B", totalB.toString())
                LinhaResumo("• C", totalC.toString())
                LinhaResumo("• D1", totalD1.toString())
                LinhaResumo("• D2", totalD2.toString())
                LinhaResumo("• E", totalE.toString())
                LinhaResumo(
                    "Larvicida utilizado",
                    String.format(Locale.US, "%.1f g", totalGramas))
                LinhaResumo("Depósitos eliminados", totalEliminados.toString()
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { /* PDF semanal depois */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C5CFF)
            )
        ) {
            Text(
                "Gerar PDF Semanal",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LinhaResumo(titulo: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(titulo, color = Color.White, fontSize = 16.sp)
        Text(
            valor,
            color = Color(0xFF00BCD4),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(Modifier.height(10.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaNovoBoletim(
    atividade: String,
    onAtividadeChange: (String) -> Unit,
    categoria: String,
    onCategoriaChange: (String) -> Unit,
    agente: String,
    onAgenteChange: (String) -> Unit,
    supervisor: String,
    onSupervisorChange: (String) -> Unit,
    local: String,
    onLocalChange: (String) -> Unit,
    cicloAno: String,
    onCicloChange: (String) -> Unit,
    onIniciar: () -> Unit,
    onVoltar: () -> Unit
) {
    BackHandler {
        onVoltar()
    }


    var atividadeExpanded by remember { mutableStateOf(false) }
    var categoriaExpanded by remember { mutableStateOf(false) }
    var mostrarErro by remember { mutableStateOf(false) }

    val dataAtual = remember {
        LocalDate.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
    }

    val Bg = Color(0xFF0F172A)

    val campoColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFF6EA8FF),
        unfocusedBorderColor = Color(0xFF5E6A82),
        focusedLabelColor = Color(0xFF6EA8FF),
        unfocusedLabelColor = Color(0xFF9AA5BA),
        cursorColor = Color(0xFF6EA8FF)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)

    )
    {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Boletim Diário",
                    color = Color(0xFF00BCD4),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Confira os dados antes de iniciar o boletim",
                    color = Color(0xFF9AA5BA),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }


        item {
            OutlinedTextField(
                value = agente,
                onValueChange = onAgenteChange,
                label = { Text("Agente") },
                shape = RoundedCornerShape(30.dp),
                colors = campoColors,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = supervisor,
                onValueChange = onSupervisorChange,
                label = { Text("Supervisor") },
                shape = RoundedCornerShape(30.dp),
                colors = campoColors,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = local,
                    onValueChange = onLocalChange,
                    label = { Text("Localidade") },
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.weight(0.9f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6EA8FF),
                        unfocusedBorderColor = Color(0xFF65708A),
                        focusedLabelColor = Color(0xFF6EA8FF),
                        unfocusedLabelColor = Color(0xFFAAB3C8)
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = categoriaExpanded,
                    onExpandedChange = {
                        categoriaExpanded =
                            !categoriaExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        shape = RoundedCornerShape(28.dp),
                        colors = campoColors,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .width(113.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoriaExpanded,
                        onDismissRequest = { categoriaExpanded = false }
                    ) {
                        listOf("Bairro", "Rural").forEach {
                            DropdownMenuItem(
                                text = { Text(it) },

                                onClick = {
                                    onCategoriaChange(it)
                                    categoriaExpanded = false
                                }
                            )
                        }
                    }
                }

            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                ExposedDropdownMenuBox(
                    expanded = atividadeExpanded,
                    onExpandedChange = {
                        atividadeExpanded =
                            !atividadeExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = atividade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Atividade") },
                        shape = RoundedCornerShape(30.dp),
                        colors = campoColors,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded =
                                    atividadeExpanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .width(157.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = atividadeExpanded,
                        onDismissRequest = {
                            atividadeExpanded = false
                        }
                    ) {
                        listOf("Tratamento", "Ação").forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    onAtividadeChange(it)
                                    atividadeExpanded = false
                                }
                            )
                        }
                    }
                }

                var cicloExpandido by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = cicloExpandido,
                    onExpandedChange = {
                        cicloExpandido = !cicloExpandido
                    }
                ) {

                    OutlinedTextField(
                        value = when (cicloAno) {
                            "1" -> "1º"
                            "2" -> "2º"
                            "3" -> "3º"
                            "4" -> "4º"
                            "5" -> "5º"
                            "6" -> "6º"
                            else -> ""
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ciclo") },

                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = cicloExpandido
                            )
                        },

                        shape = RoundedCornerShape(30.dp),
                        colors = campoColors,

                        modifier = Modifier
                            .width(120.dp)
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = cicloExpandido,
                        onDismissRequest = {
                            cicloExpandido = false
                        }
                    ) {

                        listOf(
                            "1º",
                            "2º",
                            "3º",
                            "4º",
                            "5º",
                            "6º"
                        ).forEachIndexed { index, ciclo ->

                            DropdownMenuItem(
                                text = {
                                    Text(ciclo)
                                },
                                onClick = {
                                    onCicloChange((index + 1).toString())
                                    cicloExpandido = false
                                }
                            )
                        }
                    }
                }

            }
        }

        item {
            OutlinedTextField(
                value = dataAtual,
                onValueChange = {},
                readOnly = true,
                shape = RoundedCornerShape(30.dp),
                colors = campoColors,
                modifier = Modifier.width(120.dp)
            )
        }


        item {
            if (mostrarErro) {
                Text(
                    text = "Preencha todos os dados do boletim.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }


        item {
            Spacer(Modifier.height(70.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        if (
                            local.trim().isEmpty() ||
                            agente.trim().isEmpty() ||
                            supervisor.trim().isEmpty() ||
                            cicloAno.trim().isEmpty()
                        ) {
                            mostrarErro = true
                        } else {
                            mostrarErro = false
                            onIniciar()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.48f)
                        .height(65.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5E3A9E)
                    )
                ) {
                    Text(
                        text = "Iniciar Boletim",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

    }
}
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TelaVisitas(
        atividade: String,
        categoria: String,
        agente: String,
        supervisor: String,
        localidade: String,
        ciclo: String,
        visitasIniciais: List<Visita>,
        onVoltar: () -> Unit

    ) {

        BackHandler {
            onVoltar()
        }

        val context = LocalContext.current
        val Bg = Color(0xFF0F172A)

        var visitas by remember { mutableStateOf(visitasIniciais) }
        var indiceEditando by remember { mutableStateOf<Int?>(null) }

        var quarteirao by remember { mutableStateOf("") }
        var rua by remember { mutableStateOf("") }
        LaunchedEffect(visitasIniciais) {
            if (visitasIniciais.isNotEmpty()) {
                val ultima = visitasIniciais.last()
                quarteirao = ultima.quarteirao
                rua = ultima.rua
            }
        }
        var numero by remember { mutableStateOf("") }
        var sequencia by remember { mutableStateOf("") }
        var complemento by remember { mutableStateOf("") }

        var tipoImovel by remember { mutableStateOf("Residência") }

        var inspecionado by remember { mutableStateOf(true) }
        var possuiPendencia by remember { mutableStateOf(false) }
        var pendencia by remember { mutableStateOf("") }
        var observacao by remember { mutableStateOf("") }
        var pendenciaExpanded by remember { mutableStateOf(false) }

        var foco by remember { mutableStateOf(false) }

        var depositosEliminados by remember { mutableIntStateOf(0) }

        var tratado by remember { mutableStateOf(false) }

        var a1Tratado by remember { mutableIntStateOf(0) }
        var a2Tratado by remember { mutableIntStateOf(0) }
        var bTratado by remember { mutableIntStateOf(0) }
        var cTratado by remember { mutableIntStateOf(0) }
        var d1Tratado by remember { mutableIntStateOf(0) }
        var d2Tratado by remember { mutableIntStateOf(0) }
        var eTratado by remember { mutableIntStateOf(0) }
        var gramas by remember { mutableDoubleStateOf(0.0) }
        val scope = rememberCoroutineScope()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                Spacer(Modifier.height(10.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    OutlinedTextField(
                        value = quarteirao,
                        onValueChange = { quarteirao = it },
                        label = { Text("Quarteirão") },
                        modifier = Modifier.weight(0.44f),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF6EA8FF),
                            unfocusedBorderColor = Color(0xFF65708A),
                            focusedLabelColor = Color(0xFF6EA8FF),
                            unfocusedLabelColor = Color(0xFFAAB3C8)
                        )
                    )

                    OutlinedTextField(
                        value = rua,
                        onValueChange = { rua = it },
                        label = { Text("Rua") },
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF6EA8FF),
                            unfocusedBorderColor = Color(0xFF65708A),
                            focusedLabelColor = Color(0xFF6EA8FF),
                            unfocusedLabelColor = Color(0xFFAAB3C8)
                        )
                    )

                }
            }


            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    OutlinedTextField(
                        value = numero,
                        onValueChange = { numero = it },
                        label = { Text("Nº") },
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF6EA8FF),
                            unfocusedBorderColor = Color(0xFF65708A),
                            focusedLabelColor = Color(0xFF6EA8FF),
                            unfocusedLabelColor = Color(0xFFAAB3C8)
                        )
                    )

                    OutlinedTextField(
                        value = sequencia,
                        onValueChange = { sequencia = it },
                        label = { Text("Seq.") },
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF6EA8FF),
                            unfocusedBorderColor = Color(0xFF65708A),
                            focusedLabelColor = Color(0xFF6EA8FF),
                            unfocusedLabelColor = Color(0xFFAAB3C8)
                        )
                    )

                    OutlinedTextField(
                        value = complemento,
                        onValueChange = { complemento = it },
                        label = { Text("Com.") },
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF6EA8FF),
                            unfocusedBorderColor = Color(0xFF65708A),
                            focusedLabelColor = Color(0xFF6EA8FF),
                            unfocusedLabelColor = Color(0xFFAAB3C8)
                        )
                    )

                    Button(
                        onClick = {

                            val uri = Uri.parse("geo:0,0?q=minha+localizacao")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.setPackage("com.google.android.apps.maps")
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(50.dp)

                    ) {
                        Text(
                            text = "Maps",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }


            item {
                Text("Tipo do imóvel",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                    )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        shape = RoundedCornerShape(50.dp),
                        selected = tipoImovel == "Residência",
                        onClick = { tipoImovel = "Residência" },
                        label = {
                            Text(
                                text = "Residência",
                                color = Color.White
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            labelColor = Color(0xFFD0C8DD),
                            selectedContainerColor = Color(0xFF5E3A9E),
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = tipoImovel == "Residência",
                            borderColor = Color(0xFF77738A),
                            selectedBorderColor = Color(0xFF9C6CFF),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 2.dp
                        )
                    )

                    FilterChip(
                        selected = tipoImovel == "Terreno",
                        onClick = { tipoImovel = "Terreno" },

                        label = {
                            Text(
                                text = "Terreno",
                                color = Color.White
                            )
                        },

                        shape = RoundedCornerShape(50.dp),

                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            labelColor = Color(0xFFD0C8DD),
                            selectedContainerColor = Color(0xFF5E3A9E),
                            selectedLabelColor = Color.White
                        ),

                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = tipoImovel == "Terreno",
                            borderColor = Color(0xFF77738A),
                            selectedBorderColor = Color(0xFF9C6CFF),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 2.dp
                        )
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = tipoImovel == "Comércio",
                        onClick = { tipoImovel = "Comércio" },

                        label = {
                            Text(
                                text = "Comércio",
                                color = Color.White
                            )
                        },

                        shape = RoundedCornerShape(50.dp),

                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            labelColor = Color(0xFFD0C8DD),
                            selectedContainerColor = Color(0xFF5E3A9E),
                            selectedLabelColor = Color.White
                        ),

                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = tipoImovel == "Comércio",
                            borderColor = Color(0xFF77738A),
                            selectedBorderColor = Color(0xFF9C6CFF),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 2.dp
                        )
                    )

                    FilterChip(
                        selected = tipoImovel == "Outros",
                        onClick = { tipoImovel = "Outros" },

                        label = {
                            Text(
                                text = "Outros",
                                color = Color.White
                            )
                        },

                        shape = RoundedCornerShape(50.dp),

                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            labelColor = Color(0xFFD0C8DD),
                            selectedContainerColor = Color(0xFF5E3A9E),
                            selectedLabelColor = Color.White
                        ),

                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = tipoImovel == "Outros",
                            borderColor = Color(0xFF77738A),
                            selectedBorderColor = Color(0xFF9C6CFF),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 2.dp
                        )
                    )
                }
            }

            item { HorizontalDivider() }

            item {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = inspecionado,
                            onCheckedChange = { checked ->
                                inspecionado = checked
                                if (checked) {
                                    possuiPendencia = false
                                    pendencia = ""
                                    observacao = ""
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF7E57C2),
                                uncheckedColor = Color(0xFF77738A),
                                checkmarkColor = Color.White
                            )
                        )
                        Text(
                            "Imóvel inspecionado",
                            color = Color(0xFFE0DCE8)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = possuiPendencia,
                            onCheckedChange = { checked ->
                                possuiPendencia = checked
                                if (checked) {
                                    inspecionado = false
                                } else {
                                    pendencia = ""
                                    observacao = ""
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF7E57C2),
                                uncheckedColor = Color(0xFF77738A),
                                checkmarkColor = Color.White
                            )
                        )
                        Text(
                            "Pendência",
                            color = Color(0xFFE0DCE8)
                        )
                    }
                    if (possuiPendencia) {
                        ExposedDropdownMenuBox(
                            expanded = pendenciaExpanded,
                            onExpandedChange = {
                                pendenciaExpanded = !pendenciaExpanded
                            }
                        ) {
                            OutlinedTextField(
                                value = pendencia,
                                onValueChange = {},
                                readOnly = true,

                                label = {
                                    Text("Tipo de pendência")
                                },

                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = pendenciaExpanded
                                    )
                                },

                                shape = RoundedCornerShape(20.dp),

                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,

                                    focusedLabelColor = Color(0xFFB388FF),
                                    unfocusedLabelColor = Color(0xFFB0A9C0),

                                    focusedBorderColor = Color(0xFF9C6CFF),
                                    unfocusedBorderColor = Color(0xFF77738A),

                                    focusedTrailingIconColor = Color(0xFFB388FF),
                                    unfocusedTrailingIconColor = Color(0xFF77738A)
                                ),

                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = pendenciaExpanded,
                                onDismissRequest = {
                                    pendenciaExpanded = false
                                },
                                modifier = Modifier.clip(
                                    RoundedCornerShape(20.dp)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                containerColor = Color(0xFF11182B),
                                border = BorderStroke(
                                    1.dp,
                                    Color(0xFF77738A)
                                )
                            ) {
                                listOf(
                                    "Fechado",
                                    "Recusado",
                                    "Abandonado",
                                    "Outros"
                                ).forEach { item ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = item,
                                                color = Color(0xFFE0DCE8)
                                            )
                                        },
                                        onClick = {
                                            pendencia = item
                                            pendenciaExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

            }


            item { HorizontalDivider() }
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = tratado,
                            onCheckedChange = { tratado = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF7E57C2),
                                uncheckedColor = Color(0xFF77738A),
                                checkmarkColor = Color.White
                            )
                        )

                        Text(
                            text = "Depósitos tratados",
                            color = Color.White
                        )
                    }

                    if (tratado) {

                        HorizontalDivider()

                        Text(
                            text = "Quantidade por tipo",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )

                        // A1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "A1",
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )

                            Button(
                                onClick = {
                                    if (a1Tratado > 0) a1Tratado--
                                }
                            ) {
                                Text("-")
                            }

                            Text(
                                text = a1Tratado.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Button(
                                onClick = { a1Tratado++ }
                            ) {
                                Text("+")
                            }
                        }

                        // A2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "A2",
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )

                            Button(
                                onClick = {
                                    if (a2Tratado > 0) a2Tratado--
                                }
                            ) {
                                Text("-")
                            }

                            Text(
                                text = a2Tratado.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Button(
                                onClick = { a2Tratado++ }
                            ) {
                                Text("+")
                            }
                        }

                        // B
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "B",
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )

                            Button(
                                onClick = {
                                    if (bTratado > 0) bTratado--
                                }
                            ) {
                                Text("-")
                            }

                            Text(
                                text = bTratado.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Button(
                                onClick = { bTratado++ }
                            ) {
                                Text("+")
                            }
                        }

                        // C
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "C",
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )

                            Button(
                                onClick = {
                                    if (cTratado > 0) cTratado--
                                }
                            ) {
                                Text("-")
                            }

                            Text(
                                text = cTratado.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Button(
                                onClick = { cTratado++ }
                            ) {
                                Text("+")
                            }
                        }

                        // D1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "D1",
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )

                            Button(
                                onClick = {
                                    if (d1Tratado > 0) d1Tratado--
                                }
                            ) {
                                Text("-")
                            }

                            Text(
                                text = d1Tratado.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Button(
                                onClick = { d1Tratado++ }
                            ) {
                                Text("+")
                            }
                        }

                        // D2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "D2",
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )

                            Button(
                                onClick = {
                                    if (d2Tratado > 0) d2Tratado--
                                }
                            ) {
                                Text("-")
                            }

                            Text(
                                text = d2Tratado.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Button(
                                onClick = { d2Tratado++ }
                            ) {
                                Text("+")
                            }
                        }

                        // E
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "E",
                                modifier = Modifier.weight(1f),
                                color = Color.White
                            )

                            Button(
                                onClick = {
                                    if (eTratado > 0) eTratado--
                                }
                            ) {
                                Text("-")
                            }

                            Text(
                                text = eTratado.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Button(
                                onClick = { eTratado++ }
                            ) {
                                Text("+")
                            }
                        }

                        HorizontalDivider()

                        Text(
                            text = "Larvicida utilizado",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Button(
                                onClick = {
                                    if (gramas >= 0.5) {
                                        gramas -= 0.5
                                    }
                                }
                            ) {
                                Text("-")
                            }

                            Text(
                                text = "${gramas.toString().replace(".", ",")} g",
                                modifier = Modifier.padding(horizontal = 24.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Button(
                                onClick = {
                                    gramas += 0.5
                                }
                            ) {
                                Text("+")
                            }
                        }

                        Text(
                            text = "Total de depósitos tratados: ${
                                a1Tratado +
                                        a2Tratado +
                                        bTratado +
                                        cTratado +
                                        d1Tratado +
                                        d2Tratado +
                                        eTratado
                            }",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = foco,
                        onCheckedChange = { foco = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF7E57C2),
                            uncheckedColor = Color(0xFF77738A),
                            checkmarkColor = Color.White
                        )
                    )

                    Text(
                        text = "Foco encontrado",
                        color = Color.White
                    )
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = depositosEliminados > 0,
                            onCheckedChange = { marcado ->
                                if (!marcado) {
                                    depositosEliminados = 0
                                } else if (depositosEliminados == 0) {
                                    depositosEliminados = 1
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF7E57C2),
                                uncheckedColor = Color(0xFF77738A),
                                checkmarkColor = Color.White
                            )
                        )

                        Text(
                            text = "Depósitos eliminados",
                            color = Color.White
                        )
                    }

                    if (depositosEliminados > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (depositosEliminados > 1) {
                                        depositosEliminados--
                                    } else {
                                        depositosEliminados = 0
                                    }
                                }
                            ) {
                                Text("-")
                            }

                            Text(
                                text = depositosEliminados.toString(),
                                modifier = Modifier.padding(horizontal = 24.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Button(
                                onClick = {
                                    depositosEliminados++
                                }
                            ) {
                                Text("+")
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = observacao,
                    onValueChange = { observacao = it },
                    label = {
                        Text(
                            text = "Observações",
                            color = Color(0xFFB8B5C7)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF9C6CFF),
                        unfocusedBorderColor = Color(0xFF77738A),
                        focusedLabelColor = Color(0xFFB58CFF),
                        unfocusedLabelColor = Color(0xFFB8B5C7),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFB58CFF)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
            }


            item { HorizontalDivider() }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {

                            val tratamento = if (tratado) {
                                Tratamento(
                                    a1 = a1Tratado,
                                    a2 = a2Tratado,
                                    b = bTratado,
                                    c = cTratado,
                                    d1 = d1Tratado,
                                    d2 = d2Tratado,
                                    e = eTratado,
                                    gramas = gramas
                                )
                            } else {
                                null
                            }

                            val novaVisita = Visita(
                                cicloAno = ciclo,
                                quarteirao = quarteirao,
                                rua = rua,
                                numero = numero,
                                sequencia = sequencia,
                                complemento = complemento,
                                tipoImovel = tipoImovel,
                                inspecionado = inspecionado,
                                pendencia = pendencia,
                                foco = foco,
                                depositosEliminados = depositosEliminados,
                                tratamento = tratamento,
                                observacao = observacao
                            )
                            if (indiceEditando == null) {
                                visitas = visitas + novaVisita
                            } else {
                                visitas = visitas.toMutableList().also {
                                    it[indiceEditando!!] = novaVisita
                                }
                                indiceEditando = null
                            }

                            scope.launch {
                                val cabecalhoRascunho = CabecalhoBoletim(
                                    nome = agente,
                                    supervisor = supervisor,
                                    data = LocalDate.now() .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                    localidade = localidade,
                                    quarteirao = quarteirao,
                                    atividade = atividade,
                                    categoria = categoria,
                                    ciclo = ciclo
                                )
                                val rascunho = BoletimRascunho(
                                    cabecalho = cabecalhoRascunho,
                                    visitas = visitas,
                                    visitaAtual = novaVisita,
                                    indiceAtual = if (indiceEditando == null) visitas.size
                                    else indiceEditando!!
                                )
                                RascunhoStore.salvar(context, rascunho)
                            }

                            indiceEditando = null
                            // volta para o padrão da próxima visita
                            inspecionado = true
                            possuiPendencia = false
                            pendencia = ""
                            observacao = ""
                            numero = ""
                            sequencia = ""
                            complemento = ""
                            foco = false
                            depositosEliminados = 0
                            tratado = false
                            a1Tratado = 0
                            a2Tratado = 0
                            bTratado = 0
                            cTratado = 0
                            d1Tratado = 0
                            d2Tratado = 0
                            eTratado = 0
                            gramas = 0.0
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.48f)
                            .height(65.dp),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            text = if (indiceEditando == null) "Salvar Visita" else
                                "Atualizar Visita",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            val cabecalho = CabecalhoBoletim(
                                nome = agente,
                                supervisor = supervisor,
                                data = LocalDate.now()
                                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                localidade = localidade,
                                quarteirao = quarteirao,
                                atividade = atividade,
                                categoria = categoria,
                                ciclo = ciclo
                            )
                            val visitasComObservacao = visitas.filter { it.observacao.isNotBlank() }
                            gerarBoletimPdf(context, cabecalho, visitas)

                            scope.launch {
                                AppDatabase.get(context).visitaDao().inserirTodas(visitas.map {
                                    it.toEntity() })
                            }

                            if (visitasComObservacao.isNotEmpty()) {
                                gerarObservacoesPdf(context, cabecalho, visitasComObservacao)
                            }
                            scope.launch {
                                RascunhoStore.limpar(context)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.56f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text("Gerar PDF do Boletim")
                    }
                }
            }

            item { HorizontalDivider() }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Visitas do dia",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
            }

            itemsIndexed(visitas) { index, visita ->

                Card(modifier = Modifier.fillMaxWidth()) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                quarteirao = visita.quarteirao
                                rua = visita.rua
                                numero = visita.numero
                                sequencia = visita.sequencia
                                complemento = visita.complemento
                                tipoImovel = visita.tipoImovel
                                inspecionado = visita.inspecionado
                                pendencia = visita.pendencia
                                foco = visita.foco
                                depositosEliminados = visita.depositosEliminados
                                a1Tratado = visita.tratamento?.a1 ?: 0
                                a2Tratado = visita.tratamento?.a2 ?: 0
                                bTratado = visita.tratamento?.b ?: 0
                                cTratado = visita.tratamento?.c ?: 0
                                d1Tratado = visita.tratamento?.d1 ?: 0
                                d2Tratado = visita.tratamento?.d2 ?: 0
                                eTratado = visita.tratamento?.e ?: 0
                                gramas = visita.tratamento?.gramas ?: 0.0
                                indiceEditando = index
                            }
                        ) {
                            Text("Editar")
                        }
                        TextButton(
                            onClick = {
                                visitas = visitas.filterIndexed { i, _ -> i != index }
                            }
                        ) {
                            Text("Excluir", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = buildString {
                                append(visita.numero)
                                if (visita.sequencia.isNotBlank()) {
                                    append(" - Seq. ${visita.sequencia}")
                                }
                                if (visita.complemento.isNotBlank()) {
                                    append(" - Comp. ${visita.complemento}")
                                }
                           },
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text("${visita.rua} • Q${visita.quarteirao}")

                        Text("Tipo: ${visita.tipoImovel}")

                        Text(
                            if (visita.inspecionado)
                                "Situação: Inspecionado"
                            else
                                "Situação: ${visita.pendencia}"
                        )

                        if (visita.foco) {
                            Text("Foco encontrado")
                        }

                        if (visita.depositosEliminados > 0) {
                            Text("Eliminados: ${visita.depositosEliminados}")

                        }

                        if (visita.observacao.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Obs: ${visita.observacao}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

fun Visita.toEntity(): VisitaEntity {
    return VisitaEntity(
        data = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
        ciclo = cicloAno,
        quarteirao = quarteirao,
        rua = rua,
        numero = numero,
        sequencia = sequencia,
        complemento = complemento,
        tipoImovel = tipoImovel,
        inspecionado = inspecionado,
        pendencia = pendencia,
        foco = foco,

        a1 = tratamento?.a1 ?: 0,
        a2 = tratamento?.a2 ?: 0,
        b = tratamento?.b ?: 0,
        c = tratamento?.c ?: 0,
        d1 = tratamento?.d1 ?: 0,
        d2 = tratamento?.d2 ?: 0,
        e = tratamento?.e ?: 0,
        eliminados = depositosEliminados,
        tratados = tratamento?.total ?: 0,
        larvicidaGramas = tratamento?.gramas ?: 0.0

    )
}

@Composable
fun TelaRG(
    ciclo: String,
    onVoltar: () -> Unit
) {
    BackHandler {
        onVoltar()
    }

    val context = LocalContext.current

    var visitas by remember {
        mutableStateOf(emptyList<VisitaEntity>())
    }

    LaunchedEffect(Unit) {
        visitas = AppDatabase
            .get(context)
            .visitaDao()
            .listarPorCiclo(ciclo)
    }

    val Bg = Color(0xFF0F172A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoltar) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color(0xFF00BCD4)
                )
            }

            Text(
                text = "Quarteirões registrados",
                color = Color(0xFF00BCD4),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val quarteiroes = visitas
            .map { it.quarteirao }
            .distinct()
            .sorted()

        if (quarteiroes.isEmpty()) {

            Text(
                text = "Nenhum quarteirão encontrado.",
                color = Color.LightGray,
                fontSize = 16.sp
            )

        } else {

            quarteiroes.forEach { quarteirao ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E3157)
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Quarteirão $quarteirao",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        val total = visitas.count {
                            it.quarteirao == quarteirao
                        }

                        Text(
                            text = "$total imóvel(is) registrado(s)",
                            color = Color(0xFF00BCD4),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}