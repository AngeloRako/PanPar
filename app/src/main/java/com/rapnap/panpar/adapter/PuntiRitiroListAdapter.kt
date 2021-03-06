package com.rapnap.panpar.adapter

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rapnap.panpar.R
import com.rapnap.panpar.extensions.distanceText
import com.rapnap.panpar.model.PuntoRitiro
import kotlinx.android.synthetic.main.punto_ritiro_item.view.*

class PuntiRitiroListAdapter(private var punti: ArrayList<PuntoRitiro>, private val location: Location, private val listener: OnItemEventListener<PuntoRitiro>) :

    RecyclerView.Adapter<PuntiRitiroListAdapter.PuntoRitiroHolder>() {

    class PuntoRitiroHolder(val view: View) : RecyclerView.ViewHolder(view) //{}

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PuntoRitiroHolder {
            // create a new view
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.punto_ritiro_item, parent, false)

            return PuntoRitiroHolder(
                view
            )
        }

        override fun onBindViewHolder(holder: PuntoRitiroHolder, position: Int) {

            val punto = punti.get(position)

            holder.view.name_text_view.text = punto.nome
            holder.view.address_text_view.text = punto.indirizzo
            holder.view.distance_text_view.text = distanceText(punto.location.distanceTo(location))

            holder.itemView.setOnClickListener{
                listener.onEventHappened(punto)
            }

        }

        override fun getItemCount() = punti.size

        fun setPunti(puntiRitiro: ArrayList<PuntoRitiro>) {
            this.punti = puntiRitiro
            notifyDataSetChanged()
        }
    }


