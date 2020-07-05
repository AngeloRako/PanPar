package com.rapnap.panpar.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.rapnap.panpar.R
import com.rapnap.panpar.extensions.inflate
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.repository.PaniereRepository
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*


class RecyclerAdapter(private var panieri: ArrayList<Paniere>) : RecyclerView.Adapter<RecyclerAdapter.PanieriHolder>() {

    private lateinit var inflatedView : View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.PanieriHolder {
        Log.d("ADAPTER", "onCreateViewHolder")
        inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)

        //Prendo 1/8 dello schermo (il parent ha le dimensioni dello schermo)
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
        panieri.add(newPaniere)
    }

    //1 - Make the class extend RecyclerView.ViewHolder, allowing the adapter
    //to use it as as a ViewHolder.
    class PanieriHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        //2 - Add a reference to the view you’ve inflated to allow the ViewHolder
        //to access the ImageView and TextView as an extension property.
        //Kotlin Android Extensions plugin adds hidden caching functions and fields
        // to prevent the constant querying of views.
        private var view: View = v
        private var paniere: Paniere? = null
        private val paniereRepository : PaniereRepository = PaniereRepository()
        private var isEnlarged : Boolean = false

        private var storage = Firebase.storage

        //3 - Initialize the View.OnClickListener
        init {
            v.setOnClickListener(this)
        }

        //4 - Implement the required method for View.OnClickListener since ViewHolders
        //are responsible for their own event handling.
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

            //Roba del tutorial che non so se ci servirà
            //val context = itemView.context
            //val showPaniereIntent = Intent(context, PaniereActivity::class.kt)
            //showPaniereIntent.putExtra(PANIERE_KEY, paniere)
            //context.startActivity(showPaniereIntent)
        }

        fun bindPaniere(paniere: Paniere) {
            this.paniere = paniere

            val gsReference = storage.getReferenceFromUrl(paniere.immagine)
            val ONE_MEGABYTE: Long = 1024 * 1024
            gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                view.paniereImg.setImageBitmap(bmp)
            }.addOnFailureListener {
                // Handle any errors
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
                paniereRepository.updatePaniereFollowers(id = paniere.id, punti = 20)
                Log.d("ENLARGEDVIEWS", isEnlarged.toString())
            }

            //view.paniereContent.text = paniere.contenuto.toString()
            //view.itemDate.text = photo.humanDate
            //view.itemDescription.text = photo.explanation
        }

        companion object {
            //5 - Add a key for easy reference to the item launching the RecyclerView.
            private val PANIERE_KEY = "PANIERE"
        }
    }

}