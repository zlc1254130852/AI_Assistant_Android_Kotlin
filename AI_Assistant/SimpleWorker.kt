package com.example.AI_Assistant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class SimpleWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    var context=context

    override fun doWork(): Result {

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("normal", "Normal", NotificationManager.
            IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "normal")
            .setContentTitle("Hey there!")
            .setContentText("It's time to learn!")
            .setSmallIcon(R.drawable.icon)
            .setLargeIcon(
                BitmapFactory.decodeResource(context.resources,
                    R.drawable.icon))
            .build()

        manager.notify(1, notification)
//        Log.d("SimpleWorker", "do work in SimpleWorker")
//        return Result.success()
        return Result.success()
    }

}