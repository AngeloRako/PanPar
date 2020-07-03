package com.rapnap.panpar.adapter

import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rapnap.panpar.R
import com.rapnap.panpar.extensions.inflate
import com.rapnap.panpar.model.Paniere
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*

class RecyclerAdapter(private var panieri: ArrayList<Paniere>) : RecyclerView.Adapter<RecyclerAdapter.PanieriHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.PanieriHolder {
        Log.d("ADAPTER", "onCreateViewHolder")
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
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

        //3 - Initialize the View.OnClickListener
        init {
            v.setOnClickListener(this)
        }

        //4 - Implement the required method for View.OnClickListener since ViewHolders
        //are responsible for their own event handling.
        override fun onClick(v: View) {
            Log.d("ADAPTER", "PANIERE!")

            //val context = itemView.context
            //val showPaniereIntent = Intent(context, PaniereActivity::class.kt)
            //showPaniereIntent.putExtra(PANIERE_KEY, paniere)
            //context.startActivity(showPaniereIntent)
        }

        fun bindPaniere(paniere: Paniere) {
            this.paniere = paniere
            view.paniereLocation.text = paniere.puntoRitiro.nome.toString()

            Log.d("ADAPTER", paniere.puntoRitiro.nome.toString())

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