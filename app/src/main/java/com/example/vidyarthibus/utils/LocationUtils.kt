package com.kavikiran.vidyarthibus.utils

import kotlin.math.*

object LocationUtils {

    // Maximum distance allowed from a bus stop (in meters)
    private const val THRESHOLD_METERS = 500.0

    /**
     * Checks if the user is within 500 meters
     * of any stop on the selected route
     */
    fun isNearRoute(
        userLat: Double,
        userLng: Double,
        stopCoords: List<Pair<Double, Double>>
    ): Boolean {
        for (stop in stopCoords) {
            val distance = haversine(
                userLat, userLng,
                stop.first, stop.second
            )
            if (distance <= THRESHOLD_METERS) return true
        }
        return false
    }

    /**
     * Haversine formula — calculates distance
     * between two GPS coordinates in meters
     */
    fun haversine(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    /**
     * Checks if a crowd report has expired
     * (older than 15 minutes)
     */
    fun isReportExpired(expiresAt: Long): Boolean {
        return System.currentTimeMillis() > expiresAt
    }

    /**
     * Creates an expiry timestamp
     * 15 minutes from now
     */
    fun getExpiryTime(): Long {
        return System.currentTimeMillis() + (15 * 60 * 1000)
    }

    /**
     * Formats the time difference into
     * a readable string like "2 mins ago"
     */
    fun getTimeAgo(reportedAt: Long): String {
        val diff = System.currentTimeMillis() - reportedAt
        val minutes = diff / (60 * 1000)
        return when {
            minutes < 1  -> "Just now"
            minutes == 1L -> "1 min ago"
            minutes < 15 -> "$minutes mins ago"
            else         -> "Expired"
        }
    }
}