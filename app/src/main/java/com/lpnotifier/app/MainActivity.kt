package com.lpnotifier.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var tvDestinationInfo: TextView
    private var selectedLocation: LatLng? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize destination info TextView
        tvDestinationInfo = findViewById(R.id.tv_destination_info)

        // Request location permissions
        if (!checkLocationPermissions()) {
            requestLocationPermissions()
        }

        // Initialize map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enable user's location on map
        if (checkLocationPermissions()) {
            mMap.isMyLocationEnabled = true
        }

        // Set map click listener to select destination
        mMap.setOnMapClickListener { latLng ->
            // Clear previous markers
            mMap.clear()

            // Add new marker
            mMap.addMarker(MarkerOptions().position(latLng).title("Destination"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))

            selectedLocation = latLng

            // Update destination info
            tvDestinationInfo.text = "Destination Set: ${latLng.latitude}, ${latLng.longitude}"

            // Start location tracking service
            val serviceIntent = Intent(this, LocationService::class.java).apply {
                putExtra("LATITUDE", latLng.latitude)
                putExtra("LONGITUDE", latLng.longitude)
            }
            startService(serviceIntent)

            Toast.makeText(this, "Destination Set", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable my location
                if (::mMap.isInitialized) {
                    mMap.isMyLocationEnabled = true
                }
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required to use the map",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}