package br.com.jvmoura.aceboletim

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
        canvasAtual.drawText(if (visita.pendencia.isNotBlank()) "S" else "N", colPend, y.toFloat(), paint)
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
    pdf.finishPage(paginaAtual)

    val pasta =
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val nomeArquivo =
        "Boletim_${cabecalho.localidade}_Q${cabecalho.quarteirao}_${cabecalho.data.replace("/", "-")}.pdf"
    val arquivo = File(pasta, nomeArquivo)
    pdf.writeTo(FileOutputStream(arquivo))
    pdf.close() }
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AppACE()
            }
        }
    }
}

@Composable
fun AppACE() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var boletimIniciado by remember { mutableStateOf(false) }
    var atividade by remember { mutableStateOf("Tratamento") }
    var categoria by remember { mutableStateOf("Bairro") }
    var cicloAno by remember { mutableStateOf("") }
    var local by remember { mutableStateOf("") }
    var agente by remember { mutableStateOf("") }
    var supervisor by remember { mutableStateOf("") }
    var visitasSalvas by remember { mutableStateOf(listOf<Visita>()) }
    var visitas by remember { mutableStateOf(listOf<Visita>()) }

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
    if (!boletimIniciado) {
        TelaNovoBoletim(
            atividade = atividade,
            onAtividadeChange = {
                atividade = it
            },

            categoria = categoria,
            onCategoriaChange = {
                categoria = it
            },

            agente = agente,
            onAgenteChange = {
                agente = it
             },

            supervisor = supervisor,
            onSupervisorChange = {
                supervisor = it
             },

            local = local,
            onLocalChange = {
                local = it
            },

            cicloAno = cicloAno,
            onCicloChange = {
                cicloAno = it
                },
            onIniciar = {
                boletimIniciado = true
            }
        )
     } else {
        TelaVisitas(
            atividade = atividade,
            categoria = categoria,
            agente = agente,
            supervisor = supervisor,
            localidade = local,
            ciclo = cicloAno,
            visitasIniciais = visitasSalvas
        )
    }
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
    onIniciar: () -> Unit
) {


    var atividadeExpanded by remember { mutableStateOf(false) }
    var categoriaExpanded by remember { mutableStateOf(false) }
    var mostrarErro by remember { mutableStateOf(false) }

    val dataAtual = remember {
        LocalDate.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Boletim Díario",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }


        item {
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
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded =
                                atividadeExpanded
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
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
        }

        item {
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
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
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

        item {
            OutlinedTextField(
                value = cicloAno,
                onValueChange = onCicloChange,
                label = { Text("Ciclo/Ano") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = local,
                onValueChange = onLocalChange,
                label = { Text("Localidade") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = dataAtual,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = agente,
                onValueChange = onAgenteChange,
                label = { Text("Agente") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = supervisor,
                onValueChange = onSupervisorChange,
                label = { Text("Supervisor") },
                modifier = Modifier.fillMaxWidth()
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
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        text = "Iniciar Boletim",
                        style = MaterialTheme.typography.titleMedium
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
        visitasIniciais: List<Visita>

    ) {
        val context = LocalContext.current

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Visitas do Dia",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = quarteirao,
                    onValueChange = { quarteirao = it },
                    label = { Text("Quarteirão") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = rua,
                    onValueChange = { rua = it },
                    label = { Text("Rua") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = numero,
                        onValueChange = { numero = it },
                        label = { Text("Número") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = sequencia,
                        onValueChange = { sequencia = it },
                        label = { Text("Sequência") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = complemento,
                        onValueChange = { complemento = it },
                        label = { Text("Comple.") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {

                            val uri = Uri.parse("geo:0,0?q=minha+localizacao")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.setPackage("com.google.android.apps.maps")
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.44f)
                            .height(65.dp),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            text = "Google Maps",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            item {
                Text("Tipo do imóvel", style = MaterialTheme.typography.titleMedium)
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = tipoImovel == "Residência",
                        onClick = { tipoImovel = "Residência" },
                        label = { Text("Residência") }
                    )

                    FilterChip(
                        selected = tipoImovel == "Terreno",
                        onClick = { tipoImovel = "Terreno" },
                        label = { Text("Terreno") }
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = tipoImovel == "Comércio",
                        onClick = { tipoImovel = "Comércio" },
                        label = { Text("Comércio") }
                    )

                    FilterChip(
                        selected = tipoImovel == "Outros",
                        onClick = { tipoImovel = "Outros" },
                        label = { Text("Outros") }
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
                            }
                        )
                        Text("Imóvel inspecionado")
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
                            }
                        )
                        Text("Pendência")
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
                                label = { Text("Tipo de pendência") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = pendenciaExpanded
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = pendenciaExpanded,
                                onDismissRequest = {
                                    pendenciaExpanded = false
                                }
                            ) {
                                listOf(
                                    "Fechado",
                                    "Recusado",
                                    "Abandonado",
                                    "Outros"
                                ).forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
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
                            onCheckedChange = { tratado = it }
                        )

                        Text("Depósitos tratados")
                    }

                    if (tratado) {

                        HorizontalDivider()

                        Text(
                            text = "Quantidade por tipo",
                            style = MaterialTheme.typography.titleMedium
                        )

                        // A1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "A1",
                                modifier = Modifier.weight(1f)
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
                                modifier = Modifier.weight(1f)
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
                                modifier = Modifier.weight(1f)
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
                                modifier = Modifier.weight(1f)
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
                                modifier = Modifier.weight(1f)
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
                                modifier = Modifier.weight(1f)
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
                                modifier = Modifier.weight(1f)
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
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = foco,
                        onCheckedChange = { foco = it }
                    )
                    Text("Foco encontrado")
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
                            }
                        )

                        Text("Depósitos eliminados")
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
                    label = { Text("Observações") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
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
                            .fillMaxWidth(0.45f)
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
                            gerarBoletimPdf(context, cabecalho, visitas)
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
                    Text("Visitas do dia", style = MaterialTheme.typography.headlineSmall)
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

