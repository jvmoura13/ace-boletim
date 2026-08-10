package br.com.jvmoura.aceboletim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                NovoBoletimScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoBoletimScreen() {

    val atividades = listOf("Tratamento", "Ação")
    val categorias = listOf("Bairro", "Area rural")

    var atividade by remember { mutableStateOf(atividades[0]) }
    var categoria by remember { mutableStateOf(categorias[0]) }

    var atividadeExpanded by remember { mutableStateOf(false) }
    var categoriaExpanded by remember { mutableStateOf(false) }

    var cicloAno by remember { mutableStateOf("") }
    var local by remember { mutableStateOf("") }
    val hoje = java.time.LocalDate.now()
    val dataFormatada = hoje.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    var dataAtividade by remember { mutableStateOf(dataFormatada) }
    var agente by remember { mutableStateOf("") }
    var supervisor by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Novo Boletim",
            style = MaterialTheme.typography.headlineMedium
        )

        ExposedDropdownMenuBox(
            expanded = atividadeExpanded,
            onExpandedChange = { atividadeExpanded = !atividadeExpanded }
        ) {
            OutlinedTextField(
                value = atividade,
                onValueChange = {},
                readOnly = true,
                label = { Text("Atividade") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = atividadeExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = atividadeExpanded,
                onDismissRequest = { atividadeExpanded = false }
            ) {
                atividades.forEach {
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

        ExposedDropdownMenuBox(
            expanded = categoriaExpanded,
            onExpandedChange = { categoriaExpanded = !categoriaExpanded }
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
                categorias.forEach {
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

        OutlinedTextField(
            value = cicloAno,
            onValueChange = { cicloAno = it },
            label = { Text("Ciclo/Ano") },
            placeholder = { Text("Ex.: 5/2026") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = local,
            onValueChange = { local = it },
            label = { Text("Local") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = dataAtividade,
            onValueChange = { dataAtividade = it },
            label = { Text("Data da atividade") },
            placeholder = { Text("10/08/2026") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = agente,
            onValueChange = { agente = it },
            label = { Text("Agente") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = supervisor,
            onValueChange = { supervisor = it },
            label = { Text("Supervisor") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar boletim")
        }
    }
}