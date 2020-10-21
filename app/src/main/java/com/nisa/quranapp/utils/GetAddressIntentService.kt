package com.nisa.quranapp.utils

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import java.lang.Exception
import java.util.*

class GetAddressIntentService : IntentService(IDENTIFIER){
    private var addressResultReceiver: ResultReceiver? = null

    override fun onHandleIntent(intent: Intent?) {
        var msg = ""

        //get result receiver from intent
        addressResultReceiver = intent!!.getParcelableExtra("add_receiver")
        if (addressResultReceiver == null){
            return
        }

        val location = intent
            .getParcelableExtra<Location>("add_location")

        //send no location error to results receiver
        if (location == null){
            msg= "No Location, cant go further without location"
            sendResultToReceiver(0, msg)
            return
        }
        val geocoder = Geocoder(this, Locale.getDefault())
        var address: List<Address>? = null
        try {
            address = geocoder.getFromLocation(
                location.latitude,
                location.longitude, 1
            )
        } catch (ignored: Exception){

        }
        if (address == null || address.size == 0){
            msg = "No address found for the location"
            sendResultToReceiver(1, msg)
        }
        else {
            val address = address[0]
            val addressDetails = StringBuffer()

            addressDetails.append(address.adminArea)
            addressDetails.append("\n")
            sendResultToReceiver(2, addressDetails.toString())
        }

    }

    private fun sendResultToReceiver(resultCode: Int, msg: String) {
        val bundle = Bundle()
        bundle.putString("address_result", msg)
        addressResultReceiver!!.send(resultCode, bundle)
    }

    companion object{
        private const val IDENTIFIER = "GetAddressIntentService"
    }


}