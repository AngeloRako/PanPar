package com.rapnap.panpar.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.rapnap.panpar.R
import kotlin.system.exitProcess

class HomeRiceventeActivity : AppCompatActivity() {

    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_ricevente)
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