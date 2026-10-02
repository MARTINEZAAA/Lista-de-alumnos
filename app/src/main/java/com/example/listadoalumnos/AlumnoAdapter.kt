package com.example.listadoalumnos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.listadoalumnos.databinding.ItemPersonaBinding

class AlumnoAdapter(
    private var listaAlumnos: List<Alumno>,
    private val onEditarClick: (Alumno) -> Unit,
    private val onEliminarClick: (Alumno) -> Unit
) : RecyclerView.Adapter<AlumnoAdapter.AlumnoViewHolder>() {

    inner class AlumnoViewHolder(val binding: ItemPersonaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlumnoViewHolder {
        val binding = ItemPersonaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlumnoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlumnoViewHolder, position: Int) {
        val alumno = listaAlumnos[position]
        with(holder.binding) {
            tvNombre.text = alumno.nombre
            tvCuenta.text = "Cuenta: ${alumno.cuenta}"
            tvCorreo.text = alumno.correo

            // Cargar la imagen del alumno usando la librería Glide
            Glide.with(ivFoto.context)
                .load(alumno.image)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(ivFoto)

            ibEditar.setOnClickListener {
                onEditarClick(alumno)
            }

            ibEliminar.setOnClickListener {
                onEliminarClick(alumno)
            }
        }
    }

    override fun getItemCount(): Int = listaAlumnos.size

    fun actualizarLista(nuevaLista: List<Alumno>) {
        this.listaAlumnos = nuevaLista
        notifyDataSetChanged()
    }
}
