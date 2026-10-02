package com.example.listadoalumnos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelperAlumno(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Alumnos.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_ALUMNOS = "Alumnos"
        const val COLUMN_CUENTA = "cuenta"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_CORREO = "correo"
        const val COLUMN_IMAGE = "image"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE " + TABLE_ALUMNOS + " ("
                + COLUMN_CUENTA + " TEXT PRIMARY KEY, "
                + COLUMN_NOMBRE + " TEXT, "
                + COLUMN_CORREO + " TEXT, "
                + COLUMN_IMAGE + " TEXT)")
        db.execSQL(createTable)

        // Insertar datos de prueba iniciales
        insertarDatosIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALUMNOS")
        onCreate(db)
    }

    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        val alumnosIniciales = listOf(
            Alumno("20210001", "Ana Garcia", "ana.garcia@estudiante.edu", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150"),
            Alumno("20210002", "Carlos Lopez", "carlos.lopez@estudiante.edu", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150"),
            Alumno("20210003", "Maria Rodriguez", "maria.rodriguez@estudiante.edu", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150")
        )

        for (alumno in alumnosIniciales) {
            val values = ContentValues().apply {
                put(COLUMN_CUENTA, alumno.cuenta)
                put(COLUMN_NOMBRE, alumno.nombre)
                put(COLUMN_CORREO, alumno.correo)
                put(COLUMN_IMAGE, alumno.image)
            }
            db.insert(TABLE_ALUMNOS, null, values)
        }
    }

    fun agregarAlumno(alumno: Alumno): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CUENTA, alumno.cuenta)
            put(COLUMN_NOMBRE, alumno.nombre)
            put(COLUMN_CORREO, alumno.correo)
            put(COLUMN_IMAGE, alumno.image)
        }
        val result = db.insert(TABLE_ALUMNOS, null, values)
        db.close()
        return result != -1L
    }

    fun insertarAlumno(alumno: Alumno): Boolean = agregarAlumno(alumno)

    fun obtenerAlumnos(): List<Alumno> {
        val lista = ArrayList<Alumno>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ALUMNOS", null)

        if (cursor.moveToFirst()) {
            do {
                val cuentaIndex = cursor.getColumnIndex(COLUMN_CUENTA)
                val nombreIndex = cursor.getColumnIndex(COLUMN_NOMBRE)
                val correoIndex = cursor.getColumnIndex(COLUMN_CORREO)
                val imageIndex = cursor.getColumnIndex(COLUMN_IMAGE)

                val cuenta = if (cuentaIndex >= 0) cursor.getString(cuentaIndex) else ""
                val nombre = if (nombreIndex >= 0) cursor.getString(nombreIndex) else ""
                val correo = if (correoIndex >= 0) cursor.getString(correoIndex) else ""
                val image = if (imageIndex >= 0) cursor.getString(imageIndex) else ""

                lista.add(Alumno(cuenta, nombre, correo, image))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun obtenerTodosAlumnos(): List<Alumno> = obtenerAlumnos()

    fun actualizarAlumno(alumno: Alumno): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, alumno.nombre)
            put(COLUMN_CORREO, alumno.correo)
            put(COLUMN_IMAGE, alumno.image)
        }
        val rowsAffected = db.update(
            TABLE_ALUMNOS,
            values,
            "$COLUMN_CUENTA = ?",
            arrayOf(alumno.cuenta)
        )
        db.close()
        return rowsAffected > 0
    }

    fun eliminarAlumno(cuenta: String): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(
            TABLE_ALUMNOS,
            "$COLUMN_CUENTA = ?",
            arrayOf(cuenta)
        )
        db.close()
        return rowsDeleted > 0
    }
}
