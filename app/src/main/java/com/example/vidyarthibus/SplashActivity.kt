package com.kavikiran.vidyarthibus

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.kavikiran.vidyarthibus.auth.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Sign out any existing session
        // so user can always choose their role
        FirebaseAuth.getInstance().signOut()

        // Role selection buttons
        val btnReporter = findViewById<MaterialButton>(R.id.btnReporter)
        val btnWatcher = findViewById<MaterialButton>(R.id.btnWatcher)

        // Reporter button click
        btnReporter.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("role", "reporter")
            startActivity(intent)
        }

        // Watcher button click
        btnWatcher.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("role", "watcher")
            startActivity(intent)
        }
    }
}