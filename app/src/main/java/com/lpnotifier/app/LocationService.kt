package com.lpnotifier.app

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import com.google.android.gms.location.*

class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var destinationLatitude: Double = 0.0
    private var destinationLongitude: Double = 0.0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Extract destination coordinates from intent
        destinationLatitude = intent?.getDoubleExtra("LATITUDE", 0.0) ?: 0.0
        destinationLongitude = intent?.getDoubleExtra("LONGITUDE", 0.0) ?: 0.0

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupLocationTracking()
        return START_STICKY
    }

    private fun setupLocationTracking() {
        // Configure location request
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Create location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    checkProximityToDestination(location)
                }
            }
        }

        // Start location updates
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Handle permission issues
            stopSelf()
        }
    }

    private fun checkProximityToDestination(currentLocation: Location) {
        // Create destination location
        val destinationLocation = Location("").apply {
            latitude = destinationLatitude
            longitude = destinationLongitude
        }

        // Calculate distance
        val distance = currentLocation.distanceTo(destinationLocation)

        // Trigger notification when within 100 meters
        if (distance <= 100) {
            sendProximityNotification()
        }
    }

    private fun sendProximityNotification() {
        // Create and show notification
        val notificationHelper = ProximityNotificationHelper(this)
        notificationHelper.showNotification()

        // Stop service after notification to conserve battery
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}