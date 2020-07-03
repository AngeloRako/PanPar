package com.rapnap.panpar.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.rapnap.panpar.BuildConfig
import com.rapnap.panpar.R
import kotlinx.android.synthetic.main.activity_home_donatore.*

//Deprecated?
class NuovoPaniereActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuovo_paniere)

        //Imposto la toolbar dell'activity
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.new_paniere_host_fragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController
        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) //show back button

    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.new_paniere_host_fragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        return navController.navigateUp()
                || super.onSupportNavigateUp()
    }

    override fun finish() {
        super.finish()
        ActivityNavigator.applyPopAnimationsToPendingTransition(this)
    }


}