package com.example.AI_Assistant

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class ChatAsrUtil(private val context: Context) {
    private var mr: MediaRecorder? = null
    private val mediaPlayer: MediaPlayer

    init {
        mediaPlayer = MediaPlayer()
    }

    fun onClick(actionType: Int) {
        if (actionType == MotionEvent.ACTION_DOWN) {
            if (mr == null) {
                val soundFile = File(
                    ContextCompat.getExternalFilesDirs(
                        context, Environment.DIRECTORY_DCIM
                    )[0].absolutePath + "/openai.mp3"
                )
                if (!soundFile.exists()) {
                    try {
                        soundFile.createNewFile()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                mr = MediaRecorder()
                mr!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                mr!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                mr!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                mr!!.setOutputFile(soundFile.absolutePath)
                Log.d("fileoutput", soundFile.absolutePath)
                try {
                    mr!!.prepare()
                    mr!!.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (actionType == MotionEvent.ACTION_UP) {
            if (mr != null) {
                mr!!.stop()
                mr!!.release()
                mr = null
            }
        }
    }

    fun destroy() {}
}
