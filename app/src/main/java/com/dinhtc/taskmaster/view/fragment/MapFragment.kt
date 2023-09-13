package com.dinhtc.taskmaster.view.fragment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.databinding.FragmentMapsBinding
import com.dinhtc.taskmaster.model.response.CollectPointLatLng
import com.dinhtc.taskmaster.model.response.JobDetailsResponse
import com.dinhtc.taskmaster.model.response.ListCollectPointLatLng
import com.dinhtc.taskmaster.service.LocationUpdateService
import com.dinhtc.taskmaster.utils.AndroidUtils
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.observe
import com.dinhtc.taskmaster.viewmodel.JobsViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.maps.android.ui.IconGenerator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapsBinding>(), OnMapReadyCallback {

    private var dataResponse: List<CollectPointLatLng>? = null
    private var intent: Intent? = null
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location
    private val markers = ArrayList<MarkerOptions>()

    private val jobsViewModel: JobsViewModel by viewModels()

    override val layoutResourceId: Int
        get() = R.layout.fragment_maps

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let {
                currentLocation = it
                val latLng = LatLng(it.latitude, it.longitude)
                Log.e("SSSSSSSSSSSS","${latLng.latitude}")
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(latLng).title("My Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (intent != null){
            activity?.stopService(intent)
        }
    }
    override fun onPause() {
        super.onPause()
        intent = Intent(activity, LocationUpdateService::class.java)
        activity?.startService(intent)
    }

    override fun onViewCreated() {
        jobsViewModel.getCollectPointLatLng()
        observe(jobsViewModel.getCollectPointLatLng, ::getCollectPointLatLng)
    }

    private fun getCollectPointLatLng(uiState: UiState<ListCollectPointLatLng>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
                mapFragment.getMapAsync(this)
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

                dataResponse = uiState.data.data.listItem
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e("SSSSSSSSSSS", errorMessage)
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (intent != null){
            val locationRequest = LocationRequest.create().apply {
                interval = 1000 // Cập nhật vị trí mỗi giây
                fastestInterval = 500 // Tần số cập nhật nhanh nhất
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }else{
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentLocation = location
                        val latLng = LatLng(location.latitude, location.longitude)
                        addMarker(latLng)
//                        mMap.addMarker(MarkerOptions().position(latLng).title("Vị trí của tôi"))
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                    }
                }
            } else {
                // Nếu quyền chưa được cấp, bạn có thể yêu cầu từ người dùng
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }

    }

    private fun addMarker(latLng: LatLng) {

      val item =  context?.let {
          convertLayoutToBitmap(it,R.layout.custom_layout_map)
      }
        // Thêm Marker 1
        if (dataResponse?.isNotEmpty() == true){
            for (data in dataResponse!!){
                val marker = MarkerOptions()
                    .position(LatLng(data.latitude.toDouble(),data.longitude.toDouble()))
                    .icon(item?.let { BitmapDescriptorFactory.fromBitmap(it) })
                markers.add(marker)
            }
        }

        val markerMyLocation = MarkerOptions()
            .position(latLng)
            .icon(item?.let { BitmapDescriptorFactory.fromBitmap(it) })
        markers.add(markerMyLocation)

        for (markerOption in markers) {
            val marker = mMap.addMarker(markerOption)
            marker?.showInfoWindow()
        }

        // Tính toán giới hạn của tất cả các marker trong danh sách markers
        val builder = LatLngBounds.Builder()
        for (markerOption in markers) {
            builder.include(markerOption.position)
            // Đặt title cho marker
            mMap.addMarker(markerOption)
        }

        // Tạo một LatLngBounds object
        val bounds = builder.build()

        // Tạo một CameraUpdate để zoom đến giới hạn của tất cả các marker
        val padding = 200 // Khoảng cách giữa biên của Map và các marker (px)
        val cameraUpdateBounds = CameraUpdateFactory.newLatLngBounds(bounds, padding)

        val cameraUpdateZoom = CameraUpdateFactory.newLatLngZoom(latLng, 11f)

        // Áp dụng CameraUpdate để thực hiện zoom
        mMap.moveCamera(cameraUpdateBounds)
        mMap.animateCamera(cameraUpdateZoom)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (intent != null){
            activity?.stopService(intent)
        }
    }

    private fun convertLayoutToBitmap(context: Context, layoutId: Int): Bitmap {
        // Inflate the XML layout to create a View object
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(layoutId, null)

        val tvLocation = view.findViewById<TextView>(R.id.tvLocation)
        val tvStatusJob = view.findViewById<TextView>(R.id.tvStatusJob)
        val tvEmpJob = view.findViewById<TextView>(R.id.tvEmpJob)

        if (dataResponse?.isNotEmpty() == true){
            for (data in dataResponse!!){
                tvLocation.text = data.cpName
                tvStatusJob.text = data.jobStateDesc
                tvEmpJob.text = data.fullName
            }
        }

        // Measure the View with UNSPECIFIED width and height
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        // Layout the View to ensure it has the correct dimensions
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        // Create a Bitmap with the measured width and height
        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)

        // Create a Canvas to draw the View onto the Bitmap
        val canvas = Canvas(bitmap)

        // Render the View onto the Bitmap
        view.draw(canvas)

        // Return the resulting Bitmap
        return bitmap
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}