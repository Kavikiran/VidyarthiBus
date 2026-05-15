package com.kavikiran.vidyarthibus.watcher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kavikiran.vidyarthibus.R
import com.kavikiran.vidyarthibus.model.CrowdReport
import com.kavikiran.vidyarthibus.utils.LocationUtils

class CrowdDashboardActivity : AppCompatActivity() {

    private lateinit var tvRouteName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvReporterInfo: TextView
    private lateinit var tvTimeInfo: TextView
    private lateinit var crowdProgressBar: ProgressBar
    private lateinit var progressBar: ProgressBar
    private lateinit var btnAlternatives: MaterialButton
    private lateinit var database: FirebaseDatabase

    private var routeId: String = ""
    private var routeName: String = ""
    private var crowdListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crowd_dashboard)

        // Get route details
        routeId = intent.getStringExtra("routeId") ?: ""
        routeName = intent.getStringExtra("routeName") ?: ""

        // Firebase instance
        database = FirebaseDatabase.getInstance()

        // Link UI elements
        tvRouteName = findViewById(R.id.tvRouteName)
        tvStatus = findViewById(R.id.tvStatus)
        tvReporterInfo = findViewById(R.id.tvReporterInfo)
        tvTimeInfo = findViewById(R.id.tvTimeInfo)
        crowdProgressBar = findViewById(R.id.crowdProgressBar)
        progressBar = findViewById(R.id.progressBar)
        btnAlternatives = findViewById(R.id.btnAlternatives)

        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Set route name
        tvRouteName.text = routeName

        // Alternatives button
        btnAlternatives.setOnClickListener {
            val intent = Intent(this, AlternativesActivity::class.java)
            intent.putExtra("routeId", routeId)
            intent.putExtra("routeName", routeName)
            startActivity(intent)
        }

        // Start listening to crowd status
        listenToCrowdStatus()
    }

    private fun listenToCrowdStatus() {
        progressBar.visibility = View.VISIBLE

        crowdListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                progressBar.visibility = View.GONE

                if (!snapshot.exists()) {
                    showNoData()
                    return
                }

                val report = snapshot.getValue(CrowdReport::class.java)

                if (report == null) {
                    showNoData()
                    return
                }

                // Check if report is expired
                if (LocationUtils.isReportExpired(report.expiresAt)) {
                    showExpiredData()
                    return
                }

                // Show crowd status
                updateCrowdUI(report)
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                showNoData()
            }
        }

        // Attach listener to Firebase
        database.reference
            .child("routes")
            .child(routeId)
            .child("crowdStatus")
            .addValueEventListener(crowdListener!!)
    }

    private fun updateCrowdUI(report: CrowdReport) {
        when (report.status) {
            "EMPTY" -> {
                tvStatus.text = "🟢 EMPTY"
                tvStatus.setTextColor(
                    Color.parseColor("#4CAF50")
                )
                crowdProgressBar.progress = 20
                crowdProgressBar.progressTintList =
                    android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#4CAF50"))
            }
            "SEATED" -> {
                tvStatus.text = "🟡 SEATED"
                tvStatus.setTextColor(
                    Color.parseColor("#FFC107")
                )
                crowdProgressBar.progress = 60
                crowdProgressBar.progressTintList =
                    android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#FFC107"))
            }
            "FULL" -> {
                tvStatus.text = "🔴 FULL"
                tvStatus.setTextColor(
                    Color.parseColor("#F44336")
                )
                crowdProgressBar.progress = 100
                crowdProgressBar.progressTintList =
                    android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#F44336"))
            }
        }

        // Reporter info
        tvReporterInfo.text =
            "Reported by: ${report.reporterName}"

        // Time info
        tvTimeInfo.text =
            "Updated: ${LocationUtils.getTimeAgo(report.reportedAt)}"
    }

    private fun showNoData() {
        tvStatus.text = "No Data"
        tvStatus.setTextColor(Color.parseColor("#757575"))
        crowdProgressBar.progress = 0
        tvReporterInfo.text = "No reports yet for this route"
        tvTimeInfo.text = ""
    }

    private fun showExpiredData() {
        tvStatus.text = "Expired"
        tvStatus.setTextColor(Color.parseColor("#757575"))
        crowdProgressBar.progress = 0
        tvReporterInfo.text = "Last report has expired"
        tvTimeInfo.text = "Report more than 15 mins old"
    }

    // Remove Firebase listener when screen closes
    override fun onDestroy() {
        super.onDestroy()
        crowdListener?.let {
            database.reference
                .child("routes")
                .child(routeId)
                .child("crowdStatus")
                .removeEventListener(it)
        }
    }
}