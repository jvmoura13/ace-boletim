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
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch





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

data class Eliminados(
    val a1: Int,
    val a2: Int,
    val b: Int,
    val c: Int,
    val d1: Int,
    val d2: Int,
    val e: Int
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

    val eliminadosA1: Int,
    val eliminadosA2: Int,
    val eliminadosB: Int,
    val eliminadosC: Int,
    val eliminadosD1: Int,
    val eliminadosD2: Int,
    val eliminadosE: Int,

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
    val colNum = 450f
    val colSeq = 530f
    val colComp = 610f
    val colTipo = 690f
    val colInsp = 770f
    val colPend = 850f
    val colFoco = 930f
    val colA1 = 1010f
    val colA2 = 1090f
    val colB = 1160f
    val colC = 1230f
    val colD1 = 1300f
    val colD2 = 1370f
    val colE = 1440f
    val colGram = 1510f
    val colTrat = 1580f

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
            paint.textSize = 17f
            canvasAtual.drawText("BOLETIM DE VISITAS (continuação)", 620f, 50f, paint)
            paint.textSize = 17f
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
            "PE" -> "PE"
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
                "PE" -> "PE"
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
    canvasAtual.drawText("RESUMO DO BOLETIM", 40f, y.toFloat(), paint)

    y += 35

// ======================================================
// FUNÇÕES AUXILIARES
// ======================================================

    fun ehPE(tipo: String): Boolean {
        return tipo.equals("PE", ignoreCase = true)
    }

    fun ehTerreno(tipo: String): Boolean {
        return tipo.equals("Terreno", ignoreCase = true) ||
                tipo.equals("TB", ignoreCase = true)
    }

    fun ehResidencia(tipo: String): Boolean {
        return tipo.equals("Residência", ignoreCase = true)
    }

    fun ehComercio(tipo: String): Boolean {
        return tipo.equals("Comércio", ignoreCase = true)
    }

    fun ehOutro(tipo: String): Boolean {
        return !ehPE(tipo) &&
                !ehResidencia(tipo) &&
                !ehComercio(tipo) &&
                !ehTerreno(tipo)
    }

    fun totalDepositos(visita: VisitaEntity): Int {
        return visita.a1 +
                visita.a2 +
                visita.b +
                visita.c +
                visita.d1 +
                visita.d2 +
                visita.e
    }

    fun totalEliminados(visita: VisitaEntity): Int {
        return visita.eliminadosA1 +
                visita.eliminadosA2 +
                visita.eliminadosB +
                visita.eliminadosC +
                visita.eliminadosD1 +
                visita.eliminadosD2 +
                visita.eliminadosE
    }

    fun desenharTituloTabela(titulo: String) {

        paint.textSize = 18f
        paint.isFakeBoldText = true

        canvasAtual.drawText(
            titulo,
            40f,
            y.toFloat(),
            paint
        )

        y += 10
    }

    fun desenharTabela(
        cabecalhos: List<String>,
        linhas: List<List<String>>,
        larguras: List<Float>,
        xInicial: Float = 40f,
        yInicial: Float = y.toFloat(),
        avancarY: Boolean = true
    ): Float {
        val alturaLinha = 25f
        val alturaCabecalho = 28f

        paint.textSize = 16f
        paint.style = Paint.Style.STROKE

        var x = xInicial
        var yAtual = yInicial

        // ---------- CABEÇALHO ----------

        paint.isFakeBoldText = true

        cabecalhos.forEachIndexed { indice, texto ->

            paint.style = Paint.Style.STROKE

            canvasAtual.drawRect(
                x,
                yAtual,
                x + larguras[indice],
                yAtual + alturaCabecalho,
                paint
            )

            paint.style = Paint.Style.FILL
            paint.isFakeBoldText = false

            canvasAtual.drawText(
                texto,
                x + 5f,
                yAtual + 19f,
                paint
            )

            paint.isFakeBoldText = true

            x += larguras[indice]
        }

        yAtual += alturaCabecalho

        // ---------- LINHAS ----------

        paint.isFakeBoldText = false

        linhas.forEach { linha ->

            x = xInicial

            linha.forEachIndexed { indice, valor ->

                paint.style = Paint.Style.STROKE

                canvasAtual.drawRect(
                    x,
                    yAtual,
                    x + larguras[indice],
                    yAtual + alturaLinha,
                    paint
                )

                paint.style = Paint.Style.FILL

                canvasAtual.drawText(
                    valor,
                    x + 5f,
                    yAtual + 18f,
                    paint
                )

                x += larguras[indice]
            }

            yAtual += alturaLinha
        }

        // Retorna a altura total ocupada pela tabela
        val alturaTotal = yAtual - yInicial

        if (avancarY) {
            y = yAtual.toInt() + 15
        }

        return alturaTotal
    }

    fun depositoTratado(
        tipo: String,
        deposito: (Visita) -> Int
    ): Int {
        return visitas
            .filter { visita ->
                !ehPE(visita.tipoImovel) &&
                        when (tipo) {
                            "Residência" -> ehResidencia(visita.tipoImovel)
                            "Comércio" -> ehComercio(visita.tipoImovel)
                            "TB" -> ehTerreno(visita.tipoImovel)
                            "Outros" -> ehOutro(visita.tipoImovel)
                            else -> false
                        }
            }
            .sumOf { visita -> deposito(visita) }
    }

    // ============================================================
// ============================================================
// RESUMO / FECHAMENTO DO BOLETIM
// ============================================================

// Finaliza a última página onde estavam as visitas
    pdf.finishPage(paginaAtual)

// ============================================================
// NOVA PÁGINA EXCLUSIVA PARA O FECHAMENTO
// ============================================================

    val resumoInfo = PdfDocument.PageInfo.Builder(
        1800,
        1200,
        999
    ).create()

    paginaAtual = pdf.startPage(resumoInfo)
    canvasAtual = paginaAtual.canvas

    val canvasResumo = canvasAtual

// ------------------------------------------------------------
// CONFIGURAÇÕES VISUAIS
// ------------------------------------------------------------

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1.2f
    paint.isFakeBoldText = false
    paint.textAlign = Paint.Align.LEFT

    val larguraPagina = 1800f

// ============================================================
// FUNÇÕES AUXILIARES DO RESUMO
// ============================================================

    fun desenharTituloResumo(
        titulo: String,
        x: Float,
        y: Float
    ) {
        paint.style = Paint.Style.FILL
        paint.textSize = 22f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT

        canvasResumo.drawText(
            titulo,
            x,
            y,
            paint
        )

        paint.isFakeBoldText = false
    }

    fun desenharTabelaResumo(
        x: Float,
        y: Float,
        cabecalhos: List<String>,
        linhas: List<List<String>>,
        larguras: List<Float>,
        alturaLinha: Float = 26f,
        tamanhoTexto: Float = 14f
    ) {
        require(cabecalhos.size == larguras.size)

        for (linhaIndice in 0..linhas.size) {

            val textoLinha = if (linhaIndice == 0) {
                cabecalhos
            } else {
                linhas[linhaIndice - 1]
            }

            var xAtual = x

            for (coluna in cabecalhos.indices) {

                val largura = larguras[coluna]

                val yTopo = y + (linhaIndice * alturaLinha)
                val yBase = yTopo + alturaLinha

                // ------------------------------------------------
                // BORDA DA CÉLULA
                // ------------------------------------------------

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f

                canvasResumo.drawRect(
                    xAtual,
                    yTopo,
                    xAtual + largura,
                    yBase,
                    paint
                )

                // ------------------------------------------------
                // TEXTO
                // ------------------------------------------------

                paint.style = Paint.Style.FILL
                paint.textSize = tamanhoTexto
                paint.isFakeBoldText = linhaIndice == 0 ||
                        textoLinha.firstOrNull() == "TOTAL" ||
                        textoLinha.firstOrNull() == "Geral"

                paint.textAlign =
                    if (coluna == 0) {
                        Paint.Align.LEFT
                    } else {
                        Paint.Align.CENTER
                    }

                val texto = textoLinha.getOrElse(coluna) { "" }

                val xTexto =
                    if (coluna == 0) {
                        xAtual + 6f
                    } else {
                        xAtual + (largura / 2f)
                    }

                val yTexto =
                    yTopo +
                            (alturaLinha / 2f) -
                            ((paint.ascent() + paint.descent()) / 2f)

                canvasResumo.drawText(
                    texto,
                    xTexto,
                    yTexto,
                    paint
                )

                xAtual += largura
            }
        }

        paint.isFakeBoldText = false
        paint.textAlign = Paint.Align.LEFT
    }

// ============================================================
// FUNÇÕES PARA CALCULAR OS DADOS
// ============================================================

    fun quantidadeTipo(tipo: String): Int {
        return visitas.count {
            !ehPE(it.tipoImovel) &&
                    when (tipo) {
                        "Residência" -> ehResidencia(it.tipoImovel)
                        "Comércio" -> ehComercio(it.tipoImovel)
                        "TB" -> ehTerreno(it.tipoImovel)
                        "Outros" -> ehOutro(it.tipoImovel)
                        else -> false
                    }
        }
    }

    fun inspecionadosTipo(tipo: String): Int {
        return visitas.count {
            !ehPE(it.tipoImovel) &&
                    it.inspecionado &&
                    when (tipo) {
                        "Residência" -> ehResidencia(it.tipoImovel)
                        "Comércio" -> ehComercio(it.tipoImovel)
                        "TB" -> ehTerreno(it.tipoImovel)
                        "Outros" -> ehOutro(it.tipoImovel)
                        else -> false
                    }
        }
    }

    fun pendenciaTipo(
        tipo: String,
        pendencia: String
    ): Int {
        return visitas.count {
            !ehPE(it.tipoImovel) &&
                    when (tipo) {
                        "Residência" -> ehResidencia(it.tipoImovel)
                        "Comércio" -> ehComercio(it.tipoImovel)
                        "TB" -> ehTerreno(it.tipoImovel)
                        "Outros" -> ehOutro(it.tipoImovel)
                        else -> false
                    } &&
                    it.pendencia.equals(
                        pendencia,
                        ignoreCase = true
                    )
        }
    }

    fun depositoTipo(
        tipo: String,
        deposito: (Tratamento) -> Int
    ): Int {

        return visitas.sumOf { visita ->

            if (
                ehPE(visita.tipoImovel) ||
                when (tipo) {
                    "Residência" -> !ehResidencia(visita.tipoImovel)
                    "Comércio" -> !ehComercio(visita.tipoImovel)
                    "TB" -> !ehTerreno(visita.tipoImovel)
                    "Outros" -> !ehOutro(visita.tipoImovel)
                    else -> true
                }
            ) {
                0
            } else {
                visita.tratamento?.let {
                    deposito(it)
                } ?: 0
            }
        }
    }

    fun eliminadosTipo(
        tipo: String,
        deposito: (Visita) -> Int
    ): Int {

        return visitas.sumOf { visita ->

            if (
                ehPE(visita.tipoImovel) ||
                when (tipo) {
                    "Residência" -> !ehResidencia(visita.tipoImovel)
                    "Comércio" -> !ehComercio(visita.tipoImovel)
                    "TB" -> !ehTerreno(visita.tipoImovel)
                    "Outros" -> !ehOutro(visita.tipoImovel)
                    else -> true
                }
            ) {
                0
            } else {
                deposito(visita)
            }
        }
    }

// ============================================================
// TÍTULO PRINCIPAL
// ============================================================

    paint.style = Paint.Style.FILL
    paint.textSize = 30f
    paint.isFakeBoldText = true
    paint.textAlign = Paint.Align.CENTER

    canvasResumo.drawText(
        "RESUMO DO BOLETIM",
        larguraPagina / 2f,
        45f,
        paint
    )

    paint.isFakeBoldText = false
    paint.textAlign = Paint.Align.LEFT

// ============================================================
// INFORMADOS
// ============================================================

    val informadosResidencias = quantidadeTipo("Residência")
    val informadosComercios = quantidadeTipo("Comércio")
    val informadosTerrenos = quantidadeTipo("TB")
    val informadosOutros = quantidadeTipo("Outros")

    val totalInformados =
        informadosResidencias +
                informadosComercios +
                informadosTerrenos +
                informadosOutros

    desenharTituloResumo(
        "INFORMADOS",
        40f,
        85f
    )

    desenharTabelaResumo(
        x = 40f,
        y = 95f,
        cabecalhos = listOf(
            "Tipo",
            "Quantidade"
        ),
        linhas = listOf(
            listOf("Residências", informadosResidencias.toString()),
            listOf("Comércios", informadosComercios.toString()),
            listOf("Terrenos (TB)", informadosTerrenos.toString()),
            listOf("Outros", informadosOutros.toString()),
            listOf("TOTAL", totalInformados.toString())
        ),
        larguras = listOf(
            190f,
            100f
        )
    )

// ============================================================
// INSPECIONADOS
// ============================================================

    val inspecionadosResidencias =
        inspecionadosTipo("Residência")

    val inspecionadosComercios =
        inspecionadosTipo("Comércio")

    val inspecionadosTerrenos =
        inspecionadosTipo("TB")

    val inspecionadosOutros =
        inspecionadosTipo("Outros")

    val totalInspecionados =
        inspecionadosResidencias +
                inspecionadosComercios +
                inspecionadosTerrenos +
                inspecionadosOutros

    desenharTituloResumo(
        "TRABALHADOS",
        350f,
        85f
    )

    desenharTabelaResumo(
        x = 350f,
        y = 95f,
        cabecalhos = listOf(
            "Tipo",
            "Quantidade"
        ),
        linhas = listOf(
            listOf("Residências", inspecionadosResidencias.toString()),
            listOf("Comércios", inspecionadosComercios.toString()),
            listOf("Terrenos (TB)", inspecionadosTerrenos.toString()),
            listOf("Outros", inspecionadosOutros.toString()),
            listOf("TOTAL", totalInspecionados.toString())
        ),
        larguras = listOf(
            190f,
            100f
        )
    )

// ============================================================
// PENDÊNCIAS
// ============================================================

    val fechadosResidencias =
        pendenciaTipo("Residência", "Fechado")

    val fechadosComercios =
        pendenciaTipo("Comércio", "Fechado")

    val fechadosTerrenos =
        pendenciaTipo("TB", "Fechado")

    val fechadosOutros =
        pendenciaTipo("Outros", "Fechado")

    val recusadosResidencias =
        pendenciaTipo("Residência", "Recusado")

    val recusadosComercios =
        pendenciaTipo("Comércio", "Recusado")

    val recusadosTerrenos =
        pendenciaTipo("TB", "Recusado")

    val recusadosOutros =
        pendenciaTipo("Outros", "Recusado")

    val totalFechados =
        fechadosResidencias +
                fechadosComercios +
                fechadosTerrenos +
                fechadosOutros

    val totalRecusados =
        recusadosResidencias +
                recusadosComercios +
                recusadosTerrenos +
                recusadosOutros

    val totalPendencias =
        totalFechados +
                totalRecusados

    desenharTituloResumo(
        "PENDÊNCIAS",
        660f,
        85f
    )

    desenharTabelaResumo(
        x = 660f,
        y = 95f,
        cabecalhos = listOf(
            "Tipo",
            "Fechados",
            "Recusados",
            "Pendências"
        ),
        linhas = listOf(
            listOf(
                "Residências",
                fechadosResidencias.toString(),
                recusadosResidencias.toString(),
                (fechadosResidencias + recusadosResidencias).toString()
            ),
            listOf(
                "Comércios",
                fechadosComercios.toString(),
                recusadosComercios.toString(),
                (fechadosComercios + recusadosComercios).toString()
            ),
            listOf(
                "Terrenos (TB)",
                fechadosTerrenos.toString(),
                recusadosTerrenos.toString(),
                (fechadosTerrenos + recusadosTerrenos).toString()
            ),
            listOf(
                "Outros",
                fechadosOutros.toString(),
                recusadosOutros.toString(),
                (fechadosOutros + recusadosOutros).toString()
            ),
            listOf(
                "TOTAL",
                totalFechados.toString(),
                totalRecusados.toString(),
                totalPendencias.toString()
            )
        ),
        larguras = listOf(
            170f,
            100f,
            100f,
            110f
        )
    )

// ============================================================
// IMÓVEIS TRATADOS
// ============================================================

    val residenciasTratadas = visitas.count {
        !ehPE(it.tipoImovel) &&
                ehResidencia(it.tipoImovel) &&
                (it.tratamento?.total ?: 0) > 0
    }

    val comerciosTratados = visitas.count {
        !ehPE(it.tipoImovel) &&
                ehComercio(it.tipoImovel) &&
                (it.tratamento?.total ?: 0) > 0
    }

    val terrenosTratados = visitas.count {
        !ehPE(it.tipoImovel) &&
                ehTerreno(it.tipoImovel) &&
                (it.tratamento?.total ?: 0) > 0
    }

    val outrosTratados = visitas.count {
        !ehPE(it.tipoImovel) &&
                ehOutro(it.tipoImovel) &&
                (it.tratamento?.total ?: 0) > 0
    }

    val totalImoveisTratados =
        residenciasTratadas +
                comerciosTratados +
                terrenosTratados +
                outrosTratados

    desenharTituloResumo(
        "IMÓVEIS TRATADOS",
        40f,
        275f
    )

    desenharTabelaResumo(
        x = 40f,
        y = 285f,
        cabecalhos = listOf(
            "Tipo",
            "Tratados"
        ),
        linhas = listOf(
            listOf("Residências", residenciasTratadas.toString()),
            listOf("Comércios", comerciosTratados.toString()),
            listOf("Terrenos (TB)", terrenosTratados.toString()),
            listOf("Outros", outrosTratados.toString()),
            listOf("TOTAL", totalImoveisTratados.toString())
        ),
        larguras = listOf(
            190f,
            100f
        )
    )

// ============================================================
// FOCOS
// ============================================================

    val focosResidencias = visitas.count {
        !ehPE(it.tipoImovel) &&
                it.foco &&
                ehResidencia(it.tipoImovel)
    }

    val focosComercios = visitas.count {
        !ehPE(it.tipoImovel) &&
                it.foco &&
                ehComercio(it.tipoImovel)
    }

    val focosTerrenos = visitas.count {
        !ehPE(it.tipoImovel) &&
                it.foco &&
                ehTerreno(it.tipoImovel)
    }

    val focosOutros = visitas.count {
        !ehPE(it.tipoImovel) &&
                it.foco &&
                ehOutro(it.tipoImovel)
    }

    val totalFocos =
        focosResidencias +
                focosComercios +
                focosTerrenos +
                focosOutros

    desenharTituloResumo(
        "FOCOS",
        350f,
        275f
    )

    desenharTabelaResumo(
        x = 350f,
        y = 285f,
        cabecalhos = listOf(
            "Tipo",
            "Quantidade"
        ),
        linhas = listOf(
            listOf("Residências", focosResidencias.toString()),
            listOf("Comércios", focosComercios.toString()),
            listOf("Terrenos (TB)", focosTerrenos.toString()),
            listOf("Outros", focosOutros.toString()),
            listOf("TOTAL", totalFocos.toString())
        ),
        larguras = listOf(
            190f,
            100f
        )
    )

    y += 45

// ============================================================
// DEPÓSITOS TRATADOS
// ============================================================

    fun depositoGeral(
        deposito: (Visita) -> Int
    ): Int {
        return visitas.sumOf { visita ->
            if (ehPE(visita.tipoImovel)) {
                0
            } else {
                deposito(visita)
            }
        }
    }

    val geralA1 = depositoGeral {
        it.tratamento?.a1 ?: 0
    }

    val geralA2 = depositoGeral {
        it.tratamento?.a2 ?: 0
    }

    val geralB = depositoGeral {
        it.tratamento?.b ?: 0
    }

    val geralC = depositoGeral {
        it.tratamento?.c ?: 0
    }

    val geralD1 = depositoGeral {
        it.tratamento?.d1 ?: 0
    }

    val geralD2 = depositoGeral {
        it.tratamento?.d2 ?: 0
    }

    val geralE = depositoGeral {
        it.tratamento?.e ?: 0
    }

    val geralTratados =
        geralA1 +
                geralA2 +
                geralB +
                geralC +
                geralD1 +
                geralD2 +
                geralE

    desenharTituloResumo(
        "DEPÓSITOS TRATADOS",
        40f,
        470f
    )

    desenharTabelaResumo(
        x = 40f,
        y = 480f,
        cabecalhos = listOf(
            "Tipo",
            "A1",
            "A2",
            "B",
            "C",
            "D1",
            "D2",
            "E",
            "TOTAL"
        ),
        linhas = listOf(
            listOf(
                "Residência",
                depositoTipo("Residência") { it.a1 }.toString(),
                depositoTipo("Residência") { it.a2 }.toString(),
                depositoTipo("Residência") { it.b }.toString(),
                depositoTipo("Residência") { it.c }.toString(),
                depositoTipo("Residência") { it.d1 }.toString(),
                depositoTipo("Residência") { it.d2 }.toString(),
                depositoTipo("Residência") { it.e }.toString(),
                depositoTipo("Residência") { it.total }.toString()
            ),
            listOf(
                "Comércio",
                depositoTipo("Comércio") { it.a1 }.toString(),
                depositoTipo("Comércio") { it.a2 }.toString(),
                depositoTipo("Comércio") { it.b }.toString(),
                depositoTipo("Comércio") { it.c }.toString(),
                depositoTipo("Comércio") { it.d1 }.toString(),
                depositoTipo("Comércio") { it.d2 }.toString(),
                depositoTipo("Comércio") { it.e }.toString(),
                depositoTipo("Comércio") { it.total }.toString()
            ),
            listOf(
                "TB",
                depositoTipo("TB") { it.a1 }.toString(),
                depositoTipo("TB") { it.a2 }.toString(),
                depositoTipo("TB") { it.b }.toString(),
                depositoTipo("TB") { it.c }.toString(),
                depositoTipo("TB") { it.d1 }.toString(),
                depositoTipo("TB") { it.d2 }.toString(),
                depositoTipo("TB") { it.e }.toString(),
                depositoTipo("TB") { it.total }.toString()
            ),
            listOf(
                "Outros",
                depositoTipo("Outros") { it.a1 }.toString(),
                depositoTipo("Outros") { it.a2 }.toString(),
                depositoTipo("Outros") { it.b }.toString(),
                depositoTipo("Outros") { it.c }.toString(),
                depositoTipo("Outros") { it.d1 }.toString(),
                depositoTipo("Outros") { it.d2 }.toString(),
                depositoTipo("Outros") { it.e }.toString(),
                depositoTipo("Outros") { it.total }.toString()
            ),
            listOf(
                "Geral",
                geralA1.toString(),
                geralA2.toString(),
                geralB.toString(),
                geralC.toString(),
                geralD1.toString(),
                geralD2.toString(),
                geralE.toString(),
                geralTratados.toString()
            )
        ),
        larguras = listOf(
            180f,
            100f,
            100f,
            100f,
            100f,
            100f,
            100f,
            100f,
            120f
        ),
        alturaLinha = 25f,
        tamanhoTexto = 13f
    )

// ============================================================
// DEPÓSITOS ELIMINADOS
// ============================================================

    fun eliminadosGeral(
        campo: (Visita) -> Int
    ): Int {
        return visitas.sumOf {
            if (ehPE(it.tipoImovel)) {
                0
            } else {
                campo(it)
            }
        }
    }

    val elimA1 = eliminadosGeral {
        it.eliminadosA1
    }

    val elimA2 = eliminadosGeral {
        it.eliminadosA2
    }

    val elimB = eliminadosGeral {
        it.eliminadosB
    }

    val elimC = eliminadosGeral {
        it.eliminadosC
    }

    val elimD1 = eliminadosGeral {
        it.eliminadosD1
    }

    val elimD2 = eliminadosGeral {
        it.eliminadosD2
    }

    val elimE = eliminadosGeral {
        it.eliminadosE
    }

    val totalEliminadosGeral =
        elimA1 +
                elimA2 +
                elimB +
                elimC +
                elimD1 +
                elimD2 +
                elimE

    desenharTituloResumo(
        "DEPÓSITOS ELIMINADOS",
        40f,
        655f
    )

    desenharTabelaResumo(
        x = 40f,
        y = 665f,
        cabecalhos = listOf(
            "Tipo",
            "A1",
            "A2",
            "B",
            "C",
            "D1",
            "D2",
            "E",
            "TOTAL"
        ),
        linhas = listOf(
            listOf(
                "Residência",
                eliminadosTipo("Residência") { it.eliminadosA1 }.toString(),
                eliminadosTipo("Residência") { it.eliminadosA2 }.toString(),
                eliminadosTipo("Residência") { it.eliminadosB }.toString(),
                eliminadosTipo("Residência") { it.eliminadosC }.toString(),
                eliminadosTipo("Residência") { it.eliminadosD1 }.toString(),
                eliminadosTipo("Residência") { it.eliminadosD2 }.toString(),
                eliminadosTipo("Residência") { it.eliminadosE }.toString(),
                eliminadosTipo("Residência") {
                    it.eliminadosA1 +
                            it.eliminadosA2 +
                            it.eliminadosB +
                            it.eliminadosC +
                            it.eliminadosD1 +
                            it.eliminadosD2 +
                            it.eliminadosE
                }.toString()
            ),
            listOf(
                "Comércio",
                eliminadosTipo("Comércio") { it.eliminadosA1 }.toString(),
                eliminadosTipo("Comércio") { it.eliminadosA2 }.toString(),
                eliminadosTipo("Comércio") { it.eliminadosB }.toString(),
                eliminadosTipo("Comércio") { it.eliminadosC }.toString(),
                eliminadosTipo("Comércio") { it.eliminadosD1 }.toString(),
                eliminadosTipo("Comércio") { it.eliminadosD2 }.toString(),
                eliminadosTipo("Comércio") { it.eliminadosE }.toString(),
                eliminadosTipo("Comércio") {
                    it.eliminadosA1 +
                            it.eliminadosA2 +
                            it.eliminadosB +
                            it.eliminadosC +
                            it.eliminadosD1 +
                            it.eliminadosD2 +
                            it.eliminadosE
                }.toString()
            ),
            listOf(
                "TB",
                eliminadosTipo("TB") { it.eliminadosA1 }.toString(),
                eliminadosTipo("TB") { it.eliminadosA2 }.toString(),
                eliminadosTipo("TB") { it.eliminadosB }.toString(),
                eliminadosTipo("TB") { it.eliminadosC }.toString(),
                eliminadosTipo("TB") { it.eliminadosD1 }.toString(),
                eliminadosTipo("TB") { it.eliminadosD2 }.toString(),
                eliminadosTipo("TB") { it.eliminadosE }.toString(),
                eliminadosTipo("TB") {
                    it.eliminadosA1 +
                            it.eliminadosA2 +
                            it.eliminadosB +
                            it.eliminadosC +
                            it.eliminadosD1 +
                            it.eliminadosD2 +
                            it.eliminadosE
                }.toString()
            ),
            listOf(
                "Outros",
                eliminadosTipo("Outros") { it.eliminadosA1 }.toString(),
                eliminadosTipo("Outros") { it.eliminadosA2 }.toString(),
                eliminadosTipo("Outros") { it.eliminadosB }.toString(),
                eliminadosTipo("Outros") { it.eliminadosC }.toString(),
                eliminadosTipo("Outros") { it.eliminadosD1 }.toString(),
                eliminadosTipo("Outros") { it.eliminadosD2 }.toString(),
                eliminadosTipo("Outros") { it.eliminadosE }.toString(),
                eliminadosTipo("Outros") {
                    it.eliminadosA1 +
                            it.eliminadosA2 +
                            it.eliminadosB +
                            it.eliminadosC +
                            it.eliminadosD1 +
                            it.eliminadosD2 +
                            it.eliminadosE
                }.toString()
            ),
            listOf(
                "Geral",
                elimA1.toString(),
                elimA2.toString(),
                elimB.toString(),
                elimC.toString(),
                elimD1.toString(),
                elimD2.toString(),
                elimE.toString(),
                totalEliminadosGeral.toString()
            )
        ),
        larguras = listOf(
            180f,
            100f,
            100f,
            100f,
            100f,
            100f,
            100f,
            100f,
            120f
        ),
        alturaLinha = 25f,
        tamanhoTexto = 13f
    )

// ============================================================
// LARVICIDA UTILIZADO
// ============================================================

    val totalGramasBoletim = visitas
        .filter {
            !ehPE(it.tipoImovel)
        }
        .sumOf {
            it.tratamento?.gramas ?: 0.0
        }

    desenharTituloResumo(
        "LARVICIDA UTILIZADO",
        40f,
        850f
    )

    desenharTabelaResumo(
        x = 40f,
        y = 860f,
        cabecalhos = listOf(
            "Informação",
            "Quantidade"
        ),
        linhas = listOf(
            listOf(
                "Larvicida utilizado",
                String.format(
                    java.util.Locale.US,
                    "%.1f g",
                    totalGramasBoletim
                )
            )
        ),
        larguras = listOf(
            220f,
            120f
        )
    )

// ============================================================
// FINALIZA A ÚLTIMA PÁGINA
// ============================================================

    pdf.finishPage(paginaAtual)

    val pasta = android.os.Environment.getExternalStoragePublicDirectory(
        android.os.Environment.DIRECTORY_DOWNLOADS
    )

    if (!pasta.exists()) {
        pasta.mkdirs()
    }

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

fun gerarRgPdf(
    context: android.content.Context,
    cabecalho: CabecalhoBoletim,
    quarteirao: String,
    visitas: List<Visita>
) {

    val pdf = PdfDocument()
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // ============================================================
    // TAMANHO DA PÁGINA - RETRATO
    // ============================================================

    val larguraPagina = 1240
    val alturaPagina = 1754

    val pageInfo = PdfDocument.PageInfo.Builder(
        larguraPagina,
        alturaPagina,
        1
    ).create()

    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas

    // ============================================================
    // CONFIGURAÇÕES
    // ============================================================

    val margem = 35f

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1.5f

    // ============================================================
    // TÍTULO
    // ============================================================

    paint.style = Paint.Style.FILL
    paint.textSize = 30f
    paint.isFakeBoldText = true

    canvas.drawText(
        "REGISTRO GERAL - QUARTEIRÃO",
        360f,
        45f,
        paint
    )

    paint.isFakeBoldText = false

    // ============================================================
    // CABEÇALHO
    // ============================================================

    val inicioCabecalho = 70f
    val alturaLinha = 38f

    val metade = (larguraPagina - (margem * 2)) / 2f

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1.5f

    // Caixa do cabeçalho
    canvas.drawRect(
        margem,
        inicioCabecalho,
        larguraPagina - margem,
        inicioCabecalho + alturaLinha * 3,
        paint
    )

    // Divisão vertical
    canvas.drawLine(
        larguraPagina / 2f,
        inicioCabecalho,
        larguraPagina / 2f,
        inicioCabecalho + alturaLinha * 3,
        paint
    )

    // Divisões horizontais
    canvas.drawLine(
        margem,
        inicioCabecalho + alturaLinha,
        larguraPagina - margem,
        inicioCabecalho + alturaLinha,
        paint
    )

    canvas.drawLine(
        margem,
        inicioCabecalho + alturaLinha * 2,
        larguraPagina - margem,
        inicioCabecalho + alturaLinha * 2,
        paint
    )

    paint.style = Paint.Style.FILL
    paint.textSize = 18f

    // Linha 1
    canvas.drawText(
        "Agente: ${cabecalho.nome}",
        margem + 10f,
        inicioCabecalho + 25f,
        paint
    )

    canvas.drawText(
        "Categoria: ${cabecalho.categoria}",
        larguraPagina / 2f + 10f,
        inicioCabecalho + 25f,
        paint
    )

    // Linha 2
    canvas.drawText(
        "Supervisor: ${cabecalho.supervisor}",
        margem + 10f,
        inicioCabecalho + alturaLinha + 25f,
        paint
    )

    canvas.drawText(
        "Bairro: ${cabecalho.localidade}",
        larguraPagina / 2f + 10f,
        inicioCabecalho + alturaLinha + 25f,
        paint
    )

    // Linha 3
    canvas.drawText(
        "Data: ${cabecalho.data}",
        margem + 10f,
        inicioCabecalho + alturaLinha * 2 + 25f,
        paint
    )

    canvas.drawText(
        "Quarteirão: $quarteirao",
        larguraPagina / 2f + 10f,
        inicioCabecalho + alturaLinha * 2 + 25f,
        paint
    )

    // ============================================================
    // TABELA DE IMÓVEIS
    // ============================================================

    val inicioTabela = inicioCabecalho + alturaLinha * 3 + 25f

    val larguraTabela = larguraPagina - margem * 2
    val larguraCadaTabela = larguraTabela / 2f

    val alturaCabecalhoTabela = 55f
    val alturaLinhaImovel = 30f

    // Cada metade da folha
    val esquerdaX = margem
    val direitaX = margem + larguraCadaTabela

    // Larguras das colunas
    val colRua = 350f
    val colNumero = 45f
    val colSeq = 45f
    val colComp = 50f
    val colTipo = 55f
    val colPend = larguraCadaTabela -
            colRua -
            colNumero -
            colSeq -
            colComp -
            colTipo

    // ============================================================
    // CABEÇALHO DA TABELA
    // ============================================================

    fun desenharCabecalhoTabela(x: Float) {

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f

        canvas.drawRect(
            x,
            inicioTabela,
            x + larguraCadaTabela,
            inicioTabela + alturaCabecalhoTabela,
            paint
        )

        var posX = x

        canvas.drawLine(
            posX + colRua,
            inicioTabela,
            posX + colRua,
            inicioTabela + alturaCabecalhoTabela,
            paint
        )

        posX += colRua

        canvas.drawLine(
            posX + colNumero,
            inicioTabela,
            posX + colNumero,
            inicioTabela + alturaCabecalhoTabela,
            paint
        )

        posX += colNumero

        canvas.drawLine(
            posX + colSeq,
            inicioTabela,
            posX + colSeq,
            inicioTabela + alturaCabecalhoTabela,
            paint
        )

        posX += colSeq

        canvas.drawLine(
            posX + colComp,
            inicioTabela,
            posX + colComp,
            inicioTabela + alturaCabecalhoTabela,
            paint
        )

        posX += colComp

        canvas.drawLine(
            posX + colTipo,
            inicioTabela,
            posX + colTipo,
            inicioTabela + alturaCabecalhoTabela,
            paint
        )

        paint.style = Paint.Style.FILL
        paint.textSize = 12f
        paint.isFakeBoldText = true

        canvas.drawText(
            "Logradouro",
            x + 150f,
            inicioTabela + 22f,
            paint
        )

        canvas.drawText(
            "Nº",
            x + colRua + 12f,
            inicioTabela + 22f,
            paint
        )

        canvas.drawText(
            "Seq",
            x + colRua + colNumero + 8f,
            inicioTabela + 22f,
            paint
        )

        canvas.drawText(
            "Comp.",
            x + colRua + colNumero + colSeq + 5f,
            inicioTabela + 22f,
            paint
        )

        canvas.drawText(
            "Tipo",
            x + colRua + colNumero + colSeq + colComp + 8f,
            inicioTabela + 22f,
            paint
        )

        canvas.drawText(
            "Pend.",
            x + colRua + colNumero + colSeq +
                    colComp + colTipo + 5f,
            inicioTabela + 22f,
            paint
        )

        paint.isFakeBoldText = false
    }

    desenharCabecalhoTabela(esquerdaX)
    desenharCabecalhoTabela(direitaX)

    // ============================================================
    // IMÓVEIS
    // ============================================================

    val visitasDoQuarteirao = visitas.filter {
        it.quarteirao == quarteirao
    }

    // Quantidade máxima de linhas disponíveis
    val maxLinhas = 40

    val primeiraMetade = visitasDoQuarteirao.take(maxLinhas)
    val segundaMetade = visitasDoQuarteirao.drop(maxLinhas).take(maxLinhas)

    fun desenharImovel(
        visita: Visita,
        x: Float,
        linha: Int
    ) {

        val y = inicioTabela +
                alturaCabecalhoTabela +
                linha * alturaLinhaImovel

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f

        canvas.drawRect(
            x,
            y,
            x + larguraCadaTabela,
            y + alturaLinhaImovel,
            paint
        )

        var posX = x

        canvas.drawLine(
            posX + colRua,
            y,
            posX + colRua,
            y + alturaLinhaImovel,
            paint
        )

        posX += colRua

        canvas.drawLine(
            posX + colNumero,
            y,
            posX + colNumero,
            y + alturaLinhaImovel,
            paint
        )

        posX += colNumero

        canvas.drawLine(
            posX + colSeq,
            y,
            posX + colSeq,
            y + alturaLinhaImovel,
            paint
        )

        posX += colSeq

        canvas.drawLine(
            posX + colComp,
            y,
            posX + colComp,
            y + alturaLinhaImovel,
            paint
        )

        posX += colComp

        canvas.drawLine(
            posX + colTipo,
            y,
            posX + colTipo,
            y + alturaLinhaImovel,
            paint
        )

        // --------------------------------------------------------
        // Tipo
        // --------------------------------------------------------

        val tipo = when (visita.tipoImovel) {
            "Residência" -> "R"
            "Terreno" -> "TB"
            "Comércio" -> "C"
            "PE" -> "PE"
            else -> "O"
        }

        // --------------------------------------------------------
        // Pendência
        // --------------------------------------------------------

        val pendencia = when {
            visita.pendencia.contains(
                "Fechado",
                ignoreCase = true
            ) -> "F"

            visita.pendencia.contains(
                "Recusa",
                ignoreCase = true
            ) -> "R"

            visita.pendencia.isNotBlank() -> "PE"

            else -> "--"
        }

        paint.style = Paint.Style.FILL
        paint.textSize = 11f

        val rua = if (visita.rua.length > 17) {
            visita.rua.take(17) + "…"
        } else {
            visita.rua
        }

        val complemento = if (visita.complemento.length > 9) {
            visita.complemento.take(9) + "…"
        } else {
            visita.complemento
        }

        canvas.drawText(
            rua,
            x + 4f,
            y + 20f,
            paint
        )

        canvas.drawText(
            visita.numero,
            x + colRua + 5f,
            y + 20f,
            paint
        )

        canvas.drawText(
            visita.sequencia,
            x + colRua + colNumero + 5f,
            y + 20f,
            paint
        )

        canvas.drawText(
            complemento,
            x + colRua + colNumero + colSeq + 4f,
            y + 20f,
            paint
        )

        canvas.drawText(
            tipo,
            x + colRua + colNumero + colSeq +
                    colComp + 18f,
            y + 20f,
            paint
        )

        canvas.drawText(
            pendencia,
            x + colRua + colNumero + colSeq +
                    colComp + colTipo + 12f,
            y + 20f,
            paint
        )
    }

    primeiraMetade.forEachIndexed { index, visita ->
        desenharImovel(
            visita,
            esquerdaX,
            index
        )
    }

    segundaMetade.forEachIndexed { index, visita ->
        desenharImovel(
            visita,
            direitaX,
            index
        )
    }

// ============================================================
// FECHAMENTO
// ============================================================

    val ultimaLinha = inicioTabela +
            alturaCabecalhoTabela +
            maxLinhas * alturaLinhaImovel

    val fechamentoY = ultimaLinha + 25f

    val larguraFechamento = larguraPagina - margem
    val alturaTituloFechamento = 30f
    val alturaLinhaFechamento = 28f
    val alturaTabelaFechamento =
        alturaTituloFechamento + (alturaLinhaFechamento * 4)

    val meioFechamento = larguraPagina / 2f

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1.5f

// ============================================================
// TOTAIS
// ============================================================

    val residencias = visitasDoQuarteirao.count {
        it.tipoImovel == "Residência"
    }

    val comercios = visitasDoQuarteirao.count {
        it.tipoImovel == "Comércio"
    }

    val terrenos = visitasDoQuarteirao.count {
        it.tipoImovel == "Terreno"
    }

    val outros = visitasDoQuarteirao.count {
        it.tipoImovel == "Outros"
    }

    val pe = visitas.count {
        it.tipoImovel == ("PE")
    }

    val pendencias = visitasDoQuarteirao.count {
        it.pendencia.isNotBlank()
    }

    val totalGeral =
        residencias +
                comercios +
                terrenos +
                outros +
                pe

// ============================================================
// BORDA EXTERNA
// ============================================================

    canvas.drawRect(
        margem,
        fechamentoY,
        larguraFechamento,
        fechamentoY + alturaTabelaFechamento,
        paint
    )

// ============================================================
// LINHA ABAIXO DO TÍTULO
// ============================================================

    canvas.drawLine(
        margem,
        fechamentoY + alturaTituloFechamento,
        larguraFechamento,
        fechamentoY + alturaTituloFechamento,
        paint
    )

// ============================================================
// DIVISÃO VERTICAL
// ============================================================

    canvas.drawLine(
        meioFechamento,
        fechamentoY + alturaTituloFechamento,
        meioFechamento,
        fechamentoY + alturaTabelaFechamento,
        paint
    )

// ============================================================
// LINHAS HORIZONTAIS
// ============================================================

    for (i in 1..3) {
        val y = fechamentoY +
                alturaTituloFechamento +
                (alturaLinhaFechamento * i)

        canvas.drawLine(
            margem,
            y,
            meioFechamento,
            y,
            paint
        )
    }

// ============================================================
// TÍTULO
// ============================================================

    paint.style = Paint.Style.FILL
    paint.textSize = 18f
    paint.isFakeBoldText = true

    val titulo = "FECHAMENTO"

    canvas.drawText(
        titulo,
        larguraPagina / 2f - (paint.measureText(titulo) / 2f),
        fechamentoY + 21f,
        paint
    )

// ============================================================
// TEXTOS DO FECHAMENTO
// ============================================================

    paint.isFakeBoldText = false
    paint.textSize = 16f

    val margemTexto = 15f

// Linha 1 - Residencial
    canvas.drawText(
        "Residencial (R): $residencias",
        margem + margemTexto,
        fechamentoY + alturaTituloFechamento + 20f,
        paint
    )

// Linha 2 - Comercial
    canvas.drawText(
        "Comercial (C): $comercios",
        margem + margemTexto,
        fechamentoY + alturaTituloFechamento + 48f,
        paint
    )

// Linha 3 - Terreno Baldio
    canvas.drawText(
        "Terreno Baldio (TB): $terrenos",
        margem + margemTexto,
        fechamentoY + alturaTituloFechamento + 76f,
        paint
    )

// Linha 4 - Pendências
    canvas.drawText(
        "Pendências (P): $pendencias",
        margem + margemTexto,
        fechamentoY + alturaTituloFechamento + 104f,
        paint
    )

// ============================================================
// LADO DIREITO
// ============================================================

// PE
    canvas.drawText(
        "PE: $pe",
        meioFechamento + margemTexto,
        fechamentoY + alturaTituloFechamento + 20f,
        paint
    )

// Outros
    canvas.drawText(
        "Outros (O): $outros",
        meioFechamento + margemTexto,
        fechamentoY + alturaTituloFechamento + 48f,
        paint
    )

// Total Geral
    paint.isFakeBoldText = true

    canvas.drawText(
        "TOTAL GERAL: $totalGeral",
        meioFechamento + margemTexto,
        fechamentoY + alturaTituloFechamento + 76f,
        paint
    )

    paint.isFakeBoldText = false

    // ============================================================
    // IDENTIFICAÇÃO / ASSINATURA
    // ============================================================

    val assinaturaY = fechamentoY + 165f

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1.2f

    canvas.drawRect(
        margem,
        assinaturaY,
        larguraPagina - margem,
        assinaturaY + 90f,
        paint
    )

    canvas.drawLine(
        margem,
        assinaturaY + 45f,
        larguraPagina - margem,
        assinaturaY + 45f,
        paint
    )

    paint.style = Paint.Style.FILL
    paint.textSize = 16f

    canvas.drawText(
        "Nome: ${cabecalho.nome}",
        margem + 10f,
        assinaturaY + 28f,
        paint
    )

    canvas.drawText(
        "Assinatura:",
        margem + 10f,
        assinaturaY + 73f,
        paint
    )

    canvas.drawText(
        "Data: ${cabecalho.data}",
        larguraPagina - 200f,
        assinaturaY + 73f,
        paint
    )

    // ============================================================
    // FINALIZAÇÃO
    // ============================================================

    pdf.finishPage(page)

    val pasta = android.os.Environment.getExternalStoragePublicDirectory(
        android.os.Environment.DIRECTORY_DOWNLOADS
    )

    if (!pasta.exists()) {
        pasta.mkdirs()
    }

    // IMPORTANTE:
    // O nome NÃO possui ciclo nem ano.
    // O arquivo representa o estado atual do quarteirão.

    val nomeArquivo = "RG_Q${quarteirao}.pdf"

    val arquivo = File(
        pasta,
        nomeArquivo
    )

    // FileOutputStream sobrescreve o arquivo existente.
    pdf.writeTo(
        FileOutputStream(arquivo)
    )

    pdf.close()

    Toast.makeText(
        context,
        "RG do quarteirão $quarteirao atualizado em Downloads",
        Toast.LENGTH_LONG
    ).show()
}

fun exportarBoletimXlsx(
    context: android.content.Context,
    visitas: List<Visita>,
    cabecalho: CabecalhoBoletim
) {

    val pasta = android.os.Environment.getExternalStoragePublicDirectory(
        android.os.Environment.DIRECTORY_DOWNLOADS
    )

    if (!pasta.exists()) {
        pasta.mkdirs()
    }

    val nomeArquivo =
        "Boletim_${cabecalho.localidade}_Q${cabecalho.quarteirao}_${cabecalho.data.replace("/", "-")}.xlsx"

    val arquivo = java.io.File(
        pasta,
        nomeArquivo
    )

    val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()

    try {

        // =====================================================
        // ESTILOS
        // =====================================================

        // -----------------------------------------------------
        // Estilo das células normais
        // -----------------------------------------------------

        val estiloCelula = workbook.createCellStyle()

        val fonteCelula = workbook.createFont().apply {
            color = org.apache.poi.ss.usermodel.IndexedColors.BLACK.index
        }

        estiloCelula.setFont(fonteCelula)

        estiloCelula.fillForegroundColor =
            org.apache.poi.ss.usermodel.IndexedColors.WHITE.index

        estiloCelula.fillPattern =
            org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND

        estiloCelula.borderTop =
            org.apache.poi.ss.usermodel.BorderStyle.THIN

        estiloCelula.borderBottom =
            org.apache.poi.ss.usermodel.BorderStyle.THIN

        estiloCelula.borderLeft =
            org.apache.poi.ss.usermodel.BorderStyle.THIN

        estiloCelula.borderRight =
            org.apache.poi.ss.usermodel.BorderStyle.THIN

        estiloCelula.topBorderColor =
            org.apache.poi.ss.usermodel.IndexedColors.GREEN.index

        estiloCelula.bottomBorderColor =
            org.apache.poi.ss.usermodel.IndexedColors.GREEN.index

        estiloCelula.leftBorderColor =
            org.apache.poi.ss.usermodel.IndexedColors.GREEN.index

        estiloCelula.rightBorderColor =
            org.apache.poi.ss.usermodel.IndexedColors.GREEN.index


        // -----------------------------------------------------
        // Estilo do cabeçalho
        // -----------------------------------------------------

        val estiloCabecalho = workbook.createCellStyle()

        val fonteCabecalho = workbook.createFont().apply {
            bold = true
            color = org.apache.poi.ss.usermodel.IndexedColors.BLACK.index
        }

        estiloCabecalho.setFont(fonteCabecalho)

        estiloCabecalho.fillForegroundColor =
            org.apache.poi.ss.usermodel.IndexedColors.WHITE.index

        estiloCabecalho.fillPattern =
            org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND

        estiloCabecalho.borderTop =
            org.apache.poi.ss.usermodel.BorderStyle.THIN

        estiloCabecalho.borderBottom =
            org.apache.poi.ss.usermodel.BorderStyle.THIN

        estiloCabecalho.borderLeft =
            org.apache.poi.ss.usermodel.BorderStyle.THIN

        estiloCabecalho.borderRight =
            org.apache.poi.ss.usermodel.BorderStyle.THIN

        estiloCabecalho.topBorderColor =
            org.apache.poi.ss.usermodel.IndexedColors.GREEN.index

        estiloCabecalho.bottomBorderColor =
            org.apache.poi.ss.usermodel.IndexedColors.GREEN.index

        estiloCabecalho.leftBorderColor =
            org.apache.poi.ss.usermodel.IndexedColors.GREEN.index

        estiloCabecalho.rightBorderColor =
            org.apache.poi.ss.usermodel.IndexedColors.GREEN.index


        // =====================================================
        // ABA VISITAS
        // =====================================================

        val sheet = workbook.createSheet("VISITAS")

        val cabecalhos = listOf(
            "Ciclo",
            "Quarteirão",
            "Rua",
            "Nº",
            "Sequência",
            "Complemento",
            "Tipo de Imóvel",
            "Inspecionado",
            "Pendência",
            "Foco",
            "A1 Eliminado",
            "A2 Eliminado",
            "B Eliminado",
            "C Eliminado",
            "D1 Eliminado",
            "D2 Eliminado",
            "E Eliminado",
            "A1 Tratado",
            "A2 Tratado",
            "B Tratado",
            "C Tratado",
            "D1 Tratado",
            "D2 Tratado",
            "E Tratado",
            "Larv.(g)",
            "Observação"
        )

        // =====================================================
        // CABEÇALHO
        // =====================================================

        val linhaCabecalho = sheet.createRow(0)

        cabecalhos.forEachIndexed { indice, titulo ->

            val celula = linhaCabecalho.createCell(indice)

            celula.setCellValue(titulo)

            celula.cellStyle = estiloCabecalho
        }


        // =====================================================
        // DADOS DAS VISITAS
        // =====================================================

        visitas.forEachIndexed { indice, visita ->

            val linha = sheet.createRow(indice + 1)

            linha.createCell(0).apply {
                setCellValue(visita.cicloAno)
                cellStyle = estiloCelula
            }

            linha.createCell(1).apply {
                setCellValue(visita.quarteirao)
                cellStyle = estiloCelula
            }

            linha.createCell(2).apply {
                setCellValue(visita.rua)
                cellStyle = estiloCelula
            }

            linha.createCell(3).apply {
                setCellValue(visita.numero)
                cellStyle = estiloCelula
            }

            linha.createCell(4).apply {
                setCellValue(visita.sequencia)
                cellStyle = estiloCelula
            }

            linha.createCell(5).apply {
                setCellValue(visita.complemento)
                cellStyle = estiloCelula
            }

            linha.createCell(6).apply {
                setCellValue(visita.tipoImovel)
                cellStyle = estiloCelula
            }

            linha.createCell(7).apply {
                setCellValue(
                    if (visita.inspecionado) "Sim" else "Não"
                )
                cellStyle = estiloCelula
            }

            linha.createCell(8).apply {
                setCellValue(visita.pendencia)
                cellStyle = estiloCelula
            }

            linha.createCell(9).apply {
                setCellValue(
                    if (visita.foco) "Sim" else "Não"
                )
                cellStyle = estiloCelula
            }

            linha.createCell(10).apply {
                setCellValue(visita.eliminadosA1.toDouble())
                cellStyle = estiloCelula
            }

            linha.createCell(11).apply {
                setCellValue(visita.eliminadosA2.toDouble())
                cellStyle = estiloCelula
            }

            linha.createCell(12).apply {
                setCellValue(visita.eliminadosB.toDouble())
                cellStyle = estiloCelula
            }

            linha.createCell(13).apply {
                setCellValue(visita.eliminadosC.toDouble())
                cellStyle = estiloCelula
            }

            linha.createCell(14).apply {
                setCellValue(visita.eliminadosD1.toDouble())
                cellStyle = estiloCelula
            }

            linha.createCell(15).apply {
                setCellValue(visita.eliminadosD2.toDouble())
                cellStyle = estiloCelula
            }

            linha.createCell(16).apply {
                setCellValue(visita.eliminadosE.toDouble())
                cellStyle = estiloCelula
            }

            linha.createCell(17).apply {
                setCellValue(
                    visita.tratamento?.a1?.toDouble() ?: 0.0
                )
                cellStyle = estiloCelula
            }

            linha.createCell(18).apply {
                setCellValue(
                    visita.tratamento?.a2?.toDouble() ?: 0.0
                )
                cellStyle = estiloCelula
            }

            linha.createCell(19).apply {
                setCellValue(
                    visita.tratamento?.b?.toDouble() ?: 0.0
                )
                cellStyle = estiloCelula
            }

            linha.createCell(20).apply {
                setCellValue(
                    visita.tratamento?.c?.toDouble() ?: 0.0
                )
                cellStyle = estiloCelula
            }

            linha.createCell(21).apply {
                setCellValue(
                    visita.tratamento?.d1?.toDouble() ?: 0.0
                )
                cellStyle = estiloCelula
            }

            linha.createCell(22).apply {
                setCellValue(
                    visita.tratamento?.d2?.toDouble() ?: 0.0
                )
                cellStyle = estiloCelula
            }

            linha.createCell(23).apply {
                setCellValue(
                    visita.tratamento?.e?.toDouble() ?: 0.0
                )
                cellStyle = estiloCelula
            }

            linha.createCell(24).apply {
                setCellValue(
                    visita.tratamento?.gramas ?: 0.0
                )
                cellStyle = estiloCelula
            }

            linha.createCell(25).apply {
                setCellValue(visita.observacao)
                cellStyle = estiloCelula
            }
        }




        // =====================================================
        // LARGURA DAS COLUNAS
        // =====================================================

        val larguras = listOf(
            10, // Ciclo/Ano
            15, // Quarteirão
            35, // Rua
            10, // Nº
            12, // Sequência
            20, // Complemento
            20, // Tipo do Imóvel
            15, // Inspecionado
            20, // Pendência
            10, // Foco
            8,  // A1
            8,  // A2
            8,  // B
            8,  // C
            8,  // D1
            8,  // D2
            8,  // E
            12, // A1 Tratado
            12, // A2 Tratado
            12, // B Tratado
            12, // C Tratado
            12, // D1 Tratado
            12, // D2 Tratado
            12, // E Tratado
            15, // Larvicida
            30  // Observação
        )

        larguras.forEachIndexed { indice, largura ->

            sheet.setColumnWidth(
                indice,
                largura * 256
            )
        }

                // =====================================================
                // ABA FECHAMENTO
                // =====================================================

        val fechamento = workbook.createSheet("FECHAMENTO")

                // =====================================================
                // INFORMADOS
                // =====================================================

        val tituloInformados = fechamento.createRow(0)

        tituloInformados.createCell(0).setCellValue("INFORMADOS")
        tituloInformados.createCell(1).setCellValue("Quantidade")

                // -----------------------------------------------------
                // Funções auxiliares para classificação
                // -----------------------------------------------------

        fun ehPEXlsx(tipo: String): Boolean {
            return tipo.equals("PE", ignoreCase = true)
        }

        fun ehTerrenoXlsx(tipo: String): Boolean {
            return tipo.equals("Terreno", ignoreCase = true) ||
                    tipo.equals("TB", ignoreCase = true)
        }

        fun ehResidenciaXlsx(tipo: String): Boolean {
            return tipo.equals("Residência", ignoreCase = true)
        }

        fun ehComercioXlsx(tipo: String): Boolean {
            return tipo.equals("Comércio", ignoreCase = true)
        }

        fun ehOutroXlsx(tipo: String): Boolean {
            return !ehPEXlsx(tipo) &&
                    !ehResidenciaXlsx(tipo) &&
                    !ehComercioXlsx(tipo) &&
                    !ehTerrenoXlsx(tipo)
        }

                // -----------------------------------------------------
                // Quantidade por tipo
                // -----------------------------------------------------

        fun quantidadeTipoXlsx(tipo: String): Int {

            return visitas.count { visita ->

                !ehPEXlsx(visita.tipoImovel) &&

                        when (tipo) {
                            "Residência" ->
                                ehResidenciaXlsx(visita.tipoImovel)

                            "Comércio" ->
                                ehComercioXlsx(visita.tipoImovel)

                            "TB" ->
                                ehTerrenoXlsx(visita.tipoImovel)

                            "Outros" ->
                                ehOutroXlsx(visita.tipoImovel)

                            else -> false
                        }
            }
        }

                // -----------------------------------------------------
                // Dados
                // -----------------------------------------------------

        val informados = listOf(
            "Residência" to quantidadeTipoXlsx("Residência"),
            "Comércio" to quantidadeTipoXlsx("Comércio"),
            "Terrenos (TB)" to quantidadeTipoXlsx("TB"),
            "Outros" to quantidadeTipoXlsx("Outros")
        )

                // -----------------------------------------------------
                // Preenche a tabela
                // -----------------------------------------------------

        informados.forEachIndexed { indice, (tipo, quantidade) ->

            val linha = fechamento.createRow(indice + 1)

            linha.createCell(0).setCellValue(tipo)
            linha.createCell(1).setCellValue(quantidade.toDouble())
        }

                // -----------------------------------------------------
                // TOTAL
                // -----------------------------------------------------

        val linhaTotalInformados = fechamento.createRow(5)

        linhaTotalInformados.createCell(0).setCellValue("TOTAL")

        linhaTotalInformados.createCell(1).setCellValue(
            informados.sumOf { it.second }.toDouble()
        )

                // -----------------------------------------------------
                // Largura das colunas
                // -----------------------------------------------------

        fechamento.setColumnWidth(0, 18 * 256)
        fechamento.setColumnWidth(1, 12 * 256)

        fechamento.setColumnWidth(2, 1 * 256) // espaço

            // =====================================================
            // INSPECIONADOS
            // =====================================================

        val tituloInspecionados = fechamento.getRow(0)

        tituloInspecionados.createCell(3).setCellValue("TRABALHADOS")
        tituloInspecionados.createCell(4).setCellValue("Quantidade")

                // -----------------------------------------------------
                // Quantidade de inspecionados por tipo
                // -----------------------------------------------------

        fun inspecionadosTipoXlsx(tipo: String): Int {

            return visitas.count { visita ->

                !ehPEXlsx(visita.tipoImovel) &&
                        visita.inspecionado &&

                        when (tipo) {
                            "Residência" ->
                                ehResidenciaXlsx(visita.tipoImovel)

                            "Comércio" ->
                                ehComercioXlsx(visita.tipoImovel)

                            "TB" ->
                                ehTerrenoXlsx(visita.tipoImovel)

                            "Outros" ->
                                ehOutroXlsx(visita.tipoImovel)

                            else -> false
                        }
            }
        }

                // -----------------------------------------------------
                // Dados
                // -----------------------------------------------------

        val inspecionados = listOf(
            "Residência" to inspecionadosTipoXlsx("Residência"),
            "Comércio" to inspecionadosTipoXlsx("Comércio"),
            "Terrenos (TB)" to inspecionadosTipoXlsx("TB"),
            "Outros" to inspecionadosTipoXlsx("Outros")
        )

            // -----------------------------------------------------
            // Preenche a tabela
            // -----------------------------------------------------

        inspecionados.forEachIndexed { indice, (tipo, quantidade) ->

            val linha = fechamento.getRow(indice + 1)
                ?: fechamento.createRow(indice + 1)

            linha.createCell(3).setCellValue(tipo)
            linha.createCell(4).setCellValue(quantidade.toDouble())
        }

                // -----------------------------------------------------
                // TOTAL
                // -----------------------------------------------------

        val linhaTotalInspecionados =
            fechamento.getRow(5) ?: fechamento.createRow(5)

        linhaTotalInspecionados.createCell(3).setCellValue("TOTAL")

        linhaTotalInspecionados.createCell(4).setCellValue(
            inspecionados.sumOf { it.second }.toDouble()
        )

        // =====================================================
// PENDÊNCIAS
// =====================================================

// -----------------------------------------------------
// Função auxiliar para contar pendências por tipo
// -----------------------------------------------------

        fun pendenciaTipoXlsx(
            tipo: String,
            pendencia: String
        ): Int {

            return visitas.count { visita ->

                !ehPEXlsx(visita.tipoImovel) &&

                        when (tipo) {

                            "Residência" ->
                                ehResidenciaXlsx(visita.tipoImovel)

                            "Comércio" ->
                                ehComercioXlsx(visita.tipoImovel)

                            "TB" ->
                                ehTerrenoXlsx(visita.tipoImovel)

                            "Outros" ->
                                ehOutroXlsx(visita.tipoImovel)

                            else -> false
                        } &&

                        visita.pendencia.equals(
                            pendencia,
                            ignoreCase = true
                        )
            }
        }


// -----------------------------------------------------
// Cabeçalho
// -----------------------------------------------------

        val tituloPendencias = fechamento.getRow(0)
            ?: fechamento.createRow(0)

        tituloPendencias.createCell(6)
            .setCellValue("PENDÊNCIAS")

        tituloPendencias.createCell(7)
            .setCellValue("Fechados")

        tituloPendencias.createCell(8)
            .setCellValue("Recusados")

        tituloPendencias.createCell(9)
            .setCellValue("Pendências")


// -----------------------------------------------------
// Tipos
// -----------------------------------------------------

        val tiposPendencias = listOf(
            "Residência",
            "Comércio",
            "TB",
            "Outros"
        )


// -----------------------------------------------------
// Preenche a tabela
// -----------------------------------------------------

        tiposPendencias.forEachIndexed { indice, tipo ->

            val linha = fechamento.getRow(indice + 1)
                ?: fechamento.createRow(indice + 1)

            val fechados = pendenciaTipoXlsx(
                tipo = tipo,
                pendencia = "Fechado"
            )

            val recusados = pendenciaTipoXlsx(
                tipo = tipo,
                pendencia = "Recusado"
            )

            val totalPendencias = fechados + recusados

            linha.createCell(6)
                .setCellValue(
                    when (tipo) {
                        "TB" -> "Terrenos (TB)"
                        else -> tipo
                    }
                )

            linha.createCell(7)
                .setCellValue(
                    fechados.toDouble()
                )

            linha.createCell(8)
                .setCellValue(
                    recusados.toDouble()
                )

            linha.createCell(9)
                .setCellValue(
                    totalPendencias.toDouble()
                )
        }


// -----------------------------------------------------
// TOTAL
// -----------------------------------------------------

        val linhaTotalPendencias = fechamento.getRow(5)
            ?: fechamento.createRow(5)

        linhaTotalPendencias.createCell(6)
            .setCellValue("TOTAL")


        val totalFechados = tiposPendencias.sumOf { tipo ->

            pendenciaTipoXlsx(
                tipo = tipo,
                pendencia = "Fechado"
            )
        }


        val totalRecusados = tiposPendencias.sumOf { tipo ->

            pendenciaTipoXlsx(
                tipo = tipo,
                pendencia = "Recusado"
            )
        }


        val totalPendencias = totalFechados + totalRecusados


        linhaTotalPendencias.createCell(7)
            .setCellValue(
                totalFechados.toDouble()
            )


        linhaTotalPendencias.createCell(8)
            .setCellValue(
                totalRecusados.toDouble()
            )


        linhaTotalPendencias.createCell(9)
            .setCellValue(
                totalPendencias.toDouble()
            )


// -----------------------------------------------------
// Largura das colunas
// -----------------------------------------------------

        fechamento.setColumnWidth(6, 18 * 256)
        fechamento.setColumnWidth(7, 12 * 256)
        fechamento.setColumnWidth(8, 12 * 256)
        fechamento.setColumnWidth(9, 12 * 256)

        // =====================================================
// IMÓVEIS TRATADOS
// =====================================================

// -----------------------------------------------------
// Quantidade de imóveis tratados por tipo
// -----------------------------------------------------

        fun tratadosTipoXlsx(tipo: String): Int {

            return visitas.count { visita ->

                !ehPEXlsx(visita.tipoImovel) &&

                        when (tipo) {
                            "Residência" ->
                                ehResidenciaXlsx(visita.tipoImovel)

                            "Comércio" ->
                                ehComercioXlsx(visita.tipoImovel)

                            "TB" ->
                                ehTerrenoXlsx(visita.tipoImovel)

                            "Outros" ->
                                ehOutroXlsx(visita.tipoImovel)

                            else -> false
                        } &&

                        (visita.tratamento?.total ?: 0) > 0
            }
        }

// -----------------------------------------------------
// Dados
// -----------------------------------------------------

        val tratados = listOf(
            "Residências" to tratadosTipoXlsx("Residência"),
            "Comércios" to tratadosTipoXlsx("Comércio"),
            "Terrenos (TB)" to tratadosTipoXlsx("TB"),
            "Outros" to tratadosTipoXlsx("Outros")
        )

// -----------------------------------------------------
// Título
// -----------------------------------------------------

        val tituloTratados = fechamento.createRow(7)

        tituloTratados.createCell(0).setCellValue("IMÓVEIS TRATADOS")
        tituloTratados.createCell(1).setCellValue("Tratados")

// -----------------------------------------------------
// Preenche a tabela
// -----------------------------------------------------

        tratados.forEachIndexed { indice, (tipo, quantidade) ->

            val linha = fechamento.createRow(indice + 8)

            linha.createCell(0).setCellValue(tipo)
            linha.createCell(1).setCellValue(quantidade.toDouble())
        }

// -----------------------------------------------------
// TOTAL
// -----------------------------------------------------

        val linhaTotalTratados = fechamento.createRow(12)

        linhaTotalTratados.createCell(0).setCellValue("TOTAL")

        linhaTotalTratados.createCell(1).setCellValue(
            tratados.sumOf { it.second }.toDouble()
        )

// -----------------------------------------------------
// Largura das colunas
// -----------------------------------------------------

        fechamento.setColumnWidth(0, 18 * 256)
        fechamento.setColumnWidth(1, 12 * 256)



// =====================================================
// FOCOS
// =====================================================

        fun focosTipoXlsx(tipo: String): Int {

            return visitas.count { visita ->

                !ehPEXlsx(visita.tipoImovel) &&
                        visita.foco &&
                        when (tipo) {

                            "Residência" ->
                                ehResidenciaXlsx(visita.tipoImovel)

                            "Comércio" ->
                                ehComercioXlsx(visita.tipoImovel)

                            "TB" ->
                                ehTerrenoXlsx(visita.tipoImovel)

                            "Outros" ->
                                ehOutroXlsx(visita.tipoImovel)

                            else -> false
                        }
            }
        }

// -----------------------------------------------------
// Dados
// -----------------------------------------------------

        val focos = listOf(
            "Residências" to focosTipoXlsx("Residência"),
            "Comércios" to focosTipoXlsx("Comércio"),
            "Terrenos (TB)" to focosTipoXlsx("TB"),
            "Outros" to focosTipoXlsx("Outros")
        )

// -----------------------------------------------------
// CABEÇALHO
// -----------------------------------------------------

        val tituloFocos = fechamento.getRow(7)
            ?: fechamento.createRow(7)

        tituloFocos.createCell(3).setCellValue("FOCOS")
        tituloFocos.createCell(4).setCellValue("Quantidade")

// -----------------------------------------------------
// PREENCHE A TABELA
// -----------------------------------------------------

        focos.forEachIndexed { indice, (tipo, quantidade) ->

            val linha = fechamento.getRow(indice + 8)
                ?: fechamento.createRow(indice + 8)

            linha.createCell(3).setCellValue(tipo)
            linha.createCell(4).setCellValue(quantidade.toDouble())
        }

// -----------------------------------------------------
// TOTAL
// -----------------------------------------------------

        val linhaTotalFocos = fechamento.getRow(12)
            ?: fechamento.createRow(12)

        linhaTotalFocos.createCell(3).setCellValue("TOTAL")

        linhaTotalFocos.createCell(4).setCellValue(
            focos.sumOf { it.second }.toDouble()
        )

// -----------------------------------------------------
// LARGURA DAS COLUNAS
// -----------------------------------------------------

        fechamento.setColumnWidth(3, 18 * 256)
        fechamento.setColumnWidth(4, 12 * 256)

        // =====================================================
// DEPÓSITOS TRATADOS
// =====================================================

        fun depositosTratadosTipoXlsx(
            tipo: String,
            deposito: (Tratamento) -> Int
        ): Int {

            return visitas.sumOf { visita ->

                val tratamento = visita.tratamento

                if (
                    tratamento == null ||
                    ehPEXlsx(visita.tipoImovel)
                ) {
                    0
                } else {

                    val pertenceAoTipo = when (tipo) {

                        "Residência" ->
                            ehResidenciaXlsx(visita.tipoImovel)

                        "Comércio" ->
                            ehComercioXlsx(visita.tipoImovel)

                        "TB" ->
                            ehTerrenoXlsx(visita.tipoImovel)

                        "Outros" ->
                            ehOutroXlsx(visita.tipoImovel)

                        else -> false
                    }

                    if (pertenceAoTipo) {
                        deposito(tratamento)
                    } else {
                        0
                    }
                }
            }
        }

// -----------------------------------------------------
// Dados
// -----------------------------------------------------

        val tiposDepositosTratados = listOf(
            "Residência",
            "Comércio",
            "Terrenos (TB)",
            "Outros"
        )

// -----------------------------------------------------
// Cabeçalho
// -----------------------------------------------------

        val tituloDepositosTratados = fechamento.getRow(14)
            ?: fechamento.createRow(14)

        tituloDepositosTratados.createCell(0)
            .setCellValue("DEPÓSITOS TRATADOS")

        val cabecalhoDepositosTratados = fechamento.getRow(15)
            ?: fechamento.createRow(15)

        val cabecalhosDepositos = listOf(
            "Tipo",
            "A1",
            "A2",
            "B",
            "C",
            "D1",
            "D2",
            "E",
            "TOTAL"
        )

        cabecalhosDepositos.forEachIndexed { indice, titulo ->
            cabecalhoDepositosTratados
                .createCell(indice)
                .setCellValue(titulo)
        }

// -----------------------------------------------------
// Dados por tipo
// -----------------------------------------------------

        tiposDepositosTratados.forEachIndexed { indice, tipo ->

            val linha = fechamento.getRow(indice + 16)
                ?: fechamento.createRow(indice + 16)

            val a1 = depositosTratadosTipoXlsx(tipo) { it.a1 }
            val a2 = depositosTratadosTipoXlsx(tipo) { it.a2 }
            val b = depositosTratadosTipoXlsx(tipo) { it.b }
            val c = depositosTratadosTipoXlsx(tipo) { it.c }
            val d1 = depositosTratadosTipoXlsx(tipo) { it.d1 }
            val d2 = depositosTratadosTipoXlsx(tipo) { it.d2 }
            val e = depositosTratadosTipoXlsx(tipo) { it.e }

            val total = a1 + a2 + b + c + d1 + d2 + e

            linha.createCell(0).setCellValue(
                if (tipo == "Terrenos (TB)") "TB" else tipo
            )

            linha.createCell(1).setCellValue(a1.toDouble())
            linha.createCell(2).setCellValue(a2.toDouble())
            linha.createCell(3).setCellValue(b.toDouble())
            linha.createCell(4).setCellValue(c.toDouble())
            linha.createCell(5).setCellValue(d1.toDouble())
            linha.createCell(6).setCellValue(d2.toDouble())
            linha.createCell(7).setCellValue(e.toDouble())
            linha.createCell(8).setCellValue(total.toDouble())
        }



// -----------------------------------------------------
// GERAL
// -----------------------------------------------------

        val linhaGeralDepositosTratados = fechamento.getRow(20)
            ?: fechamento.createRow(20)

        linhaGeralDepositosTratados.createCell(0)
            .setCellValue("Geral")

        for (coluna in 1..8) {

            val totalColuna = (16..19).sumOf { linha ->
                fechamento.getRow(linha)
                    ?.getCell(coluna)
                    ?.numericCellValue
                    ?.toInt() ?: 0
            }

            linhaGeralDepositosTratados
                .createCell(coluna)
                .setCellValue(totalColuna.toDouble())
        }

// -----------------------------------------------------
// Larguras
// -----------------------------------------------------

        fechamento.setColumnWidth(0, 18 * 256)
        fechamento.setColumnWidth(1, 8 * 256)
        fechamento.setColumnWidth(2, 8 * 256)
        fechamento.setColumnWidth(3, 8 * 256)
        fechamento.setColumnWidth(4, 8 * 256)
        fechamento.setColumnWidth(5, 8 * 256)
        fechamento.setColumnWidth(6, 8 * 256)
        fechamento.setColumnWidth(7, 8 * 256)
        fechamento.setColumnWidth(8, 10 * 256)

        // =====================================================
// DEPÓSITOS ELIMINADOS
// =====================================================

        fun eliminadosTipoXlsx(
            tipo: String,
            eliminado: (Visita) -> Int
        ): Int {

            return visitas.sumOf { visita ->

                if (ehPEXlsx(visita.tipoImovel)) {
                    0
                } else {

                    val pertenceAoTipo = when (tipo) {

                        "Residência" ->
                            ehResidenciaXlsx(visita.tipoImovel)

                        "Comércio" ->
                            ehComercioXlsx(visita.tipoImovel)

                        "TB" ->
                            ehTerrenoXlsx(visita.tipoImovel)

                        "Outros" ->
                            ehOutroXlsx(visita.tipoImovel)

                        else -> false
                    }

                    if (pertenceAoTipo) {
                        eliminado(visita)
                    } else {
                        0
                    }
                }
            }
        }

// -----------------------------------------------------
// Tipos
// -----------------------------------------------------

        val tiposDepositosEliminados = listOf(
            "Residência",
            "Comércio",
            "TB",
            "Outros"
        )

// -----------------------------------------------------
// Cabeçalho
// -----------------------------------------------------

        val tituloDepositosEliminados = fechamento.getRow(22)
            ?: fechamento.createRow(22)

        tituloDepositosEliminados
            .createCell(0)
            .setCellValue("DEPÓSITOS ELIMINADOS")

// -----------------------------------------------------
// Cabeçalhos das colunas
// -----------------------------------------------------

        val cabecalhoDepositosEliminados = fechamento.getRow(23)
            ?: fechamento.createRow(23)

        val cabecalhosEliminados = listOf(
            "Tipo",
            "A1",
            "A2",
            "B",
            "C",
            "D1",
            "D2",
            "E",
            "TOTAL"
        )

        cabecalhosEliminados.forEachIndexed { indice, titulo ->

            cabecalhoDepositosEliminados
                .createCell(indice)
                .setCellValue(titulo)
        }

// -----------------------------------------------------
// Dados
// -----------------------------------------------------

        tiposDepositosEliminados.forEachIndexed { indice, tipo ->

            val linha = fechamento.getRow(indice + 24)
                ?: fechamento.createRow(indice + 24)

            val a1 = eliminadosTipoXlsx(tipo) { it.eliminadosA1 }
            val a2 = eliminadosTipoXlsx(tipo) { it.eliminadosA2 }
            val b = eliminadosTipoXlsx(tipo) { it.eliminadosB }
            val c = eliminadosTipoXlsx(tipo) { it.eliminadosC }
            val d1 = eliminadosTipoXlsx(tipo) { it.eliminadosD1 }
            val d2 = eliminadosTipoXlsx(tipo) { it.eliminadosD2 }
            val e = eliminadosTipoXlsx(tipo) { it.eliminadosE }

            val total = a1 + a2 + b + c + d1 + d2 + e

            linha.createCell(0)
                .setCellValue(
                    if (tipo == "Residência") {
                        "Residência"
                    } else {
                        tipo
                    }
                )

            linha.createCell(1).setCellValue(a1.toDouble())
            linha.createCell(2).setCellValue(a2.toDouble())
            linha.createCell(3).setCellValue(b.toDouble())
            linha.createCell(4).setCellValue(c.toDouble())
            linha.createCell(5).setCellValue(d1.toDouble())
            linha.createCell(6).setCellValue(d2.toDouble())
            linha.createCell(7).setCellValue(e.toDouble())
            linha.createCell(8).setCellValue(total.toDouble())
        }

// -----------------------------------------------------
// GERAL
// -----------------------------------------------------

        val linhaGeralEliminados = fechamento.getRow(28)
            ?: fechamento.createRow(28)

        linhaGeralEliminados
            .createCell(0)
            .setCellValue("Geral")

        for (coluna in 1..8) {

            val totalColuna = (24..27).sumOf { numeroLinha ->

                fechamento
                    .getRow(numeroLinha)
                    ?.getCell(coluna)
                    ?.numericCellValue
                    ?.toInt()
                    ?: 0
            }

            linhaGeralEliminados
                .createCell(coluna)
                .setCellValue(totalColuna.toDouble())
        }

// -----------------------------------------------------
// Largura das colunas
// -----------------------------------------------------

        fechamento.setColumnWidth(0, 18 * 256)
        fechamento.setColumnWidth(1, 8 * 256)
        fechamento.setColumnWidth(2, 8 * 256)
        fechamento.setColumnWidth(3, 8 * 256)
        fechamento.setColumnWidth(4, 8 * 256)
        fechamento.setColumnWidth(5, 8 * 256)
        fechamento.setColumnWidth(6, 8 * 256)
        fechamento.setColumnWidth(7, 8 * 256)
        fechamento.setColumnWidth(8, 10 * 256)

        // =====================================================
// LARVICIDA UTILIZADO
// =====================================================

// Pega a quantidade total de larvicida utilizada
        val totalLarvicida = visitas
            .filter { !ehPEXlsx(it.tipoImovel) }
            .sumOf { it.tratamento?.gramas ?: 0.0 }

// -----------------------------------------------------
// Título
// -----------------------------------------------------

        val linhaTituloLarvicida = fechamento.createRow(
            fechamento.lastRowNum + 2
        )

        linhaTituloLarvicida.createCell(0)
            .setCellValue("LARVICIDA UTILIZADO")

// -----------------------------------------------------
// Cabeçalho
// -----------------------------------------------------

        val linhaCabecalhoLarvicida = fechamento.createRow(
            linhaTituloLarvicida.rowNum + 1
        )

        linhaCabecalhoLarvicida.createCell(0)
            .setCellValue("Informação")

        linhaCabecalhoLarvicida.createCell(1)
            .setCellValue("Quantidade")

// -----------------------------------------------------
// Dados
// -----------------------------------------------------

        val linhaLarvicida = fechamento.createRow(
            linhaCabecalhoLarvicida.rowNum + 1
        )

        linhaLarvicida.createCell(0)
            .setCellValue("Larvicida utilizado")

        linhaLarvicida.createCell(1)
            .setCellValue("${totalLarvicida} g")

// -----------------------------------------------------
// Largura das colunas
// -----------------------------------------------------

        fechamento.setColumnWidth(0, 22 * 256)
        fechamento.setColumnWidth(1, 15 * 256)



        // =====================================================
        // CONGELA O CABEÇALHO
        // =====================================================

        sheet.createFreezePane(0, 1)

        // =====================================================
// ESTILIZAÇÃO DO FECHAMENTO
// =====================================================

// -----------------------------------------------------
// CORES
// -----------------------------------------------------

        val corVerdeFechamento =
            org.apache.poi.ss.usermodel.IndexedColors.GREEN.index

        val corBrancoFechamento =
            org.apache.poi.ss.usermodel.IndexedColors.WHITE.index

        val corPretoFechamento =
            org.apache.poi.ss.usermodel.IndexedColors.BLACK.index


// =====================================================
// FONTES
// =====================================================

        val fonteNormalFechamento = workbook.createFont().apply {
            fontName = "Arial"
            fontHeightInPoints = 9
            color = corPretoFechamento
        }

        val fonteNegritoFechamento = workbook.createFont().apply {
            fontName = "Arial"
            fontHeightInPoints = 9
            bold = true
            color = corPretoFechamento
        }


// =====================================================
// ESTILO — CÉLULA NORMAL
// =====================================================

        val estiloCelulaFechamentoNovo = workbook.createCellStyle().apply {

            setFont(fonteNormalFechamento)

            fillForegroundColor = corBrancoFechamento
            fillPattern =
                org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND

            borderTop =
                org.apache.poi.ss.usermodel.BorderStyle.THIN

            borderBottom =
                org.apache.poi.ss.usermodel.BorderStyle.THIN

            borderLeft =
                org.apache.poi.ss.usermodel.BorderStyle.THIN

            borderRight =
                org.apache.poi.ss.usermodel.BorderStyle.THIN

            topBorderColor = corVerdeFechamento
            bottomBorderColor = corVerdeFechamento
            leftBorderColor = corVerdeFechamento
            rightBorderColor = corVerdeFechamento

            verticalAlignment =
                org.apache.poi.ss.usermodel.VerticalAlignment.CENTER
        }


// =====================================================
// ESTILO — TÍTULO
// =====================================================

        val estiloTituloFechamentoNovo = workbook.createCellStyle().apply {

            setFont(fonteNegritoFechamento)

            fillForegroundColor = corBrancoFechamento
            fillPattern =
                org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND

            borderTop =
                org.apache.poi.ss.usermodel.BorderStyle.THIN

            borderBottom =
                org.apache.poi.ss.usermodel.BorderStyle.THIN

            borderLeft =
                org.apache.poi.ss.usermodel.BorderStyle.THIN

            borderRight =
                org.apache.poi.ss.usermodel.BorderStyle.THIN

            topBorderColor = corVerdeFechamento
            bottomBorderColor = corVerdeFechamento
            leftBorderColor = corVerdeFechamento
            rightBorderColor = corVerdeFechamento

            alignment =
                org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER

            verticalAlignment =
                org.apache.poi.ss.usermodel.VerticalAlignment.CENTER
        }


// =====================================================
// ESTILO — TOTAL
// =====================================================

        val estiloTotalFechamentoNovo = workbook.createCellStyle().apply {

            setFont(fonteNegritoFechamento)

            fillForegroundColor = corBrancoFechamento
            fillPattern =
                org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND

            borderTop =
                org.apache.poi.ss.usermodel.BorderStyle.THIN

            borderBottom =
                org.apache.poi.ss.usermodel.BorderStyle.THIN

            borderLeft =
                org.apache.poi.ss.usermodel.BorderStyle.THIN

            borderRight =
                org.apache.poi.ss.usermodel.BorderStyle.THIN

            topBorderColor = corVerdeFechamento
            bottomBorderColor = corVerdeFechamento
            leftBorderColor = corVerdeFechamento
            rightBorderColor = corVerdeFechamento

            verticalAlignment =
                org.apache.poi.ss.usermodel.VerticalAlignment.CENTER
        }


// =====================================================
// INFORMADOS — A:B
// =====================================================

// Título
        for (coluna in 0..1) {
            fechamento.getRow(0)
                ?.getCell(coluna)
                ?.cellStyle = estiloTituloFechamentoNovo
        }

// Dados
        for (linha in 1..4) {
            for (coluna in 0..1) {
                fechamento.getRow(linha)
                    ?.getCell(coluna)
                    ?.cellStyle = estiloCelulaFechamentoNovo
            }
        }

// Total
        for (coluna in 0..1) {
            fechamento.getRow(5)
                ?.getCell(coluna)
                ?.cellStyle = estiloTotalFechamentoNovo
        }


// =====================================================
// INSPECIONADOS — D:E
// =====================================================

// Título
        for (coluna in 3..4) {
            fechamento.getRow(0)
                ?.getCell(coluna)
                ?.cellStyle = estiloTituloFechamentoNovo
        }

// Dados
        for (linha in 1..4) {
            for (coluna in 3..4) {
                fechamento.getRow(linha)
                    ?.getCell(coluna)
                    ?.cellStyle = estiloCelulaFechamentoNovo
            }
        }

// Total
        for (coluna in 3..4) {
            fechamento.getRow(5)
                ?.getCell(coluna)
                ?.cellStyle = estiloTotalFechamentoNovo
        }

        // =====================================================
// IMÓVEIS TRATADOS - A:B
// =====================================================

// -----------------------------------------------------
// Título
// -----------------------------------------------------

        for (coluna in 0..1) {
            fechamento.getRow(7)
                ?.getCell(coluna)
                ?.let { celula ->
                    celula.cellStyle = estiloTituloFechamentoNovo
                }
        }

// -----------------------------------------------------
// Dados
// -----------------------------------------------------

        for (linha in 8..11) {
            for (coluna in 0..1) {
                fechamento.getRow(linha)
                    ?.getCell(coluna)
                    ?.let { celula ->
                        celula.cellStyle = estiloCelulaFechamentoNovo
                    }
            }
        }

// -----------------------------------------------------
// Total
// -----------------------------------------------------

        for (coluna in 0..1) {
            fechamento.getRow(12)
                ?.getCell(coluna)
                ?.let { celula ->
                    celula.cellStyle = estiloTotalFechamentoNovo
                }
        }

        // =====================================================
// FOCOS - D:E
// =====================================================

// -----------------------------------------------------
// Título
// -----------------------------------------------------

        for (coluna in 3..4) {
            fechamento.getRow(7)
                ?.getCell(coluna)
                ?.let { celula ->
                    celula.cellStyle = estiloTituloFechamentoNovo
                }
        }

// -----------------------------------------------------
// Dados
// -----------------------------------------------------

        for (linha in 8..11) {
            for (coluna in 3..4) {
                fechamento.getRow(linha)
                    ?.getCell(coluna)
                    ?.let { celula ->
                        celula.cellStyle = estiloCelulaFechamentoNovo
                    }
            }
        }

// -----------------------------------------------------
// Total
// -----------------------------------------------------

        for (coluna in 3..4) {
            fechamento.getRow(12)
                ?.getCell(coluna)
                ?.let { celula ->
                    celula.cellStyle = estiloTotalFechamentoNovo
                }
        }


// =====================================================
// PENDÊNCIAS — G:J
// =====================================================

// Título
        for (coluna in 6..9) {
            fechamento.getRow(0)
                ?.getCell(coluna)
                ?.cellStyle = estiloTituloFechamentoNovo
        }

// Dados
        for (linha in 1..4) {
            for (coluna in 6..9) {
                fechamento.getRow(linha)
                    ?.getCell(coluna)
                    ?.cellStyle = estiloCelulaFechamentoNovo
            }
        }

// Total
        for (coluna in 6..9) {
            fechamento.getRow(5)
                ?.getCell(coluna)
                ?.cellStyle = estiloTotalFechamentoNovo
        }

        // ============================================================
// ESTILO — DEPÓSITOS TRATADOS
// ============================================================

// Título
        for (coluna in 0..8) {
            fechamento.getRow(14)
                ?.getCell(coluna)
                ?.let { celula ->
                    celula.cellStyle = estiloTituloFechamentoNovo
                }
        }

// Cabeçalho
        for (coluna in 0..8) {
            fechamento.getRow(15)
                ?.getCell(coluna)
                ?.let { celula ->
                    celula.cellStyle = estiloTituloFechamentoNovo
                }
        }

// Dados
        for (linha in 16..19) {
            for (coluna in 0..8) {
                fechamento.getRow(linha)
                    ?.getCell(coluna)
                    ?.let { celula ->
                        celula.cellStyle = estiloCelulaFechamentoNovo
                    }
            }
        }

// Total / Geral
        for (coluna in 0..8) {
            fechamento.getRow(20)
                ?.getCell(coluna)
                ?.let { celula ->
                    celula.cellStyle = estiloTotalFechamentoNovo
                }
        }

        // ============================================================
// ESTILO — DEPÓSITOS ELIMINADOS
// ============================================================

// Título
        for (coluna in 0..8) {
            fechamento.getRow(22)
                ?.getCell(coluna)
                ?.let { celula ->
                    celula.cellStyle = estiloTituloFechamentoNovo
                }
        }

// Cabeçalho
        for (coluna in 0..8) {
            fechamento.getRow(23)
                ?.getCell(coluna)
                ?.let { celula ->
                    celula.cellStyle = estiloTituloFechamentoNovo
                }
        }

// Dados
        for (linha in 24..27) {
            for (coluna in 0..8) {
                fechamento.getRow(linha)
                    ?.getCell(coluna)
                    ?.let { celula ->
                        celula.cellStyle = estiloCelulaFechamentoNovo
                    }
            }
        }

// Total / Geral
        for (coluna in 0..8) {
            fechamento.getRow(28)
                ?.getCell(coluna)
                ?.let { celula ->
                    celula.cellStyle = estiloTotalFechamentoNovo
                }
        }

        // ============================================================
// ESTILO — LARVICIDA UTILIZADO
// ============================================================

// Título
        for (coluna in 0..1) {
            fechamento.getRow(30)
                ?.getCell(coluna)
                ?.let { celula ->
                    celula.cellStyle = estiloTituloFechamentoNovo
                }
        }

// Cabeçalho
        for (coluna in 0..1) {
            fechamento.getRow(31)
                ?.getCell(coluna)
                ?.let { celula ->
                    celula.cellStyle = estiloTituloFechamentoNovo
                }
        }

// Dados
        for (coluna in 0..1) {
            fechamento.getRow(32)
                ?.getCell(coluna)
                ?.let { celula ->
                    celula.cellStyle = estiloCelulaFechamentoNovo
                }
        }


        // =====================================================
        // SALVA O ARQUIVO
        // =====================================================

        java.io.FileOutputStream(arquivo).use { outputStream ->

            workbook.write(outputStream)

            outputStream.flush()
        }

    } finally {

        workbook.close()
    }


    // =====================================================
    // AVISO
    // =====================================================

    android.widget.Toast.makeText(
        context,
        "Boletim XLSX criado em Downloads",
        android.widget.Toast.LENGTH_LONG
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
    var cicloAno = (((LocalDate.now().monthValue - 1) / 2) + 1).toString()
    var quarteirao by remember { mutableStateOf("") }
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

            quarteirao = quarteirao,
            onQuarteiraoChange = {
                quarteirao = it
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
            agente = nomeAgente,
            supervisor = supervisorAgente,
            localidade = localidadeAgente,
            categoria = categoria,
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

    // ==================== PENDÊNCIAS ====================

// FECHADAS
    val fechadasResidencias = visitasUteis.count {
        it.pendencia.trim().equals("Fechado", ignoreCase = true) &&
                it.tipoImovel == "Residência"
    }

    val fechadasComercios = visitasUteis.count {
        it.pendencia.trim().equals("Fechado", ignoreCase = true) &&
                it.tipoImovel == "Comércio"
    }

    val fechadasTerrenos = visitasUteis.count {
        it.pendencia.trim().equals("Fechado", ignoreCase = true) &&
                it.tipoImovel == "Terreno"
    }

    val fechadasOutros = visitasUteis.count {
        it.pendencia.trim().equals("Fechado", ignoreCase = true) &&
                it.tipoImovel != "Residência" &&
                it.tipoImovel != "Comércio" &&
                it.tipoImovel != "Terreno" &&
                it.tipoImovel != "PE"
    }

    val fechadas = fechadasResidencias +
            fechadasComercios +
            fechadasTerrenos +
            fechadasOutros


// RECUSADAS
    val recusadasResidencias = visitasUteis.count {
        it.pendencia.trim().equals("Recusado", ignoreCase = true) &&
                it.tipoImovel == "Residência"
    }

    val recusadasComercios = visitasUteis.count {
        it.pendencia.trim().equals("Recusado", ignoreCase = true) &&
                it.tipoImovel == "Comércio"
    }

    val recusadasTerrenos = visitasUteis.count {
        it.pendencia.trim().equals("Recusado", ignoreCase = true) &&
                it.tipoImovel == "Terreno"
    }

    val recusadasOutros = visitasUteis.count {
        it.pendencia.trim().equals("Recusado", ignoreCase = true) &&
                it.tipoImovel != "Residência" &&
                it.tipoImovel != "Comércio" &&
                it.tipoImovel != "Terreno" &&
                it.tipoImovel != "PE"
    }

    val recusadas = recusadasResidencias +
            recusadasComercios +
            recusadasTerrenos +
            recusadasOutros


    // TOTAL DE PENDÊNCIAS
    val totalPendencias = fechadas + recusadas

    val imoveisComFoco = visitasUteis.count { it.foco }

    val totalEliminados = visitasUteis.sumOf {
        it.eliminadosA1 +
                it.eliminadosA2 +
                it.eliminadosB +
                it.eliminadosC +
                it.eliminadosD1 +
                it.eliminadosD2 +
                it.eliminadosE
    }

    // DEPÓSITOS ELIMINADOS POR TIPO DE IMÓVEL

    val residenciasEliminados = visitasUteis.sumOf {
        if (it.tipoImovel == "Residência") {
            it.eliminadosA1 +
                    it.eliminadosA2 +
                    it.eliminadosB +
                    it.eliminadosC +
                    it.eliminadosD1 +
                    it.eliminadosD2 +
                    it.eliminadosE
        } else 0
    }

    val comerciosEliminados = visitasUteis.sumOf {
        if (it.tipoImovel == "Comércio") {
            it.eliminadosA1 +
                    it.eliminadosA2 +
                    it.eliminadosB +
                    it.eliminadosC +
                    it.eliminadosD1 +
                    it.eliminadosD2 +
                    it.eliminadosE
        } else 0
    }

    val terrenosEliminados = visitasUteis.sumOf {
        if (it.tipoImovel == "Terreno") {
            it.eliminadosA1 +
                    it.eliminadosA2 +
                    it.eliminadosB +
                    it.eliminadosC +
                    it.eliminadosD1 +
                    it.eliminadosD2 +
                    it.eliminadosE
        } else 0
    }

    val outrosEliminados = visitasUteis.sumOf {
        if (
            it.tipoImovel != "Residência" &&
            it.tipoImovel != "Comércio" &&
            it.tipoImovel != "Terreno" &&
            it.tipoImovel != "PE"
        ) {
            it.eliminadosA1 +
                    it.eliminadosA2 +
                    it.eliminadosB +
                    it.eliminadosC +
                    it.eliminadosD1 +
                    it.eliminadosD2 +
                    it.eliminadosE
        } else 0
    }

    //TOTAL DE TRATADOS
    val totalTratados = visitasUteis.count {
        it.tratados && it.tipoImovel != "PE"
    }

    // ==================== IMÓVEIS TRATADOS POR TIPO ====================

    val residenciasTratadas = visitasUteis.count {
        it.tratados && it.tipoImovel == "Residência"
    }

    val comerciosTratados = visitasUteis.count {
        it.tratados && it.tipoImovel == "Comércio"
    }

    val terrenosTratados = visitasUteis.count {
        it.tratados && it.tipoImovel == "Terreno"
    }

    val outrosTratados = visitasUteis.count {
        it.tratados &&
                it.tipoImovel != "Residência" &&
                it.tipoImovel != "Comércio" &&
                it.tipoImovel != "Terreno" &&
                it.tipoImovel != "PE"
    }

    val totalA1 = visitasUteis.sumOf { it.a1 }
    val totalA2 = visitasUteis.sumOf { it.a2 }
    val totalB = visitasUteis.sumOf { it.b }
    val totalC = visitasUteis.sumOf { it.c }
    val totalD1 = visitasUteis.sumOf { it.d1 }
    val totalD2 = visitasUteis.sumOf { it.d2 }
    val totalE = visitasUteis.sumOf { it.e }

    val totalDepositosTratados =
        totalA1 + totalA2 + totalB + totalC + totalD1 + totalD2 + totalE

    // ==================== TRATAMENTOS POR TIPO DE IMÓVEL ====================

// RESIDÊNCIAS
    val residenciaA1 = visitasUteis
        .filter { it.tipoImovel == "Residência" }
        .sumOf { it.a1 }

    val residenciaA2 = visitasUteis
        .filter { it.tipoImovel == "Residência" }
        .sumOf { it.a2 }

    val residenciaB = visitasUteis
        .filter { it.tipoImovel == "Residência" }
        .sumOf { it.b }

    val residenciaC = visitasUteis
        .filter { it.tipoImovel == "Residência" }
        .sumOf { it.c }

    val residenciaD1 = visitasUteis
        .filter { it.tipoImovel == "Residência" }
        .sumOf { it.d1 }

    val residenciaD2 = visitasUteis
        .filter { it.tipoImovel == "Residência" }
        .sumOf { it.d2 }

    val residenciaE = visitasUteis
        .filter { it.tipoImovel == "Residência" }
        .sumOf { it.e }


// COMÉRCIOS
    val comercioA1 = visitasUteis
        .filter { it.tipoImovel == "Comércio" }
        .sumOf { it.a1 }

    val comercioA2 = visitasUteis
        .filter { it.tipoImovel == "Comércio" }
        .sumOf { it.a2 }

    val comercioB = visitasUteis
        .filter { it.tipoImovel == "Comércio" }
        .sumOf { it.b }

    val comercioC = visitasUteis
        .filter { it.tipoImovel == "Comércio" }
        .sumOf { it.c }

    val comercioD1 = visitasUteis
        .filter { it.tipoImovel == "Comércio" }
        .sumOf { it.d1 }

    val comercioD2 = visitasUteis
        .filter { it.tipoImovel == "Comércio" }
        .sumOf { it.d2 }

    val comercioE = visitasUteis
        .filter { it.tipoImovel == "Comércio" }
        .sumOf { it.e }


// TERRENOS
    val terrenoA1 = visitasUteis
        .filter { it.tipoImovel == "Terreno" }
        .sumOf { it.a1 }

    val terrenoA2 = visitasUteis
        .filter { it.tipoImovel == "Terreno" }
        .sumOf { it.a2 }

    val terrenoB = visitasUteis
        .filter { it.tipoImovel == "Terreno" }
        .sumOf { it.b }

    val terrenoC = visitasUteis
        .filter { it.tipoImovel == "Terreno" }
        .sumOf { it.c }

    val terrenoD1 = visitasUteis
        .filter { it.tipoImovel == "Terreno" }
        .sumOf { it.d1 }

    val terrenoD2 = visitasUteis
        .filter { it.tipoImovel == "Terreno" }
        .sumOf { it.d2 }

    val terrenoE = visitasUteis
        .filter { it.tipoImovel == "Terreno" }
        .sumOf { it.e }


// OUTROS
    val outrosA1 = visitasUteis
        .filter {
            it.tipoImovel != "Residência" &&
                    it.tipoImovel != "Comércio" &&
                    it.tipoImovel != "Terreno" &&
                    it.tipoImovel != "PE"
        }
        .sumOf { it.a1 }

    val outrosA2 = visitasUteis
        .filter {
            it.tipoImovel != "Residência" &&
                    it.tipoImovel != "Comércio" &&
                    it.tipoImovel != "Terreno" &&
                    it.tipoImovel != "PE"
        }
        .sumOf { it.a2 }

    val outrosB = visitasUteis
        .filter {
            it.tipoImovel != "Residência" &&
                    it.tipoImovel != "Comércio" &&
                    it.tipoImovel != "Terreno" &&
                    it.tipoImovel != "PE"
        }
        .sumOf { it.b }

    val outrosC = visitasUteis
        .filter {
            it.tipoImovel != "Residência" &&
                    it.tipoImovel != "Comércio" &&
                    it.tipoImovel != "Terreno" &&
                    it.tipoImovel != "PE"
        }
        .sumOf { it.c }

    val outrosD1 = visitasUteis
        .filter {
            it.tipoImovel != "Residência" &&
                    it.tipoImovel != "Comércio" &&
                    it.tipoImovel != "Terreno" &&
                    it.tipoImovel != "PE"
        }
        .sumOf { it.d1 }

    val outrosD2 = visitasUteis
        .filter {
            it.tipoImovel != "Residência" &&
                    it.tipoImovel != "Comércio" &&
                    it.tipoImovel != "Terreno" &&
                    it.tipoImovel != "PE"
        }
        .sumOf { it.d2 }

    val outrosE = visitasUteis
        .filter {
            it.tipoImovel != "Residência" &&
                    it.tipoImovel != "Comércio" &&
                    it.tipoImovel != "Terreno" &&
                    it.tipoImovel != "PE"
        }
        .sumOf { it.e }

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
                LinhaResumo(
                    titulo = "Fechadas",
                    valor = fechadas.toString()
                )

                LinhaResumo(
                    titulo = "  Residências fechadas",
                    valor = fechadasResidencias.toString()
                )

                LinhaResumo(
                    titulo = "  Comércios fechados",
                    valor = fechadasComercios.toString()
                )

                LinhaResumo(
                    titulo = "  Terrenos fechados",
                    valor = fechadasTerrenos.toString()
                )

                LinhaResumo(
                    titulo = "  Outros fechados",
                    valor = fechadasOutros.toString()
                )

                LinhaResumo(
                    titulo = "Recusadas",
                    valor = recusadas.toString()
                )

                LinhaResumo(
                    titulo = "  Residências recusadas",
                    valor = recusadasResidencias.toString()
                )

                LinhaResumo(
                    titulo = "  Comércios recusados",
                    valor = recusadasComercios.toString()
                )

                LinhaResumo(
                    titulo = "  Terrenos recusados",
                    valor = recusadasTerrenos.toString()
                )

                LinhaResumo(
                    titulo = "  Outros recusados",
                    valor = recusadasOutros.toString()
                )

                LinhaResumo(
                    titulo = "Total de pendências",
                    valor = totalPendencias.toString()
                )

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFF26304F)
                )

                            //RESUMO TRATADOS

                LinhaResumo(
                    titulo = "Imóveis tratados",
                    valor = totalTratados.toString()
                )

                LinhaResumo(
                    titulo = "  Residências tratadas",
                    valor = residenciasTratadas.toString()
                )

                LinhaResumo(
                    titulo = "  Comércios tratados",
                    valor = comerciosTratados.toString()
                )

                LinhaResumo(
                    titulo = "  Terrenos tratados",
                    valor = terrenosTratados.toString()
                )

                LinhaResumo(
                    titulo = "  Outros tratados",
                    valor = outrosTratados.toString()
                )

                LinhaResumo(
                    titulo = "Depósitos tratados",
                    valor = totalDepositosTratados.toString()
                )

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

                LinhaResumo("RESIDÊNCIAS", "")
                LinhaResumo("• A1", residenciaA1.toString())
                LinhaResumo("• A2", residenciaA2.toString())
                LinhaResumo("• B", residenciaB.toString())
                LinhaResumo("• C", residenciaC.toString())
                LinhaResumo("• D1", residenciaD1.toString())
                LinhaResumo("• D2", residenciaD2.toString())
                LinhaResumo("• E", residenciaE.toString())

                LinhaResumo("COMÉRCIOS", "")
                LinhaResumo("• A1", comercioA1.toString())
                LinhaResumo("• A2", comercioA2.toString())
                LinhaResumo("• B", comercioB.toString())
                LinhaResumo("• C", comercioC.toString())
                LinhaResumo("• D1", comercioD1.toString())
                LinhaResumo("• D2", comercioD2.toString())
                LinhaResumo("• E", comercioE.toString())

                LinhaResumo("TERRENOS", "")
                LinhaResumo("• A1", terrenoA1.toString())
                LinhaResumo("• A2", terrenoA2.toString())
                LinhaResumo("• B", terrenoB.toString())
                LinhaResumo("• C", terrenoC.toString())
                LinhaResumo("• D1", terrenoD1.toString())
                LinhaResumo("• D2", terrenoD2.toString())
                LinhaResumo("• E", terrenoE.toString())

                LinhaResumo("OUTROS", "")
                LinhaResumo("• A1", outrosA1.toString())
                LinhaResumo("• A2", outrosA2.toString())
                LinhaResumo("• B", outrosB.toString())
                LinhaResumo("• C", outrosC.toString())
                LinhaResumo("• D1", outrosD1.toString())
                LinhaResumo("• D2", outrosD2.toString())
                LinhaResumo("• E", outrosE.toString())

                            //RESUMO DEPOSITOS ELIMINADOS

                LinhaResumo("Depósitos eliminados", totalEliminados.toString()

                )

                LinhaResumo("• A1", totalA1.toString())
                LinhaResumo("• A2", totalA2.toString())
                LinhaResumo("• B", totalB.toString())
                LinhaResumo("• C", totalC.toString())
                LinhaResumo("• D1", totalD1.toString())
                LinhaResumo("• D2", totalD2.toString())
                LinhaResumo("• E", totalE.toString())

                LinhaResumo("RESIDÊNCIAS", "")
                LinhaResumo("• A1", residenciaA1.toString())
                LinhaResumo("• A2", residenciaA2.toString())
                LinhaResumo("• B", residenciaB.toString())
                LinhaResumo("• C", residenciaC.toString())
                LinhaResumo("• D1", residenciaD1.toString())
                LinhaResumo("• D2", residenciaD2.toString())
                LinhaResumo("• E", residenciaE.toString())

                LinhaResumo("COMÉRCIOS", "")
                LinhaResumo("• A1", comercioA1.toString())
                LinhaResumo("• A2", comercioA2.toString())
                LinhaResumo("• B", comercioB.toString())
                LinhaResumo("• C", comercioC.toString())
                LinhaResumo("• D1", comercioD1.toString())
                LinhaResumo("• D2", comercioD2.toString())
                LinhaResumo("• E", comercioE.toString())

                LinhaResumo("TERRENOS", "")
                LinhaResumo("• A1", terrenoA1.toString())
                LinhaResumo("• A2", terrenoA2.toString())
                LinhaResumo("• B", terrenoB.toString())
                LinhaResumo("• C", terrenoC.toString())
                LinhaResumo("• D1", terrenoD1.toString())
                LinhaResumo("• D2", terrenoD2.toString())
                LinhaResumo("• E", terrenoE.toString())

                LinhaResumo("OUTROS", "")
                LinhaResumo("• A1", outrosA1.toString())
                LinhaResumo("• A2", outrosA2.toString())
                LinhaResumo("• B", outrosB.toString())
                LinhaResumo("• C", outrosC.toString())
                LinhaResumo("• D1", outrosD1.toString())
                LinhaResumo("• D2", outrosD2.toString())
                LinhaResumo("• E", outrosE.toString())
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
    quarteirao: String,
    onQuarteiraoChange: (String) -> Unit,
    onIniciar: () -> Unit,
    onVoltar: () -> Unit
) {
    BackHandler {
        onVoltar()
    }


    var atividadeExpanded by remember { mutableStateOf(false) }
    var categoriaExpanded by remember { mutableStateOf(false) }
    var mostrarErro by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
                    modifier = Modifier
                        .width(210.dp),
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
                            .width(118.dp)
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
                            .width(165.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = atividadeExpanded,
                        onDismissRequest = {
                            atividadeExpanded = false
                        }
                    ) {
                        listOf("Tratamento",
                            "Ação 1ª V",
                            "Ação 2ª V",
                            "Ação 3ª V",
                            "Ação 4ª V",
                            "Ação 5ª V",
                            "Ação Bloq."
                        ).forEach {
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

                OutlinedTextField(
                    value = quarteirao,
                    onValueChange = onQuarteiraoChange,
                    label = {
                        Text("Quarteirão")
                    },
                    shape = RoundedCornerShape(30.dp),
                    colors = campoColors,

                    modifier = Modifier
                        .width(125.dp),
                    singleLine = true
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
                    value = dataAtual,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data") },
                    shape = RoundedCornerShape(30.dp),
                    colors = campoColors,
                    modifier = Modifier.width(165.dp)
                )

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

                    shape = RoundedCornerShape(30.dp),
                    colors = campoColors,

                    modifier = Modifier
                        .width(125.dp)
                )

            }
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
                            cicloAno.trim().isEmpty() ||
                            quarteirao.trim().isEmpty()
                        ) {
                            mostrarErro = true
                        } else {

                            scope.launch {

                                if (atividade == "Tratamento") {

                                    val anoAtual = LocalDate.now().year

                                    val rgFinalizado = AppDatabase
                                        .get(context)
                                        .rgDao()
                                        .estaFinalizado(
                                            ano = anoAtual,
                                            ciclo = cicloAno,
                                            quarteirao = quarteirao.trim()
                                        ) == true

                                    if (rgFinalizado) {
                                        mostrarErro = true
                                        return@launch
                                    }
                                }

                                mostrarErro = false
                                onIniciar()
                            }
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

        var eliminadosExpandido by remember { mutableStateOf(false) }

        var eliminadosA1 by remember { mutableIntStateOf(0) }
        var eliminadosA2 by remember { mutableIntStateOf(0) }
        var eliminadosB by remember { mutableIntStateOf(0) }
        var eliminadosC by remember { mutableIntStateOf(0) }
        var eliminadosD1 by remember { mutableIntStateOf(0) }
        var eliminadosD2 by remember { mutableIntStateOf(0) }
        var eliminadosE by remember { mutableIntStateOf(0) }

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

                    FilterChip(
                        selected = tipoImovel == "PE",
                        onClick = { tipoImovel = "PE" },

                        label = {
                            Text(
                                text = "PE",
                                color = Color.White
                            )
                        },

                        shape = RoundedCornerShape(size = 50.dp),

                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            labelColor = Color(0xFFFDC08D),
                            selectedContainerColor = Color(0xFFE53A9E),
                            selectedLabelColor = Color.White
                        ),

                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = tipoImovel == "PE",
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
                                    "PE"
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    val totalEliminados =
                        eliminadosA1 +
                                eliminadosA2 +
                                eliminadosB +
                                eliminadosC +
                                eliminadosD1 +
                                eliminadosD2 +
                                eliminadosE

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                            Checkbox(
                                checked = eliminadosExpandido,
                                onCheckedChange = { marcado ->

                                    eliminadosExpandido = marcado

                                    if (!marcado) {
                                        eliminadosA1 = 0
                                        eliminadosA2 = 0
                                        eliminadosB = 0
                                        eliminadosC = 0
                                        eliminadosD1 = 0
                                        eliminadosD2 = 0
                                        eliminadosE = 0
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


                    if (eliminadosExpandido) {

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            // A1
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "A1",
                                    color = Color.White,
                                    modifier = Modifier.width(50.dp)
                                )

                                Button(
                                    onClick = {
                                        if (eliminadosA1 > 0) {
                                            eliminadosA1--
                                        }
                                    }
                                ) {
                                    Text("-")
                                }

                                Text(
                                    text = eliminadosA1.toString(),
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Button(
                                    onClick = {
                                        eliminadosA1++
                                    }
                                ) {
                                    Text("+")
                                }
                            }

                            // A2
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "A2",
                                    color = Color.White,
                                    modifier = Modifier.width(50.dp)
                                )

                                Button(
                                    onClick = {
                                        if (eliminadosA2 > 0) {
                                            eliminadosA2--
                                        }
                                    }
                                ) {
                                    Text("-")
                                }

                                Text(
                                    text = eliminadosA2.toString(),
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Button(
                                    onClick = {
                                        eliminadosA2++
                                    }
                                ) {
                                    Text("+")
                                }
                            }

                            // B
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "B",
                                    color = Color.White,
                                    modifier = Modifier.width(50.dp)
                                )

                                Button(
                                    onClick = {
                                        if (eliminadosB > 0) {
                                            eliminadosB--
                                        }
                                    }
                                ) {
                                    Text("-")
                                }

                                Text(
                                    text = eliminadosB.toString(),
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Button(
                                    onClick = {
                                        eliminadosB++
                                    }
                                ) {
                                    Text("+")
                                }
                            }

                            // C
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "C",
                                    color = Color.White,
                                    modifier = Modifier.width(50.dp)
                                )

                                Button(
                                    onClick = {
                                        if (eliminadosC > 0) {
                                            eliminadosC--
                                        }
                                    }
                                ) {
                                    Text("-")
                                }

                                Text(
                                    text = eliminadosC.toString(),
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Button(
                                    onClick = {
                                        eliminadosC++
                                    }
                                ) {
                                    Text("+")
                                }
                            }

                            // D1
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "D1",
                                    color = Color.White,
                                    modifier = Modifier.width(50.dp)
                                )

                                Button(
                                    onClick = {
                                        if (eliminadosD1 > 0) {
                                            eliminadosD1--
                                        }
                                    }
                                ) {
                                    Text("-")
                                }

                                Text(
                                    text = eliminadosD1.toString(),
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Button(
                                    onClick = {
                                        eliminadosD1++
                                    }
                                ) {
                                    Text("+")
                                }
                            }

                            // D2
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "D2",
                                    color = Color.White,
                                    modifier = Modifier.width(50.dp)
                                )

                                Button(
                                    onClick = {
                                        if (eliminadosD2 > 0) {
                                            eliminadosD2--
                                        }
                                    }
                                ) {
                                    Text("-")
                                }

                                Text(
                                    text = eliminadosD2.toString(),
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Button(
                                    onClick = {
                                        eliminadosD2++
                                    }
                                ) {
                                    Text("+")
                                }
                            }

                            // E
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "E",
                                    color = Color.White,
                                    modifier = Modifier.width(50.dp)
                                )

                                Button(
                                    onClick = {
                                        if (eliminadosE > 0) {
                                            eliminadosE--
                                        }
                                    }
                                ) {
                                    Text("-")
                                }

                                Text(
                                    text = eliminadosE.toString(),
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Button(
                                    onClick = {
                                        eliminadosE++
                                    }
                                ) {
                                    Text("+")
                                }
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

                                eliminadosA1 = eliminadosA1,
                                eliminadosA2 = eliminadosA2,
                                eliminadosB = eliminadosB,
                                eliminadosC = eliminadosC,
                                eliminadosD1 = eliminadosD1,
                                eliminadosD2 = eliminadosD2,
                                eliminadosE = eliminadosE,

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
                            eliminadosExpandido = false
                            eliminadosA1 = 0
                            eliminadosA2 = 0
                            eliminadosB = 0
                            eliminadosC = 0
                            eliminadosD1 = 0
                            eliminadosD2 = 0
                            eliminadosE = 0
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

                            exportarBoletimXlsx(
                                context = context,
                                cabecalho = cabecalho,
                                visitas = visitas
                            )
                        },
                        modifier = Modifier.fillMaxWidth(0.56f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text("Testar XLSX")
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
                                eliminadosA1 = visita.eliminadosA1
                                eliminadosA2 = visita.eliminadosA2
                                eliminadosB = visita.eliminadosB
                                eliminadosC = visita.eliminadosC
                                eliminadosD1 = visita.eliminadosD1
                                eliminadosD2 = visita.eliminadosD2
                                eliminadosE = visita.eliminadosE
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

                        val totalEliminados =
                            visita.eliminadosA1 +
                                    visita.eliminadosA2 +
                                    visita.eliminadosB +
                                    visita.eliminadosC +
                                    visita.eliminadosD1 +
                                    visita.eliminadosD2 +
                                    visita.eliminadosE

                        if (totalEliminados > 0) {
                            Text("Eliminados: $totalEliminados")
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
        ano = LocalDate.now().year,
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

        eliminadosA1 = 0,
        eliminadosA2 = 0,
        eliminadosB = 0,
        eliminadosC = 0,
        eliminadosD1 = 0,
        eliminadosD2 = 0,
        eliminadosE = 0,

        tratados = tratamento != null,

        larvicidaGramas = tratamento?.gramas ?: 0.0

    )
}

