package com.example.AI_Assistant

import android.content.Context
import android.media.MediaPlayer
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class ChatTtsUtil(private val context: Context) {
    private val mediaPlayer: MediaPlayer

    init {
        mediaPlayer = MediaPlayer()
    }

    fun ttsStart(result: String) {
        val json2 = """      {
        "model": "tts-1",
        "input": "$result",
        "voice": "alloy"
      }"""
        val mediaType2 = MediaType.parse("application/json")
        val requestBody2 = RequestBody.create(mediaType2, json2)
        val request2 = Request.Builder().post(requestBody2)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "")
            .url("https://api.openai.com/v1/audio/speech").build()
        val okHttpClient2 = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS).build()

//            Response response2 = okHttpClient2.newCall(request2).execute();
        val call = okHttpClient2.newCall(request2)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TAG-FAILED：", e.toString() + "")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val `is` = body?.byteStream()
                val bs = ByteArray(1024)
                var len: Int
                Log.d(
                    "fileStorage",
                    ContextCompat.getExternalFilesDirs(
                        context,
                        Environment.DIRECTORY_DCIM
                    )[0].absolutePath
                )
                val os: OutputStream = FileOutputStream(
                    ContextCompat.getExternalFilesDirs(
                        context, Environment.DIRECTORY_DCIM
                    )[0].absolutePath + "/speech.mp3"
                )

                while (`is`?.read(bs).also {
                        if (it != null) {
                            len = it
                        } else {len=0}
                    } != -1) {
                    os.write(bs, 0, len)
                }
                os.close()
                `is`?.close()
                mediaPlayer.reset()
                mediaPlayer.setDataSource(
                    ContextCompat.getExternalFilesDirs(
                        context,
                        Environment.DIRECTORY_DCIM
                    )[0].absolutePath + "/speech.mp3"
                )
                mediaPlayer.prepare()
                mediaPlayer.start()
            }
        })
    }
}
