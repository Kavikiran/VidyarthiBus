package com.kavikiran.vidyarthibus.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kavikiran.vidyarthibus.R
import com.kavikiran.vidyarthibus.host.SelectRouteActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var etName: TextInputEditText
    private lateinit var etCollege: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvLogin: TextView
    private var userRole: String = "reporter"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Get role from previous screen
        userRole = intent.getStringExtra("role") ?: "reporter"

        // Firebase instances
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Link UI elements
        etName = findViewById(R.id.etName)
        etCollege = findViewById(R.id.etCollege)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressBar = findViewById(R.id.progressBar)
        tvLogin = findViewById(R.id.tvLogin)

        // Register button click
        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val college = etCollege.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validate inputs
            if (name.isEmpty()) {
                etName.error = "Please enter your name"
                return@setOnClickListener
            }
            if (college.isEmpty()) {
                etCollege.error = "Please enter your college name"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                etEmail.error = "Please enter your email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Please enter a password"
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            registerUser(name, college, email, password)
        }

        // Go back to Login screen
        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(
        name: String,
        college: String,
        email: String,
        password: String
    ) {
        // Show loading
        progressBar.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""

                    // Save user details to Firebase Database
                    val userMap = mapOf(
                        "name" to name,
                        "college" to college,
                        "email" to email,
                        "role" to userRole
                    )

                    database.reference
                        .child("users")
                        .child(userId)
                        .setValue(userMap)
                        .addOnCompleteListener { dbTask ->
                            progressBar.visibility = View.GONE
                            btnRegister.isEnabled = true

                            if (dbTask.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Account Created Successfully! 🎉",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Go to Select Route screen
                                val intent = Intent(
                                    this,
                                    SelectRouteActivity::class.java
                                )
                                intent.putExtra("role", userRole)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to save user data: ${dbTask.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true
                    Toast.makeText(
                        this,
                        "Registration Failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}