package com.rapnap.panpar.adapter

import android.content.ContentValues.TAG
import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.rapnap.panpar.R
import com.rapnap.panpar.extensions.inflate
import com.rapnap.panpar.extensions.prettyString
import com.rapnap.panpar.extensions.prettyText
import com.rapnap.panpar.model.Abbinamento
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.Utente
import kotlinx.android.synthetic.main.paniere_item_row.view.*
import kotlinx.android.synthetic.main.recyclerview_item_row.view.contenuto_group
import kotlinx.android.synthetic.main.recyclerview_item_row.view.paniereLocation
import kotlinx.android.synthetic.main.recyclerview_item_row.view.paniereValue

class PanieriSinteticiAdapter(
    private var panieri: ArrayList<Paniere>,
    private val userId: String,
    private val tipologia: Utente.Tipologia,
    private val context: Context
) : RecyclerView.Adapter<PanieriSinteticiAdapter.PanieriHolder>() {

    private lateinit var inflatedView: View
    private val storage = Firebase.storage

    var abbinamenti = ArrayList<Abbinamento>()

    //Classe che estende la RecyclerView.ViewHolder, fa usare la ViewHolder all'adapter
    class PanieriHolder(val view: View) : RecyclerView.ViewHolder(view) //{}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PanieriSinteticiAdapter.PanieriHolder {
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
        var isMine = false

        if (tipologia == Utente.Tipologia.RICEVENTE) {
            abbinamenti.forEach {
                if (paniere.id == it.paniere) {
                    isMine = true
                    holder.view.codiceSegreto.text = it.codiceSegreto
                    holder.view.id_paniere.text = it.paniere
                }
            }

            when(isMine) {
                true ->
                    holder.view.setOnClickListener {
                        if ( holder.view.codiceView.visibility == View.VISIBLE ) {
                            TransitionManager.beginDelayedTransition(holder.view.paniere_sintetico_card, AutoTransition())
                            holder.view.codiceView.visibility = View.GONE
                        } else {
                            TransitionManager.beginDelayedTransition(holder.view.paniere_sintetico_card, AutoTransition())
                            holder.view.codiceView.visibility = View.VISIBLE
                        }
                    }
                false ->
                    holder.view.setOnClickListener {}
            }
        } else {
            holder.view.setOnClickListener {}
        }


        //Inserisco la posizione del paniere e dati sulla distanza
        holder.view.paniereLocation.text = paniere.puntoRitiro.nome
        holder.view.indirizzo.text = paniere.puntoRitiro.indirizzo

        //Svuoto il ViewGroup contenente i Chip contenuto (eventualmente riciclati) e
        // mostro quelli del paniere
        holder.view.contenuto_group.removeAllViews()
        paniere.contenuto.forEach {
            val chip = Chip(holder.view.context).apply {
                this.isCheckable = false
                this.isFocusableInTouchMode = false
                this.text = it.toString().toLowerCase().capitalize()
                holder.view.contenuto_group.addView(this)
            }
            Log.d("ADAPTER", "Contenuto: ${chip}")
        }

        holder.view.paniereValue.text = "Valore: ${paniere.calcolaValore()}"
        holder.view.stato.text = paniere.stato.prettyText()
        holder.view.stato.setTextColor(ContextCompat.getColor(context, R.color.secondaryColor))

        when (tipologia) {

            Utente.Tipologia.DONATORE -> {
                when (paniere.stato) {
                    Paniere.Stato.RITIRATO -> {
                        holder.view.data.text =
                            "Ritirato il ${paniere.dataRicezione?.prettyString()}"
                    }
                    Paniere.Stato.ASSEGNATO -> {
                        holder.view.data.text =
                            "Da consegnare (entro) il ${paniere.dataConsegnaPrevista?.prettyString()}"
                    }
                    else -> {
                        holder.view.data.text =
                            "Creato il ${paniere.dataInserimento?.prettyString()}"
                    }
                }
            }
            Utente.Tipologia.RICEVENTE -> {
                when (paniere.stato) {

                    Paniere.Stato.ASSEGNATO -> {
                        when (paniere.ricevente == userId) {
                            true -> {
                                holder.view.stato.text = "Vinto"
                                holder.view.stato.setTextColor(ContextCompat.getColor(context,R.color.successColor))
                            }
                            false -> {
                                holder.view.stato.text = "Perso"
                                holder.view.stato.setTextColor(ContextCompat.getColor(context, R.color.failureColor))
                            }
                        }
                        holder.view.data.text =
                            "Disponibile al ritiro dal ${paniere.dataConsegnaPrevista?.prettyString()}"
                    }
                    Paniere.Stato.IN_ATTESA_DI_MATCH -> {
                        holder.view.data.text =
                            "Disponibile al ritiro dal ${paniere.dataConsegnaPrevista?.prettyString()}"
                    }
                    Paniere.Stato.IN_GIACENZA, Paniere.Stato.ASSEGNATO -> {
                        holder.view.data.text =
                            "Da ritirare entro 2 giorni dal ${paniere.dataConsegnaPrevista?.prettyString()}"
                    }
                    else -> {
                        holder.view.data.text =
                            "Ritirato il ${paniere.dataRicezione?.prettyString()}"
                    }
                }
            }


        }
        Log.d("ADAPTER", "Valore: " + paniere.calcolaValore().toString())

    }

    fun setData(panieri: ArrayList<Paniere>) {

        Log.d(TAG, "MI SONO ARRIVATI: ${panieri.size} panieri!!! LOLXDLOL")
        this.panieri.clear()
        this.panieri.addAll(panieri)
        notifyDataSetChanged()
    }


}