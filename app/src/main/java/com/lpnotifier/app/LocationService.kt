// File: LocationService.kt
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
        destinationLatitude = intent?.getDoubleExtra("LATITUDE", 0.0) ?: 0.0
        destinationLongitude = intent?.getDoubleExtra("LONGITUDE", 0.0) ?: 0.0

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupLocationTracking()
        return START_STICKY
    }

    private fun setupLocationTracking() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

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
        }
    }

    private fun checkProximityToDestination(currentLocation: Location) {
        val destinationLocation = Location("").apply {
            latitude = destinationLatitude
            longitude = destinationLongitude
        }

        val distance = currentLocation.distanceTo(destinationLocation)

        // Trigger notification when within 100 meters
        if (distance <= 100) {
            sendProximityNotification()
        }
    }

    private fun sendProximityNotification() {
        // Implement notification logic
        val notificationHelper = ProximityNotificationHelper(this)
        notificationHelper.showNotification()

        // Stop service after notification to conserve battery
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}