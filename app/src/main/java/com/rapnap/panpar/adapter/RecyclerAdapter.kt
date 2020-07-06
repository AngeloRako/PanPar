package com.rapnap.panpar.adapter

import android.graphics.BitmapFactory
import android.location.Location
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
import com.rapnap.panpar.model.distanceText
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*
import java.io.File


class RecyclerAdapter(private var panieri: ArrayList<Paniere>, private var location: Location, private val onFollowListener: OnItemEventListener<Paniere>) : RecyclerView.Adapter<RecyclerAdapter.PanieriHolder>() {

    private lateinit var inflatedView: View
    private val storage = Firebase.storage

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PanieriHolder {
        Log.d("ADAPTER", "onCreateViewHolder")
        inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)

        //Prendo 1/4 dello schermo (il parent ha le dimensioni dello schermo)
        //per ogni riga = paniere da mostrare
        //inflatedView.layoutParams.height = parent.measuredHeight/6;

        return PanieriHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return panieri.size
    }

    override fun onBindViewHolder(holder: PanieriHolder, position: Int) {
        Log.d("ADAPTER", "Mmmm")
        val paniere = panieri[position]

        if (paniere.immagine != null) {
            holder.view.paniereImg.layoutParams.height = 600
            val gsReference = storage.getReferenceFromUrl(paniere.immagine!!)
            val ONE_MEGABYTE: Long = 1024 * 1024    //Limite di conversione, aumentare
            gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener { it: ByteArray ->
                val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                holder.view.paniereImg.setImageBitmap(bmp)
            }.addOnFailureListener {
                Log.d("ADAPTER", "Sono un fallito e non so convertire i byteArray")
            }
        } else {
            holder.view.paniereImg.layoutParams.height = 0
            val imgFile = File("drawable/empty_png.png")

            if (imgFile.exists()) {
                val myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath())
                holder.view.paniereImg.setImageBitmap(myBitmap)
            }
        }

        //Inserisco la posizione del paniere
        holder.view.paniereLocation.text = paniere.puntoRitiro.nome
        holder.view.paniereIndirizzo.text = paniere.puntoRitiro.indirizzo
        holder.view.paniereDistanza.text = distanceText(paniere.puntoRitiro.location.distanceTo(location))


        //Concateno il contenuto del paniere in una sola stringa
        //ed inserisco il contenuto del paniere
        paniere.contenuto.forEach {
            val chip = Chip(holder.view.context).apply{
                this.isCheckable = false
                this.isFocusableInTouchMode = false
                this.text = it.toString().toLowerCase().capitalize()
                holder.view.contenuto_group.addView(this)
            }
            Log.d("ADAPTER", "Contenuto: $chip.toString()")
        }

        holder.view.paniereValue.text = "Valore: " + paniere.calcolaValore().toString()
        Log.d("ADAPTER", "Valore: " + paniere.calcolaValore().toString())


        holder.view.followPaniere.setOnClickListener{
            onFollowListener.onEventHappened(paniere)
        }
    }

    fun addNewItem(newPaniere: Paniere) {
        //Provare a passare la lista intera
        panieri.add(newPaniere)
    }

    //Classe che estende la RecyclerView.ViewHolder, fa usare la ViewHolder all'adapter
    class PanieriHolder(val view: View) : RecyclerView.ViewHolder(view) {


    }
}