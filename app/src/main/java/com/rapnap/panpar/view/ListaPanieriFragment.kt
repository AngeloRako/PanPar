package com.rapnap.panpar.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.rapnap.panpar.R
import com.rapnap.panpar.adapter.RecyclerAdapter
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.viewmodel.ListaPanieriViewModel
import kotlinx.android.synthetic.main.fragment_lista_panieri.view.*

class ListaPanieriFragment: Fragment(R.layout.fragment_lista_panieri) {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter
    private var panieriList : ArrayList<Paniere> = ArrayList()
    private val listaPanieriVM : ListaPanieriViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lista_panieri, container, false)

        linearLayoutManager = LinearLayoutManager(this.activity)
        view.rec_view.layoutManager = linearLayoutManager

        adapter = RecyclerAdapter(panieriList)
        view.rec_view.adapter = adapter

        return view
    }

    override fun onStart() {
        super.onStart()
        //Parte chiamando la funzione che comunica con la repository per prendere i panieri
        //in base al punteggio dell'utente
        listaPanieriVM.getPanieriByScore() {

            Log.d("ACTIVITY", "Ho chiamato getPanieriByScore")

            //Osserva i panieri ottenuti
            listaPanieriVM.getListaPanieri().observe(this, Observer<ArrayList<Paniere>> {
                //Assegna a panieriList i panieri ottenuti, che verrà poi passato all'adapter
                panieriList = it
                Log.d("ACTIVITY", "Ho assegnato ad i panieri i valori che stavano nel DB." +
                        " La dimensione della lista dei panieri è: " + panieriList.size.toString())

                panieriList.forEach() {
                    //runOnUiThread {             //Per qualche motivo non funzione nel fragment
                    adapter.addNewItem(it)
                    adapter.notifyItemInserted((panieriList.size-1))    //Posizione in cui ho inserito, sempre alla fine
                    Log.d("ACTIVITY", "Ho " + adapter.itemCount.toString() + " panieri.")
                    //}
                }
            })
        }
    }

    //Questo aggiorna la view quando scrolli, ma se carichiamo tutti i panieri una volta e per tutte
    //non serve (magari potrebbe essere un miglioramento futuro, tipo caricarne prima 10 poi gli
    //altri man mano che scrolli)

//    private fun setRecyclerViewScrollListener() {
//        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                val totalItemCount = recyclerView.layoutManager!!.itemCount
//                richiediPaniere()
//            }
//        })
//    }
}