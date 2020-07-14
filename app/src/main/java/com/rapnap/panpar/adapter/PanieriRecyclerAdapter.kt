package com.rapnap.panpar.adapter

import android.location.Location
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.rapnap.panpar.R
import com.rapnap.panpar.extensions.distanceText
import com.rapnap.panpar.extensions.inflate
import com.rapnap.panpar.model.Paniere
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*


class PanieriRecyclerAdapter(private var panieri: ArrayList<Paniere>, private var location: Location, private val onFollowListener: OnItemEventListener<Paniere>) : RecyclerView.Adapter<PanieriRecyclerAdapter.PaniereHolder>() {

    //Classe che estende la RecyclerView.ViewHolder, fa usare la ViewHolder all'adapter
    class PaniereHolder(val view: View) : RecyclerView.ViewHolder(view) //{}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaniereHolder {
        Log.d("ADAPTER", "onCreateViewHolder")
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)

        return PaniereHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return panieri.size
    }

    override fun onBindViewHolder(holder: PaniereHolder, position: Int) {
        Log.d("ADAPTER", "Mmmm")
        val paniere = panieri[position]

        holder.view.setOnClickListener{}

        //Inserisco la posizione del paniere e dati sulla distanza
        holder.view.paniereLocation.text = paniere.puntoRitiro.nome
        holder.view.paniereIndirizzo.text = paniere.puntoRitiro.indirizzo
        holder.view.paniereDistanza.text = distanceText(paniere.puntoRitiro.location.distanceTo(location))

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


        holder.view.followPaniere.setOnClickListener{
            //Segnalo al fragment che ho tappato "Follow" e che qualcosa dovrà succedere
            //Fornisco anche la view della cella, così posso aggiornare appropriatamente a seconda
            //di cosa succede
            onFollowListener.onEventHappened(paniere, it)
            //panieri.removeAt(position)
            //this.notifyItemRemoved(position)
        }
    }

    fun setData(panieri: ArrayList<Paniere>){
        this.panieri.clear()
        this.panieri.addAll(panieri)
        notifyDataSetChanged()
    }


}