package com.tuyrt.backgroundcamera

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast

/**
 * Created by tuyrt7 on 2021/4/13.
 * 后台服务，用于创建带预览的 SmallCameraWindow
 */
class SilentPicService : Service() {

    private val TAG = "SilentPicService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!SmallCameraWindow.isShowing) {
            SmallCameraWindow.show(this, { e ->
                Log.d(TAG, "error is: $e")
                Toast.makeText(this, "error: $e", Toast.LENGTH_SHORT).show()
                stopSelf()
            }, { photoPath ->
                Log.d(TAG, "拍照: $photoPath")
                Toast.makeText(this, "拍照: $photoPath", Toast.LENGTH_SHORT).show()
                stopSelf()
            })
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        SmallCameraWindow.dismiss()
        Log.d(TAG, "onDestroy: ")
    }
}