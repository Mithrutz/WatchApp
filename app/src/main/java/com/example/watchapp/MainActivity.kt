package com.example.watchapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    // onCreate se apeleaza cand Android porneste ecranul
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Leaga acest ecran de fisierul res/layout/activity_main.xml
        setContentView(R.layout.activity_main)

        // Mod imersiv: ascunde bara de stare si bara de navigare.
        // Un swipe de la marginea ecranului le arata temporar, apoi dispar singure.
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}
