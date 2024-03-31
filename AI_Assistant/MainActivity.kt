package com.example.AI_Assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : Activity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        audioPermission
        checkNeedPermissions()
        findViewById<View>(R.id.btn_chat).setOnClickListener(this)
        findViewById<View>(R.id.btn_detect).setOnClickListener(this)

        val request = PeriodicWorkRequest.Builder(SimpleWorker::class.java, 15,
            TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueue(request)

        window.setBackgroundDrawableResource(R.color.colorPrimary)
        val nightModeSwitch = NightModeSwitch(this,window)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private val audioPermission: Unit
        private get() {
            val flag = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            if (flag != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    1
                )
            }
        }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_chat) {
            val it = Intent(this, ChatActivity::class.java)
            startActivity(it)
        } else if(v.id == R.id.btn_detect) {
            val it = Intent(this, ObjDetectActivity::class.java)
            startActivity(it)
        }
    }

    private fun checkNeedPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )
        }
    }
}