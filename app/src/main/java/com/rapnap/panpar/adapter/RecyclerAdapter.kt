package com.rapnap.panpar.adapter

import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.rapnap.panpar.R
import com.rapnap.panpar.extensions.inflate
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.repository.PaniereRepository
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*
import java.io.File


class RecyclerAdapter(private var panieri: ArrayList<Paniere>) : RecyclerView.Adapter<RecyclerAdapter.PanieriHolder>() {

    private lateinit var inflatedView : View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.PanieriHolder {
        Log.d("ADAPTER", "onCreateViewHolder")
        inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)

        //Prendo 1/6 dello schermo (il parent ha le dimensioni dello schermo)
        //per ogni riga = paniere da mostrare
        inflatedView.layoutParams.height = parent.measuredHeight/6;

        return PanieriHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return panieri.size
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.PanieriHolder, position: Int) {
        Log.d("ADAPTER", "Mmmm")
        val itemPaniere = panieri[position]
        holder.bindPaniere(itemPaniere)
    }

    fun addNewItem(newPaniere: Paniere){
        //Provare a passare la lista intera
        panieri.add(newPaniere)
    }

    //Classe che estende la RecyclerView.ViewHolder, fa usare la ViewHolder all'adapter
    class PanieriHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        //Rifermiento alla view inflated per accedere agli elementi
        private var view: View = v
        private var paniere: Paniere? = null
        private val paniereRepository : PaniereRepository = PaniereRepository()
        private var isEnlarged : Boolean = false

        //Va fatto fare alla repository
        private var storage = Firebase.storage

        //Inizializza il View.OnClickListener
        init {
            v.setOnClickListener(this)
        }

        //Implementa i metodi in View.OnClickListener
        override fun onClick(v: View) {
            Log.d("ADAPTER", "PANIERE!")

            if(!isEnlarged) {
                //Quando tappo su un paniere mostro il suo valore ed il pulsante SEGUI
                v.paniereValue.layoutParams.height = MATCH_PARENT
                v.layoutParams.height = view.layoutParams.height * 2

                v.followPaniere.alpha = 1.0f
                v.followPaniere.isEnabled = true

                v.requestLayout()
                isEnlarged = true
            } else {
                v.paniereValue.layoutParams.height = 0
                v.layoutParams.height = view.layoutParams.height / 2

                v.followPaniere.alpha = 0.0f
                v.followPaniere.isEnabled = false

                v.requestLayout()
                isEnlarged = false
            }
        }

        fun bindPaniere(paniere: Paniere) {
            this.paniere = paniere

            if (paniere.immagine != null) {
                val gsReference = storage.getReferenceFromUrl(paniere.immagine!!)
                val ONE_MEGABYTE: Long = 1024 * 1024    //Limite di conversione, aumentare
                gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                    val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                    view.paniereImg.setImageBitmap(bmp)
                }.addOnFailureListener {
                    Log.d("ADAPTER", "Sono un fallito e non so convertire i byteArray")
                }
            } else {
                val imgFile = File("drawable/empty_png.png")

                if (imgFile.exists()) {
                    val myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath())
                    view.paniereImg.setImageBitmap(myBitmap)
                }
            }

            //Inserisco la posizione del paniere
            view.paniereLocation.text = paniere.puntoRitiro.nome

            //Concateno il contenuto del paniere in una sola stringa
            //ed inserisco il contenuto del paniere
            var contentText : String = ""
            paniere.contenuto.forEach {
                contentText = contentText + it.toString() + ", "
            }
            contentText = contentText.substring(0, contentText.length - 2)
            Log.d("ADAPTER", "Contenuto: " + contentText)
            view.paniereContent.text = contentText

            view.paniereValue.text = "Valore: " + paniere.calcolaValore().toString()
            Log.d("ADAPTER", "Valore: " + paniere.calcolaValore().toString())

            view.followPaniere.alpha = 0.0f
            view.followPaniere.isEnabled = false

            view.followPaniere.setOnClickListener {
                //Bisogna prima ottenere i punti dell'utente
                paniereRepository.updatePaniereFollowers(id = paniere.id, punti = 1000)
            }

        }

//        Per associare una KEY al Paniere
//        companion object {
//            private val PANIERE_KEY = "PANIERE"
//        }
    }

}