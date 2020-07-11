package com.rapnap.panpar.view

import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.rapnap.panpar.R

//Deprecated?
class NuovoPaniereActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.DonatoreTheme)
        setContentView(R.layout.activity_nuovo_paniere)
        negotiateDeviceDarkMode()

        //Imposto la toolbar dell'activity
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.new_paniere_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupActionBarWithNavController(this, navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun finish() {
        super.finish()
        ActivityNavigator.applyPopAnimationsToPendingTransition(this)
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