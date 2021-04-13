package com.tuyrt.backgroundcamera

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.permissionutil.AdapterPermissionListener
import com.permissionutil.Permission
import com.permissionutil.PermissionImpl

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionImpl.init(this)
            .permission(
                Permission.WRITE_EXTERNAL_STORAGE,
                Permission.CAMERA,
                Permission.SYSTEM_ALERT_WINDOW
            )
            .requestPermission(object : AdapterPermissionListener() {
                override fun onGranted() {
                    Toast.makeText(this@MainActivity, "有权限了", Toast.LENGTH_SHORT).show()
                }
            })
        findViewById<Button>(R.id.takePhoto).setOnClickListener {
            startService(Intent(this, SilentPicService::class.java))
        }
    }
}