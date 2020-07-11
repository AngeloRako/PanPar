package com.rapnap.panpar.view

import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.rapnap.panpar.R

class HomeRiceventeActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.RiceventeTheme)
        setContentView(R.layout.activity_home_ricevente)

        negotiateDeviceDarkMode()

        //Imposto la toolbar dell'activity
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.ricevente_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(this, navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    private fun negotiateDeviceDarkMode() {
        val isNightMode = this.resources.configuration.uiMode
            .and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val typedValue = TypedValue()

        this.window.addFlags(
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )

        if (isNightMode) {
            this.theme
                .resolveAttribute(android.R.attr.colorBackground, typedValue, true)
            window.statusBarColor = typedValue.data
            window.navigationBarColor = typedValue.data
        }
    }


}