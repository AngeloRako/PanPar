package com.rapnap.panpar.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import com.rapnap.panpar.R
import com.rapnap.panpar.model.Contenuto
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.PuntoRitiro
import com.rapnap.panpar.viewmodel.HomeDonatoreViewModel
import com.rapnap.panpar.viewmodel.SignInViewModel
import kotlinx.android.synthetic.main.activity_home_donatore.*
import kotlin.system.exitProcess

class HomeDonatoreActivity : AppCompatActivity() {

    private lateinit var hdvm: HomeDonatoreViewModel
    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_home_donatore)

        val vm: HomeDonatoreViewModel by viewModels()
        hdvm = vm

        donateBtn.setOnClickListener{

            val newPaniere = Paniere(puntoRitiro = PuntoRitiro(), contenuto = arrayListOf(Contenuto.ALTRO))

            hdvm.donate(newPaniere){
                textView.setText("FATTO, VEDI IL DB")
            }

        }


    }

    override fun onBackPressed() {

        if(backPressedTime + 2000 > System.currentTimeMillis()){
            super.onBackPressed()

        } else {
            Toast.makeText(applicationContext, "Premi indietro di nuovo per uscire", Toast.LENGTH_SHORT).show()
        }

        backPressedTime = System.currentTimeMillis()

    }
}