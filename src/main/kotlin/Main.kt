package org.example

import java.nio.file.*
import java.io.*

fun main() {
    // Ruta del fichero
    val ficheroCotizaciones = "src/main/resources/cotizacion.csv"

    // Ruta fichero de destino
    val ficheroModificado: Path = Paths.get("src/main/resources/cotizacion2.csv")

    // Lectura del fichero
    val lectura = leerFichero(ficheroCotizaciones)

    // Escritura del fichero
    escribirFichero(lectura, ficheroModificado)

    // Imprime el contenido del mapa
    lectura.forEach { columna, valores ->
        println("Columna: $columna, Valores: $valores")
    }
}

fun leerFichero(filePath: String): MutableMap<String, List<String>> {
    val lineas = File(filePath).readLines()

    // Esta contiene los nombres de las columnas
    val nombresColumnas = lineas.first().split(";")

    // Mapa para almacenar los datos por columna
    val datosColumnas = mutableMapOf<String, MutableList<String>>()

    // Inicializo el mapa con las columnas
    for (columna in nombresColumnas) {
        datosColumnas[columna] = mutableListOf()
    }

    // Lleno el mapa con los valores
    for (linea in lineas.drop(1)) {  // salto la primera línea por que es la cabecera
        val valores = linea.split(";")
        for ((indice, valor) in valores.withIndex()) {
            val nombreColumna = nombresColumnas[indice]

            // Se reemplazan los puntos por nada y las comas por puntos para evitar errores
            val valorLimpio = valor.replace(".", "").replace(",", ".")
            datosColumnas[nombreColumna]?.add(valorLimpio)
        }
    }

    // Devolver como un mapa inmutable, por que sino me daba error
    return datosColumnas.mapValues { it.value.toList() }.toMutableMap()
}

fun escribirFichero(diccionario: MutableMap<String, List<String>>, ruta: Path) {
    // Se elimina la columna "Nombre"
    diccionario.remove("Nombre")

    // Creo el directorio si no existe
    Files.createDirectories(ruta.parent)

    val bw: BufferedWriter = Files.newBufferedWriter(ruta, StandardOpenOption.CREATE, StandardOpenOption.WRITE)

    // Escritura en el fichero
    bw.use { writer ->
        diccionario.forEach { (key, value) ->
            // Intento de conversión a double para las estadísticas
            val doubleList: List<Double> = value.mapNotNull { it.toDoubleOrNull() }

            // Solo escribe si la lista no está vacía
            if (doubleList.isNotEmpty()) {
                writer.write("Columna ${key} - Mínimo: ${doubleList.minOrNull()}, Máximo: ${doubleList.maxOrNull()}, Media: ${doubleList.average()}\n")
                writer.newLine()
            } else {
                writer.write("Columna ${key} - No se pudieron calcular estadísticas\n")
                writer.newLine()
            }
        }
    }
}
