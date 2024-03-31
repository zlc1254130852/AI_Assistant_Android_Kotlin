package com.example.AI_Assistant

import android.util.Log
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.util.concurrent.TimeUnit

object ChatGptUtil {
    fun sendHttpRequest(question: String, callback: Callback?) {
        val url = "https://api.openai.com/v1/chat/completions"
        val json = """{
        "model": "gpt-3.5-turbo",
                "messages": [
        {
            "role": "system",
                "content": "You are a helpful assistant."
        },
        {
            "role": "user",
                "content": "$question"
        }
    ],"stream":true,"max_tokens":500,"temperature":0.5
  }"""
        val mediaType = MediaType.parse("application/json")
        val requestBody = RequestBody.create(mediaType, json)
        val request = Request.Builder().post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "")
            .url(url)
            .build()
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
        okHttpClient.newCall(request).enqueue(callback)
    }

    /**
     *
     * @param result
     * @return
     * @throws JSONException
     */
    @Throws(JSONException::class)
    fun parse(result: String?): String {
        var content = ""
        val jsonTokener = JSONTokener(result)
        val jsonObject = JSONObject(jsonTokener)
        val choices = jsonObject.getJSONArray("choices")
        if (choices != null) {
            val messageObject = choices.getJSONObject(0).getJSONObject("message")
            content = messageObject.getString("content")
        }
        return content
    }

    @Throws(JSONException::class)
    fun parseStream(result: String?): String {
        var content = ""
        val jsonTokener = JSONTokener(result)
        val jsonObject = JSONObject(jsonTokener)
        val choices = jsonObject.getJSONArray("choices")
        if (choices != null) {
            val messageObject = choices.getJSONObject(0).getJSONObject("delta")
            val finish_reason = choices.getJSONObject(0).getString("finish_reason")
            if (finish_reason != "stop") content = messageObject.getString("content")
        }
        return content
    }
}
