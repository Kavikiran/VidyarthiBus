package com.kavikiran.vidyarthibus.host

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kavikiran.vidyarthibus.R
import com.kavikiran.vidyarthibus.model.CrowdReport
import com.kavikiran.vidyarthibus.utils.LocationUtils

class ReportCrowdActivity : AppCompatActivity() {

    private lateinit var btnEmpty: MaterialButton
    private lateinit var btnSeated: MaterialButton
    private lateinit var btnFull: MaterialButton
    private lateinit var tvRouteName: TextView
    private lateinit var tvLocationCheck: TextView
    private lateinit var tvLastReport: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private var routeId: String = ""
    private var routeName: String = ""

    // Sample stop coordinates for testing
    // In production these would come from Firebase
    private val sampleStopCoords = listOf(
        Pair(13.3409, 74.7421), // Udupi
        Pair(13.3528, 74.7874), // Manipal
        Pair(13.3470, 74.7981)  // MIT College
    )

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_crowd)

        // Get route details from intent
        routeId = intent.getStringExtra("routeId") ?: ""
        routeName = intent.getStringExtra("routeName") ?: ""

        // Firebase instances
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        // Location client
        fusedLocationClient = LocationServices
            .getFusedLocationProviderClient(this)

        // Link UI elements
        btnEmpty = findViewById(R.id.btnEmpty)
        btnSeated = findViewById(R.id.btnSeated)
        btnFull = findViewById(R.id.btnFull)
        tvRouteName = findViewById(R.id.tvRouteName)
        tvLocationCheck = findViewById(R.id.tvLocationCheck)
        tvLastReport = findViewById(R.id.tvLastReport)
        progressBar = findViewById(R.id.progressBar)

        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Set route name
        tvRouteName.text = routeName

        // Load last report
        loadLastReport()

        // Button clicks
        btnEmpty.setOnClickListener { checkLocationAndReport("EMPTY") }
        btnSeated.setOnClickListener { checkLocationAndReport("SEATED") }
        btnFull.setOnClickListener { checkLocationAndReport("FULL") }
    }

    private fun checkLocationAndReport(status: String) {
        // Show location checking message
        tvLocationCheck.visibility = View.VISIBLE
        tvLocationCheck.text = "📍 Checking your location..."
        setButtonsEnabled(false)

        // Check location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        // Get current location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location == null) {
                    // Location unavailable — allow report with warning
                    tvLocationCheck.text =
                        "⚠️ Location unavailable — submitting anyway"
                    submitReport(status)
                    return@addOnSuccessListener
                }

                // Check if near route
                val isNear = LocationUtils.isNearRoute(
                    location.latitude,
                    location.longitude,
                    sampleStopCoords
                )

                if (isNear) {
                    tvLocationCheck.text = "📍 Location verified ✅"
                    submitReport(status)
                } else {
                    tvLocationCheck.text =
                        "❌ You are not near this bus route!"
                    setButtonsEnabled(true)
                    Toast.makeText(
                        this,
                        "You must be within 500m of a bus stop to report",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener {
                tvLocationCheck.text = "⚠️ Location check failed"
                setButtonsEnabled(true)
            }
    }

    private fun submitReport(status: String) {
        progressBar.visibility = View.VISIBLE

        val userId = auth.currentUser?.uid ?: ""
        val userName = auth.currentUser?.email?.substringBefore("@") ?: "Student"
        val now = System.currentTimeMillis()
        val expiresAt = LocationUtils.getExpiryTime()

        val report = CrowdReport(
            status = status,
            reportedBy = userId,
            reporterName = userName,
            reportedAt = now,
            expiresAt = expiresAt
        )

        database.reference
            .child("routes")
            .child(routeId)
            .child("crowdStatus")
            .setValue(report)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                setButtonsEnabled(true)

                if (task.isSuccessful) {
                    val emoji = when (status) {
                        "EMPTY" -> "🟢"
                        "SEATED" -> "🟡"
                        else -> "🔴"
                    }
                    Toast.makeText(
                        this,
                        "$emoji Report submitted! Expires in 15 mins",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Failed to submit: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun loadLastReport() {
        database.reference
            .child("routes")
            .child(routeId)
            .child("crowdStatus")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val report = snapshot.getValue(CrowdReport::class.java)
                    if (report != null) {
                        if (LocationUtils.isReportExpired(report.expiresAt)) {
                            tvLastReport.text = "No active reports for this route"
                        } else {
                            val timeAgo = LocationUtils
                                .getTimeAgo(report.reportedAt)
                            tvLastReport.text =
                                "Last report: ${report.status} — $timeAgo"
                        }
                    }
                }
            }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        btnEmpty.isEnabled = enabled
        btnSeated.isEnabled = enabled
        btnFull.isEnabled = enabled
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    this,
                    "Permission granted! Tap a button to report",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}