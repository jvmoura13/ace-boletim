package br.com.jvmoura.aceboletim

data class BoletimRascunho(
    val cabecalho: CabecalhoBoletim,
    val visitas: List<Visita>,
    val visitaAtual: Visita,
    val indiceAtual: Int
)