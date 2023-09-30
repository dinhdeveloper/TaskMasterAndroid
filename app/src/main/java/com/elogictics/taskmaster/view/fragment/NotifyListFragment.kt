package com.elogictics.taskmaster.view.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.elogictics.taskmaster.R
import com.elogictics.taskmaster.common.view.BaseFragment
import com.elogictics.taskmaster.databinding.FragmentNotiListBinding
import com.elogictics.taskmaster.model.LocationDetail
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.Locale


class NotifyListFragment : BaseFragment<FragmentNotiListBinding>(),OnMapReadyCallback {

    private var marker: Marker? = null
    private var googleMap: GoogleMap? = null
    private var locationManager: LocationManager? = null
    val REQUEST_PERMISSION_GPS = 4
    override val layoutResourceId: Int
        get() = R.layout.fragment_noti_list

    override fun onViewCreated() {
        setUpMapIfNeeded()
        localGPS()
    }

    private fun localGPS() {
        if (checkLocationPermission(activity)) {
            Log.d("DANG", "get  local GPS")
            locationManager =
                activity?.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager?
            locationManager?.requestLocationUpdates(
                android.location.LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                locationListener
            )
        }
    }

    private fun checkLocationPermission(activity: Activity?): Boolean {
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED &&
            context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (activity != null) {
                ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSION_GPS)
            }
            return false
        }
        return true
    }


    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            var mes =
                "Location changed: Lat: " + location.latitude.toString() + " Lng: " + location.longitude
            val longitude = "Longitude: " + location.longitude
            val latitude = "Latitude: " + location.latitude
            var cityName: String? = null
            val gcd = activity?.let { Geocoder(it.applicationContext, Locale.getDefault()) }
            val addresses: List<Address>
            try {
                addresses = location.latitude.let {
                    gcd?.getFromLocation(
                        it,
                        location.longitude,
                        1
                    )
                } as List<Address>

                Log.e("SSSSSSSSSSSSSS","$latitude")
                Log.e("SSSSSSSSSSSSSS","$longitude")
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val s = """$longitude$latitude My Current City is: $cityName""".trimIndent()
            Log.d("DANG", s)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String) {
            // Nothing
        }

        override fun onProviderDisabled(provider: String) {
            // Nothing
        }
    }

    private fun setUpMapIfNeeded() {
        if (googleMap == null) {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
            mapFragment?.getMapAsync(OnMapReadyCallback { googleMap ->
                    this.googleMap = googleMap
                    setUpMap()
                })
        }
    }
    private fun setUpMap() {
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap?.clear() // Xóa các marker cũ

        val locations = listOf(
            LocationDetail("Ngã tư hàng xanh", "Ngã tư hàng xanh đây", LatLng(10.8704617, 106.725351)),
            LocationDetail("20 Cộng Hòa", "His Talent : Plenty of money", LatLng(10.8030648, 106.6501163)),
            LocationDetail("Vị trí của tôi", "Vị trí của tôi", LatLng(10.8129724, 106.717002))
        )

        // Tạo một danh sách LatLngBounds để bao gồm tất cả các vị trí
        val builder = LatLngBounds.builder()

        for (location in locations) {
            marker = googleMap?.addMarker(
                MarkerOptions()
                    .position(location.latLng)
                    .title(location.title)
                    .snippet(location.description)
            )

            builder.include(location.latLng) // Thêm vị trí vào builder để tính toán zoom
            // Hiển thị thông tin chi tiết của marker
            marker?.showInfoWindow()
        }

        // Zoom đến toàn bộ các vị trí
        val bounds = builder.build()
        val padding = 100 // Padding cho việc zoom
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        googleMap?.animateCamera(cameraUpdate)
    }



    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }
}