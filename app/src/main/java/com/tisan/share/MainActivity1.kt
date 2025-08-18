package com.tisan.share
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kj.infinite.R

class MainActivity1 : ComponentActivity() {

    private lateinit var locationManager: LocationManager
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private val LOCATION_UPDATE_INTERVAL = 10000L // 5 seconds
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var satelliteCount: Int = 0

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (location != null) {
                // 如果获取到位置，打印经纬度
                logLocation(location)
            } else {
                // 如果没有获取到位置，打印错误信息
                logNoLocation()
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            satelliteCount = 0
            for (i in 0 until status.satelliteCount) {
                if (status.usedInFix(i)) {
                    satelliteCount++
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            LOCATION_UPDATE_INTERVAL,
            0f,
            locationListener,
            Looper.getMainLooper()
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationManager.registerGnssStatusCallback(gnssStatusCallback, handler)
        } else {
            locationManager.addGpsStatusListener { event ->
                val gpsStatus = locationManager.getGpsStatus(null)
                satelliteCount = gpsStatus?.satellites?.count { it.usedInFix() } ?: 0
            }
        }

        startLoggingLocation()
    }

    private fun startLoggingLocation() {
        runnable = object : Runnable {
            override fun run() {
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastKnownLocation != null) {
                    logLocation(lastKnownLocation)
                } else {
                    logNoLocation()
                }
                handler.postDelayed(this, LOCATION_UPDATE_INTERVAL)
            }
        }
        handler.post(runnable!!)
    }

    private fun logLocation(location: Location) {
        val message = "Lat: ${location.latitude}, Lon: ${location.longitude}, Satellites in use: $satelliteCount"
        Log.d("LocationUpdate", message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun logNoLocation() {
        val message = "Failed to get location, Satellites in use: $satelliteCount"
        Log.e("LocationUpdate", message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 移除位置更新和停止handler，防止内存泄漏
        locationManager.removeUpdates(locationListener)
        handler.removeCallbacks(runnable!!)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationManager.unregisterGnssStatusCallback(gnssStatusCallback)
        }
    }
}
