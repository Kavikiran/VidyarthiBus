package com.kavikiran.vidyarthibus.watcher

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kavikiran.vidyarthibus.R
import com.kavikiran.vidyarthibus.model.AutoContact

class AlternativesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoContacts: TextView
    private lateinit var database: FirebaseDatabase

    private var routeId: String = ""
    private var routeName: String = ""
    private val contactList = mutableListOf<AutoContact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alternatives)

        // Get route details
        routeId = intent.getStringExtra("routeId") ?: ""
        routeName = intent.getStringExtra("routeName") ?: ""

        // Firebase instance
        database = FirebaseDatabase.getInstance()

        // Link UI elements
        recyclerView = findViewById(R.id.recyclerViewAutos)
        progressBar = findViewById(R.id.progressBar)
        tvNoContacts = findViewById(R.id.tvNoContacts)

        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load auto contacts
        loadAutoContacts()
    }

    private fun loadAutoContacts() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvNoContacts.visibility = View.GONE

        database.reference
            .child("alternatives")
            .child(routeId)
            .child("autos")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    contactList.clear()

                    for (autoSnap in snapshot.children) {
                        val name = autoSnap.child("name")
                            .getValue(String::class.java) ?: ""
                        val phone = autoSnap.child("phone")
                            .getValue(String::class.java) ?: ""
                        val area = autoSnap.child("area")
                            .getValue(String::class.java) ?: ""

                        contactList.add(AutoContact(name, phone, area))
                    }

                    progressBar.visibility = View.GONE

                    if (contactList.isEmpty()) {
                        tvNoContacts.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvNoContacts.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE

                        // Set adapter
                        recyclerView.adapter = AutoAdapter(contactList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@AlternativesActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}