@Composable
fun TelaRG(
    agente: String,
    supervisor: String,
    localidade: String,
    categoria: String,
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
        val todasVisitas = AppDatabase
            .get(context)
            .visitaDao()
            .listarTodasParaRg()

        visitas = todasVisitas
            .groupBy {
                listOf(
                    it.quarteirao,
                    it.rua,
                    it.numero,
                    it.sequencia,
                    it.complemento
                )
            }
            .values
            .mapNotNull { registros ->
                registros.maxByOrNull { it.id }
            }
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
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val visitasDoQuarteirao = visitas
                                    .filter {
                                        it.quarteirao == quarteirao
                                    }
                                    .map {
                                        Visita(
                                            cicloAno = it.ciclo,
                                            quarteirao = it.quarteirao,
                                            rua = it.rua,
                                            numero = it.numero,
                                            sequencia = it.sequencia,
                                            complemento = it.complemento,
                                            tipoImovel = it.tipoImovel,
                                            inspecionado = it.inspecionado,
                                            pendencia = it.pendencia,
                                            foco = it.foco,
                                            eliminadosA1 = it.eliminadosA1,
                                            eliminadosA2 = it.eliminadosA2,
                                            eliminadosB = it.eliminadosB,
                                            eliminadosC = it.eliminadosC,
                                            eliminadosD1 = it.eliminadosD1,
                                            eliminadosD2 = it.eliminadosD2,
                                            eliminadosE = it.eliminadosE,
                                            tratamento = Tratamento(
                                                a1 = it.a1,
                                                a2 = it.a2,
                                                b = it.b,
                                                c = it.c,
                                                d1 = it.d1,
                                                d2 = it.d2,
                                                e = it.e,
                                                gramas = it.larvicidaGramas
                                            ),
                                            observacao = ""
                                        )
                                    }

                                val cabecalho = CabecalhoBoletim(
                                    nome = agente,
                                    supervisor = supervisor,
                                    data = LocalDate.now().format(
                                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    ),
                                    localidade = localidade,
                                    quarteirao = quarteirao,
                                    atividade = "RG",
                                    categoria = categoria,
                                    ciclo = ciclo
                                )

                                gerarRgPdf(
                                    context = context,
                                    cabecalho = cabecalho,
                                    quarteirao = quarteirao,
                                    visitas = visitasDoQuarteirao
                                )
                            }
                        ) {
                            Text("Gerar RG")
                        }
                    }
                }
            }
        }
    }
}