package com.example.destinationalert.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.destinationalert.MainActivity
import com.example.destinationalert.R
import com.example.destinationalert.data.local.entities.Trip
import com.example.destinationalert.data.repository.TripRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var repository: TripRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeTrip: Trip? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    checkDistance(location)
                }
            }
        }
        
        startObservingTrip()
    }

    private fun startObservingTrip() {
        serviceScope.launch {
            repository.getActiveTrip().collectLatest { trip ->
                activeTrip = trip
                if (trip == null || !trip.isActive) {
                    stopSelf() // Stop service if no active trip
                } else {
                    // Update notification content?
                }
            }
        }
    }

    @SuppressLint("MissingPermission") // Checked before starting service
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Handle permission loss
            stopSelf()
        }
    }

    private fun checkDistance(currentLocation: Location) {
        val trip = activeTrip ?: return
        
        val destinationLocation = Location("destination").apply {
            latitude = trip.destinationLat
            longitude = trip.destinationLng
        }
        
        val distanceMeters = currentLocation.distanceTo(destinationLocation)
        
        android.util.Log.d("LocationTracker", "Current Lat/Lng: ${currentLocation.latitude}, ${currentLocation.longitude}")
        android.util.Log.d("LocationTracker", "Target: ${trip.destinationName} (${trip.destinationLat}, ${trip.destinationLng})")
        android.util.Log.d("LocationTracker", "DISTANCE: ${distanceMeters.toInt()} meters (Alert Radius: ${trip.alertRadiusMeters}m)")

        // Ensure to update notification with distance? (Optional)
        updateNotification("Distance: ${distanceMeters.toInt()}m")

        if (distanceMeters <= trip.alertRadiusMeters) {
            android.util.Log.d("LocationTracker", "ALARM TRIGGERED! Distance $distanceMeters < Radius ${trip.alertRadiusMeters}")
            triggerArrivalAlert(trip.destinationName)
            completeTrip()
        }
    }

    private fun completeTrip() {
        serviceScope.launch {
            repository.completeTrip()
        }
        // stopSelf will be called by observer
    }

    private fun triggerArrivalAlert(destinationName: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "destination_alert_channel")
            .setContentTitle("Arriving at $destinationName!")
            .setContentText("You are within range of your destination.")
            .setSmallIcon(R.drawable.ic_launcher) // Ensure this exists or use system icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(2, notification)
    }

    private fun updateNotification(contentText: String) {
         val notification = createNotification(contentText)
         val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
         notificationManager.notify(1, notification)
    }

    private fun createNotification(contentText: String): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "location_tracking_channel")
            .setContentTitle("Tracking Trip")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) 
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification("Initializing..."))
        startLocationUpdates()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }
}
