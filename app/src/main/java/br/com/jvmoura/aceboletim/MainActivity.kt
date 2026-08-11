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
    var boletimIniciado by remember { mutableStateOf(false) }
    BackHandler(enabled = boletimIniciado) {
        boletimIniciado = false
    }
    if (!boletimIniciado) {
        TelaNovoBoletim(
            onIniciar = { boletimIniciado = true }
        )
    } else {
        TelaVisitas()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaNovoBoletim(onIniciar: () -> Unit) {

    var atividade by remember { mutableStateOf("Tratamento") }
    var atividadeExpanded by remember { mutableStateOf(false) }
    var categoria by remember { mutableStateOf("Bairro") }
    var categoriaExpanded by remember { mutableStateOf(false) }
    var cicloAno by remember { mutableStateOf("") }
    var local by remember { mutableStateOf("") }
    var agente by remember { mutableStateOf("") }
    var supervisor by remember { mutableStateOf("") }
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
                                atividade = it
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
                                categoria = it
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
                onValueChange = { cicloAno = it },
                label = { Text("Ciclo/Ano") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = local,
                onValueChange = { local = it },
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
                onValueChange = { agente = it },
                label = { Text("Agente") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = supervisor,
                onValueChange = { supervisor = it },
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
                        .fillMaxWidth(0.25f)
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
    fun TelaVisitas() {
        val context = LocalContext.current

        var visitas by remember { mutableStateOf(listOf<Visita>()) }
        var indiceEditando by remember { mutableStateOf<Int?>(null) }

        var quarteirao by remember { mutableStateOf("") }
        var rua by remember { mutableStateOf("") }
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
                        label = { Text("Complemento") },
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
                            .fillMaxWidth(0.50f)
                            .height(65.dp),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            text = "Abrir Google Maps",
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

                            indiceEditando = null
                            // volta para o padrão da próxima visita
                            inspecionado = true
                            possuiPendencia = false
                            pendencia = ""
                            observacao = ""
                            numero = ""
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
                            .fillMaxWidth(0.25f)
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

            item { HorizontalDivider() }

            item {
                Text("Visitas do dia", style = MaterialTheme.typography.headlineSmall)
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
                            text = if (visita.complemento.isBlank())
                                visita.numero
                            else
                                "${visita.numero} - Comp. ${visita.complemento}",
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
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

