package com.rapnap.panpar.view

import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.rapnap.panpar.R
import com.rapnap.panpar.model.PuntoRitiro
import com.rapnap.panpar.model.toLocation
import com.rapnap.panpar.viewmodel.NuovoPaniereViewModel
import kotlinx.android.synthetic.main.fragment_punti_ritiro_list.view.*


class PuntiRitiroListFragment : Fragment(), PuntiRitiroListAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: PuntiRitiroListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val nuovoPaniereVM: NuovoPaniereViewModel
            by navGraphViewModels(R.id.new_paniere_graph)

    //TODO: Da ottenere da fuori (Data Injection?)
    private var currentLocation = LatLng(40.643396, 14.865041)
    private var maxDistance = 500000000000.0

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            // Inflate the layout for this fragment
            val view = inflater.inflate(R.layout.fragment_punti_ritiro_list, container, false)

            viewManager = LinearLayoutManager(this.activity)
            recyclerView = view.punti_ritiro_recycler_view.apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                // use a linear layout manager
                layoutManager = viewManager

            }

            //Location utente (La posso chiedere IRT oppure al VewModel..)
            var loc = currentLocation.toLocation()


            nuovoPaniereVM.getPuntiDiRitiro(loc, maxDistance){}

            nuovoPaniereVM.puntiRitiro.observe(viewLifecycleOwner, Observer<List<PuntoRitiro>> {

                viewAdapter = PuntiRitiroListAdapter(it, loc, this)
                recyclerView.adapter = viewAdapter

            })

            return view
        }


    /*  Elementi di Menu nella ActionBar    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.punti_ritiro_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.toggle_map ->{

            Navigation.findNavController(requireView()).popBackStack()

        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    //Cosa succede quando clicco su una cella?

    override fun onItemClick(punto: PuntoRitiro) {
        nuovoPaniereVM.setPuntoRitiroSelezionato(punto)
        Navigation.findNavController(requireView()).popBackStack()
    }


}