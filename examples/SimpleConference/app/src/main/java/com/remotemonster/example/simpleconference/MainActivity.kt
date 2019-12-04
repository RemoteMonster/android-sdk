package com.remotemonster.example.simpleconference

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.remotemonster.example.simpleconference.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val REMON_PERMISSION_REQUEST = 0x0101

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        this.checkPermission()

        mBinding.btnConnectRoom.setOnClickListener {
            if (mBinding.etRoomId.text.toString() == "") {

                Snackbar.make( mBinding.root, "Room ID를 입력해 주세요.", Snackbar.LENGTH_SHORT).show()
            } else {

                val intent = Intent( this, RoomActivity::class.java )
                intent.putExtra("ROOM", mBinding.etRoomId.text.toString() )
                this.startActivity(intent)
            }
        }
    }




    // 안드로이드 권한을 체크를 위한 기본적인 루틴으로, 권한이 없는 경우의 예외처리는 제외 되어있습니다.
    // 사용자에게 필수로 권한을 확인받아야 하는  요소는 CAMERA,RECORD_AUDIO,WRITE_EXTERNAL_STORAGE 입니다.
    private fun checkPermission() {
        // 안드로이드 6.0 미만의 경우 별도 체크하지 않습니다.
        if( Build.VERSION.SDK_INT < 23) {
            return
        }

        val mandatoryPermissions = arrayListOf(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.CHANGE_NETWORK_STATE",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.BLUETOOTH",
            "android.permission.READ_PHONE_STATE"
        )

        val grantList = mutableListOf<String>()
        for( permission in mandatoryPermissions ) {
            val permissionResult = ContextCompat.checkSelfPermission(this, permission)
            if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                grantList.add(permission)
            }
        }

        if(grantList.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, grantList.toTypedArray() , REMON_PERMISSION_REQUEST )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if( requestCode == REMON_PERMISSION_REQUEST ) {
            val deniedList = arrayListOf<String>()
            if( grantResults.isNotEmpty() ) {
                for( i in grantResults.indices ) {
                    if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                        deniedList.add( permissions[i])
                    }
                }
            }

            if( deniedList.isNotEmpty()) {
                // 특정 권한이 없는 경우
                Snackbar.make( mBinding.root, "권한을 체크하세요.", Snackbar.LENGTH_SHORT).show()

            } else {

            }
        }
    }
}