package com.rapnap.panpar.adapter

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.rapnap.panpar.R
import com.rapnap.panpar.extensions.inflate
import com.rapnap.panpar.model.Paniere
import kotlinx.android.synthetic.main.paniere_item_row.view.*
import kotlinx.android.synthetic.main.recyclerview_item_row.view.contenuto_group
import kotlinx.android.synthetic.main.recyclerview_item_row.view.paniereLocation
import kotlinx.android.synthetic.main.recyclerview_item_row.view.paniereValue

class PanieriSinteticiAdapter (private var panieri: ArrayList<Paniere>): RecyclerView.Adapter<PanieriSinteticiAdapter.PanieriHolder>() {

    private lateinit var inflatedView: View
    private val storage = Firebase.storage

    //Classe che estende la RecyclerView.ViewHolder, fa usare la ViewHolder all'adapter
    class PanieriHolder(val view: View) : RecyclerView.ViewHolder(view) //{}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PanieriSinteticiAdapter.PanieriHolder {
        Log.d("ADAPTER", "onCreateViewHolder")
        inflatedView = parent.inflate(R.layout.paniere_item_row, false)

        return PanieriHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return panieri.size
    }

    override fun onBindViewHolder(holder: PanieriHolder, position: Int) {
        Log.d("ADAPTER", "Mmmm")
        val paniere = panieri[position]

        //Inserisco la posizione del paniere e dati sulla distanza
        holder.view.paniereLocation.text = paniere.puntoRitiro.nome
        holder.view.indirizzo.text = paniere.puntoRitiro.indirizzo

        //Svuoto il ViewGroup contenente i Chip contenuto (eventualmente riciclati) e
        // mostro quelli del paniere
        holder.view.contenuto_group.removeAllViews()
        paniere.contenuto.forEach {
            val chip = Chip(holder.view.context).apply{
                this.isCheckable = false
                this.isFocusableInTouchMode = false
                this.text = it.toString().toLowerCase().capitalize()
                holder.view.contenuto_group.addView(this)
            }
            Log.d("ADAPTER", "Contenuto: ${chip}")
        }

        holder.view.paniereValue.text = "Valore: ${paniere.calcolaValore()}"
        Log.d("ADAPTER", "Valore: " + paniere.calcolaValore().toString())

    }



    fun addNewItem(newPaniere: Paniere) {
        //Provare a passare la lista intera
        panieri.add(newPaniere)
    }


}