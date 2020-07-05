package com.rapnap.panpar.view

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rapnap.panpar.R
import com.rapnap.panpar.model.PuntoRitiro
import com.rapnap.panpar.model.distanceText
import com.rapnap.panpar.viewmodel.NuovoPaniereViewModel

class PuntiRitiroListAdapter(private var punti: List<PuntoRitiro>, private val location: Location, private val listener: OnItemClickListener) :

    RecyclerView.Adapter<PuntiRitiroListAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view){

        val nameTextView: TextView = view.findViewById(R.id.name_text_view)
        val addressTextView: TextView = view.findViewById(R.id.address_text_view)
        val distanceTextView: TextView = view.findViewById(R.id.distance_text_view)

    }

    /* Interfaccia per gestire gli OnClick sulla cella   */
    interface OnItemClickListener {
        fun onItemClick(punto: PuntoRitiro)
    }

    // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PuntiRitiroListAdapter.ViewHolder {
            // create a new view
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.punto_ritiro_item, parent, false)

            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val punto = punti.get(position)

            holder.nameTextView.text = punto.nome
            holder.addressTextView.text = punto.indirizzo
            holder.distanceTextView.text = distanceText(punto.location.distanceTo(location))

            holder.itemView.setOnClickListener{
                listener.onItemClick(punto)
            }

        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = punti.size

        fun setPunti(puntiRitiro: List<PuntoRitiro>) {
            this.punti = puntiRitiro
            notifyDataSetChanged()
        }
    }


