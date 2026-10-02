package com.example.listadoalumnos

data class Alumno(
    val cuenta: String,
    val nombre: String,
    val correo: String,
    val image: String
) {
    val imagen: String
        get() = image
}
