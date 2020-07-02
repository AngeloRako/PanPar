package com.rapnap.panpar.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.rapnap.panpar.R

class HomeDonatoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(R.layout.activity_home_donatore)

        //Imposto la toolbar dell'activity
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.donatore_fragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController
        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)


    }
}