package com.nisa.quranapp.activity

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.google.android.gms.location.*
import com.nisa.quranapp.R
import com.nisa.quranapp.adapter.SurahAdapter
import com.nisa.quranapp.detail.DetailSurahActivity
import com.nisa.quranapp.fragmentt.JadwalSholatFragment.Companion.newInstance
import com.nisa.quranapp.model.ModelSurah
import com.nisa.quranapp.network.Api
import com.nisa.quranapp.utils.GetAddressIntentService
import kotlinx.android.synthetic.main.activity_list_surah_actvity.*
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList

class ListSurahActvity : AppCompatActivity() , SurahAdapter.onSelectDataa{

    var surahAdapter: SurahAdapter? = null
    var progressDialog: ProgressDialog? = null
    var modelSurah: MutableList<ModelSurah> = ArrayList()
    var hariIni: String? = null
    var tanggal: String? = null
    private var fussedLocationClient: FusedLocationProviderClient? = null
    private var addressResultReceiver: LocationAddressResultReceiver? = null
    private var currentLocation: Location? = null
     var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_surah_actvity)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Mohon Tunggu")
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage("Sedang menampilkan data")

        addressResultReceiver = LocationAddressResultReceiver(Handler())

        val dateNow = Calendar.getInstance().time
        hariIni = android.text.format.DateFormat.format("EEEE", dateNow)
        as String
        tanggal = android.text.format.DateFormat.format("d MMMM yyyy", dateNow) as String
        tvToday.setText("$hariIni, ")
        tvDate.setText(tanggal)

        val sendDetail = newInstance("detail")
        llTime.setOnClickListener(View.OnClickListener {
            sendDetail.show(supportFragmentManager, sendDetail.tag)
        })

        llMosque.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@ListSurahActvity,MasjidActivity::class.java))
        })
        rvSurah.setLayoutManager(LinearLayoutManager(this))
        rvSurah.setHasFixedSize(true)

        fussedLocationClient = LocationServices
            .getFusedLocationProviderClient(this)
        locationCallback = object  :LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.locations[0]

            }
        }
        startLocationUpdates()
        listSurah()

    }

    private val address: Unit
    get() {
        if (!Geocoder.isPresent()){
            Toast.makeText(this@ListSurahActvity,
            "cant find current address, ",
            Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, GetAddressIntentService::class.java)
        intent.putExtra("add_receiver", addressResultReceiver)
        intent.putExtra("add_location", currentLocation)
        startService(intent)
    }

    private fun listSurah() {
        progressDialog!!.show()
        AndroidNetworking.get(Api.URL_LIST_SURAH)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener{
                override fun onResponse(response: JSONArray) {
                    for (i in 0 until response.length()){
                        try {
                            progressDialog!!.dismiss()
                            val dataApi = ModelSurah()
                            val jsonObject = response.getJSONObject(i)
                            dataApi.nomor = jsonObject.getString("nomor")
                            dataApi.nama = jsonObject.getString("nama")
                            dataApi.type = jsonObject.getString("type")
                            dataApi.ayat = jsonObject.getString("ayat")
                            dataApi.asma = jsonObject.getString("asma")
                            dataApi.arti = jsonObject.getString("arti")
                            dataApi.audio = jsonObject.getString("audio")
                            dataApi.keterangan = jsonObject.getString("keterangan")
                            modelSurah.add(dataApi)
                            showListSurah()
                        } catch (e: JSONException){
                            e.printStackTrace()
                            Toast.makeText(this@ListSurahActvity
                            , "Gagal menampilkan data", Toast.LENGTH_SHORT).show()
                        }
                    }

                }

                //sampe sini

                override fun onError(anError: ANError?) {
                        progressDialog!!.dismiss()
                    Toast.makeText(
                        this@ListSurahActvity,
                        "Tidak ada jaringan",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })

    }

    private fun showListSurah() {

        surahAdapter = SurahAdapter(
            this@ListSurahActvity,
            modelSurah, this)

        rvSurah!!.adapter = surahAdapter
    }

    private fun startLocationUpdates() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),LOCATION_PERMISSION_REQUEST_CODE )
        } else{
            val locationRequest = LocationRequest()
            locationRequest.interval = 1000
            locationRequest.fastestInterval = 1000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            fussedLocationClient!!.requestLocationUpdates(
                locationRequest, locationCallback, null)
        }
    }

    override fun onSelected(modelSurah: ModelSurah?) {
        val intent = Intent(this@ListSurahActvity, DetailSurahActivity::class.java)
        intent.putExtra("detailSurah", modelSurah)
        startActivity(intent)

    }

    private inner class LocationAddressResultReceiver
    internal constructor(handler: Handler?) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            if (resultCode == 0) { address }
            if (resultCode == 1) {
                Toast.makeText(
                    this@ListSurahActvity,
                    "Address not found", Toast.LENGTH_SHORT
                ).show()
            }
            val currentAdd = resultData.getString("address_result")
            showResults(currentAdd) } }
    private fun showResults(currentAdd: String?) {
        txtLocation!!.text = currentAdd
    }
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }
    override fun onPause() {
        super.onPause()
        fussedLocationClient!!.removeLocationUpdates(locationCallback)
    }

    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
    }
}