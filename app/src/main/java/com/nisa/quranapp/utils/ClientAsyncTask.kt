package com.nisa.quranapp.utils

import android.os.AsyncTask
import com.nisa.quranapp.fragmentt.JadwalSholatFragment
import okhttp3.HttpUrl
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.math.MathContext
import java.net.HttpURLConnection
import java.net.URL

class ClientAsyncTask(
    private val mathContext: JadwalSholatFragment,
    postExecuteListener: OnPostExecuteListener
):AsyncTask<String, String, String>() {

    val CONNECTION_TIMEOUT_MILLISECONDS = 60000
    private val mPostExecuteListener:OnPostExecuteListener = postExecuteListener

    interface OnPostExecuteListener {
        fun onPostExecute(result: String)
    }

    override fun doInBackground(vararg urls: String?): String {
        var urlConnection: HttpURLConnection? = null

        try {
            var url = URL(urls[0])
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connectTimeout = CONNECTION_TIMEOUT_MILLISECONDS
            urlConnection.readTimeout = CONNECTION_TIMEOUT_MILLISECONDS

            val inString = streamToString(urlConnection.inputStream)

            return inString
        } catch (ex: Exception){

        } finally {
            if (urlConnection != null){
                urlConnection.disconnect()
            }
        }

        return ""
    }

    private fun streamToString(inputStream: InputStream): String {
        val bufferReader = BufferedReader(InputStreamReader(inputStream))
        var line: String
        var result = ""

        try {
            do {
                line = bufferReader.readLine()
                if (line != null){
                    result += line
                }
            } while (true)
            inputStream.close()
        } catch (ex: Exception){

        }
        return result
    }
}