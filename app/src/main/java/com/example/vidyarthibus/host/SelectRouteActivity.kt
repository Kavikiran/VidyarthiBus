package com.kavikiran.vidyarthibus.host

import android.content.Intent
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
import com.kavikiran.vidyarthibus.model.Route
import com.kavikiran.vidyarthibus.watcher.CrowdDashboardActivity

class SelectRouteActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoRoutes: TextView
    private lateinit var tvRoleBadge: TextView
    private lateinit var database: FirebaseDatabase
    private var userRole: String = "reporter"
    private val routeList = mutableListOf<Route>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_route)

        // Get role from previous screen
        userRole = intent.getStringExtra("role") ?: "reporter"

        // Firebase instance
        database = FirebaseDatabase.getInstance()

        // Link UI elements
        recyclerView = findViewById(R.id.recyclerViewRoutes)
        progressBar = findViewById(R.id.progressBar)
        tvNoRoutes = findViewById(R.id.tvNoRoutes)
        tvRoleBadge = findViewById(R.id.tvRoleBadge)

        // Show role badge
        tvRoleBadge.text = if (userRole == "reporter")
            "🚌 Reporter" else "⏳ Watcher"

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load routes from Firebase
        loadRoutes()
    }

    private fun loadRoutes() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvNoRoutes.visibility = View.GONE

        database.reference
            .child("routes")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    routeList.clear()

                    for (routeSnap in snapshot.children) {
                        val id = routeSnap.key ?: ""
                        val name = routeSnap.child("name")
                            .getValue(String::class.java) ?: ""
                        val stops = mutableListOf<String>()

                        for (stopSnap in routeSnap.child("stops").children) {
                            stopSnap.getValue(String::class.java)
                                ?.let { stops.add(it) }
                        }

                        routeList.add(Route(id, name, stops))
                    }

                    progressBar.visibility = View.GONE

                    if (routeList.isEmpty()) {
                        tvNoRoutes.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvNoRoutes.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE

                        // Set adapter
                        recyclerView.adapter = RouteAdapter(routeList) { route ->
                            onRouteSelected(route)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@SelectRouteActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun onRouteSelected(route: Route) {
        if (userRole == "reporter") {
            // Go to Report Crowd screen
            val intent = Intent(this, ReportCrowdActivity::class.java)
            intent.putExtra("routeId", route.id)
            intent.putExtra("routeName", route.name)
            intent.putExtra("role", userRole)
            startActivity(intent)
        } else {
            // Go to Crowd Dashboard screen
            val intent = Intent(this, CrowdDashboardActivity::class.java)
            intent.putExtra("routeId", route.id)
            intent.putExtra("routeName", route.name)
            intent.putExtra("role", userRole)
            startActivity(intent)
        }
    }
}