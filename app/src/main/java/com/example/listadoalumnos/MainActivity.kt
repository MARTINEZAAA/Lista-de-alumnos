package com.example.listadoalumnos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.listadoalumnos.databinding.ActivityMainBinding
import com.example.listadoalumnos.databinding.DialogAlumnoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DBHelperAlumno
    private lateinit var adapter: AlumnoAdapter
    private var listaCompletaAlumnos: List<Alumno> = emptyList()

    companion object {
        private const val AVATAR_1 = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200"
        private const val AVATAR_2 = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200"
        private const val AVATAR_3 = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DBHelperAlumno(this)

        configurarRecyclerView()
        configurarBuscador()

        binding.fabAgregar.setOnClickListener {
            mostrarDialogoRegistro(alumno = null)
        }

        binding.btnPrimerAlumno.setOnClickListener {
            mostrarDialogoRegistro(alumno = null)
        }

        cargarAlumnos()
    }

    private fun configurarRecyclerView() {
        adapter = AlumnoAdapter(
            listaAlumnos = emptyList(),
            onEditarClick = { alumno -> mostrarDialogoRegistro(alumno) },
            onEliminarClick = { alumno -> confirmarEliminacion(alumno) }
        )

        binding.rvAlumnos.layoutManager = LinearLayoutManager(this)
        binding.rvAlumnos.adapter = adapter
    }

    private fun configurarBuscador() {
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarAlumnos(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun cargarAlumnos() {
        listaCompletaAlumnos = dbHelper.obtenerAlumnos()
        val query = binding.etBuscar.text.toString()
        filtrarAlumnos(query)
    }

    private fun filtrarAlumnos(query: String) {
        val listaFiltrada = if (query.isBlank()) {
            listaCompletaAlumnos
        } else {
            listaCompletaAlumnos.filter { alumno ->
                alumno.nombre.contains(query, ignoreCase = true) ||
                        alumno.cuenta.contains(query, ignoreCase = true) ||
                        alumno.correo.contains(query, ignoreCase = true)
            }
        }

        actualizarEstadoLista(listaFiltrada)
    }

    private fun actualizarEstadoLista(lista: List<Alumno>) {
        val total = listaCompletaAlumnos.size
        binding.tvTotalAlumnos.text = if (total == 1) "1 Registrado" else "$total Registrados"

        if (lista.isEmpty()) {
            binding.layoutSinAlumnos.visibility = View.VISIBLE
            binding.rvAlumnos.visibility = View.GONE
        } else {
            binding.layoutSinAlumnos.visibility = View.GONE
            binding.rvAlumnos.visibility = View.VISIBLE
        }
        adapter.actualizarLista(lista)
    }

    /**
     * Muestra la hoja inferior (BottomSheet) para registrar un nuevo alumno o editar uno existente.
     */
    private fun mostrarDialogoRegistro(alumno: Alumno?) {
        val sheetBinding = DialogAlumnoBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(sheetBinding.root)

        val esEdicion = alumno != null

        if (esEdicion) {
            sheetBinding.tvTituloDialogo.text = "Editar Alumno"
            sheetBinding.tvSubtituloDialogo.text = "Modifica los datos del alumno y guarda los cambios"
            sheetBinding.btnGuardar.text = "Actualizar Cambios"

            sheetBinding.etCuenta.setText(alumno.cuenta)
            sheetBinding.etCuenta.isEnabled = false // La cuenta es la llave primaria
            sheetBinding.etNombre.setText(alumno.nombre)
            sheetBinding.etCorreo.setText(alumno.correo)
            sheetBinding.etImagen.setText(alumno.image)
            actualizarVistaPreviaFoto(sheetBinding, alumno.image)
        } else {
            sheetBinding.tvTituloDialogo.text = "Registrar Nuevo Alumno"
            sheetBinding.tvSubtituloDialogo.text = "Ingresa los datos del nuevo estudiante para guardarlo en la BD"
            sheetBinding.btnGuardar.text = "Guardar Alumno"
        }

        // Listener en tiempo real para actualizar la foto de vista previa al escribir la URL
        sheetBinding.etImagen.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                actualizarVistaPreviaFoto(sheetBinding, s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Chips de avatares predeterminados para facilidad de uso
        sheetBinding.chipAvatar1.setOnClickListener {
            sheetBinding.etImagen.setText(AVATAR_1)
        }
        sheetBinding.chipAvatar2.setOnClickListener {
            sheetBinding.etImagen.setText(AVATAR_2)
        }
        sheetBinding.chipAvatar3.setOnClickListener {
            sheetBinding.etImagen.setText(AVATAR_3)
        }

        sheetBinding.btnCancelar.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        sheetBinding.btnGuardar.setOnClickListener {
            val cuenta = sheetBinding.etCuenta.text.toString().trim()
            val nombre = sheetBinding.etNombre.text.toString().trim()
            val correo = sheetBinding.etCorreo.text.toString().trim()
            val imagen = sheetBinding.etImagen.text.toString().trim()

            sheetBinding.tilCuenta.error = null
            sheetBinding.tilNombre.error = null

            if (cuenta.isEmpty()) {
                sheetBinding.tilCuenta.error = "Número de cuenta obligatorio"
                return@setOnClickListener
            }
            if (nombre.isEmpty()) {
                sheetBinding.tilNombre.error = "Nombre completo obligatorio"
                return@setOnClickListener
            }

            val alumnoModelo = Alumno(cuenta, nombre, correo, imagen)

            if (esEdicion) {
                val actualizado = dbHelper.actualizarAlumno(alumnoModelo)
                if (actualizado) {
                    Toast.makeText(this, "¡Alumno actualizado exitosamente!", Toast.LENGTH_SHORT).show()
                    cargarAlumnos()
                    bottomSheetDialog.dismiss()
                } else {
                    Toast.makeText(this, "Error al actualizar la información", Toast.LENGTH_SHORT).show()
                }
            } else {
                val insertado = dbHelper.agregarAlumno(alumnoModelo)
                if (insertado) {
                    Toast.makeText(this, "¡Alumno registrado con éxito!", Toast.LENGTH_SHORT).show()
                    cargarAlumnos()
                    bottomSheetDialog.dismiss()
                } else {
                    sheetBinding.tilCuenta.error = "Esta cuenta ya está registrada"
                    Toast.makeText(this, "La cuenta $cuenta ya existe", Toast.LENGTH_LONG).show()
                }
            }
        }

        bottomSheetDialog.show()
    }

    private fun actualizarVistaPreviaFoto(binding: DialogAlumnoBinding, url: String) {
        Glide.with(this)
            .load(if (url.isBlank()) R.drawable.ic_person_placeholder else url)
            .placeholder(R.drawable.ic_person_placeholder)
            .error(R.drawable.ic_person_placeholder)
            .into(binding.ivPreviewFoto)
    }

    private fun confirmarEliminacion(alumno: Alumno) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Alumno")
            .setMessage("¿Deseas eliminar a ${alumno.nombre} (${alumno.cuenta}) de la base de datos?")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                val eliminado = dbHelper.eliminarAlumno(alumno.cuenta)
                if (eliminado) {
                    Toast.makeText(this, "Alumno eliminado correctamente", Toast.LENGTH_SHORT).show()
                    cargarAlumnos()
                } else {
                    Toast.makeText(this, "No se pudo eliminar el registro", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
