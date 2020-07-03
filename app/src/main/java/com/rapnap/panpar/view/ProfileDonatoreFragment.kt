package com.rapnap.panpar.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.rapnap.panpar.R
import com.rapnap.panpar.model.Contenuto
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.PuntoRitiro
import com.rapnap.panpar.viewmodel.ProfileDonatoreViewModel
import kotlinx.android.synthetic.main.fragment_profile_donatore.*


class ProfileDonatoreFragment : Fragment(R.layout.fragment_profile_donatore) { //<- Se metto questo evito il onCreateView

    private val pdvm: ProfileDonatoreViewModel by viewModels()
    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    activity?.finish()

                } else {
                    Toast.makeText(activity?.applicationContext, "Premi indietro di nuovo per uscire", Toast.LENGTH_SHORT).show()
                }

                backPressedTime = System.currentTimeMillis()

            }
        })

    }
/*
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_donatore, container, false)
        return view
    }
*/
    override fun onStart() {
        super.onStart()

        donateBtn.setOnClickListener{

            val newPaniere = Paniere(puntoRitiro = PuntoRitiro(), contenuto = arrayListOf(Contenuto.ALTRO))

            pdvm.donate(newPaniere){
                textView.setText("FATTO, VEDI IL DB")
            }

        }

        proceedBtn.setOnClickListener{

            Navigation.findNavController(this.requireView()).navigate(R.id.donatoreToSceltaPunto)

        }

}


}