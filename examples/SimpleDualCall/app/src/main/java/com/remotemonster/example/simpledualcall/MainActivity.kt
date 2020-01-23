package com.remotemonster.example.simpledualcall


import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.remotemonster.example.simpledualcall.databinding.ActivityMainBinding
import com.remotemonster.sdk.RemonCall
import com.remotemonster.sdk.RemonCast
import com.remotemonster.sdk.RemonClient
import com.remotemonster.sdk.data.CloseType

// 가장 단순한 형태의 P2P 다중 통화에 대한 샘플입니다.
// 나를 제외한 2명과 통화하는 경우 사용자마다 2개의 채널을 생성해 총 3개의 채널을 사용합니다.
// 각 채널명은 중복되지 않은 명칭이어야 합니다. (테스트계정 사용시 다른 유저와 채널명이 중복될 수 있습니다.)
// 실제 서비스환경에서는 서비스를 위한 별도 DB를 사용하게 되므로, 해당 DB의 사용자 데이터를 사용해
// 중복되지 않는 채널명을 생성해 사용합니다.
class MainActivity : AppCompatActivity() {
    private val REMON_PERMISSION_REQUEST = 0x0101

    // 채널을 구분하기 위해 임의로 지정한 값입니다.
    enum class ChannelIndex {
        FIRST, SECOND, PREVIEW
    }

    var currentSelectedChannel = ChannelIndex.FIRST

    // RemonCall 객체 정의 - P2P 1:1 통화
    // 1:1 통화는 RemonCall 을 사용합니다.
    // 다중 통화를 위해서는 연결할 인원만큼 RemonCall 객체를 추가합니다.
    private var remonCall1: RemonCall? = null
    private var remonCall2: RemonCall? = null


    // 각 peer의 로컬뷰는 연결이후에 표시됩니다.
    // 연결전 로컬뷰를 표시하기 위해서는 별도의 통화 객체를 로컬용으로 사용해야 합니다.
    private var localRemonCallForPreview: RemonCall? = null


    // 연결 여부 저장을 위한 맵
    private var connectionMap = mutableMapOf(ChannelIndex.FIRST to false, ChannelIndex.SECOND to false)

    // 안드로이드 UI에 관련된 부분
    private lateinit var binding:ActivityMainBinding
    private lateinit var inputMethodManager:InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // bind activity layout
        binding = DataBindingUtil.setContentView( this, R.layout.activity_main)

        // 퍼미션 체크를 위한 루틴입니다.
        checkPermission()

        // ui 업데이트
        updateAllViews()


        // 프리뷰를 위한 객체만 별도 생성하고, 프리뷰를 동작시킵니다.
        // 프리뷰가 빌요하지 않은 경우 각 연결의 로컬뷰를 사용할 수 있습니다.
        initRemonCall(ChannelIndex.PREVIEW)


        // 버튼 이벤트 연결
        // 연결, 종료 버튼 클릭이벤트를 정의합니다. RemonCall 객체를 생성하고, 서비스에 연결합니다.
        // RemonCall, RemonCast 객체들은 재사용하지말고, 연결시 생성하고, 종료시 제거해 주어야 합니다.
        binding.btnConnect1.setOnClickListener {
            createRemonObject(ChannelIndex.FIRST)
        }

        binding.btnConnect2.setOnClickListener {
            createRemonObject(ChannelIndex.SECOND)
        }

        binding.layoutRemote1.setOnClickListener{
            currentSelectedChannel = ChannelIndex.FIRST
            updateAllViews()
        }
        binding.layoutRemote2.setOnClickListener{
            currentSelectedChannel = ChannelIndex.SECOND
            updateAllViews()
        }

        // 메시지 버튼 : 선택된 채널로 메시지를 보냅니다.
        binding.btnSend.setOnClickListener {
            val msg = binding.etMessage.text.toString()
            binding.etMessage.text.clear()
            inputMethodManager.hideSoftInputFromWindow(binding.etMessage.windowToken, 0)
            binding.etMessage.clearFocus()

            val remonCall = when( currentSelectedChannel ){
                ChannelIndex.FIRST -> remonCall1
                ChannelIndex.SECOND -> remonCall2
                else -> null
            }

            if( msg.isNotEmpty()) {
                remonCall?.sendMessage(msg)
            }
        }
    }

    override fun onDestroy() {
        remonCall1?.close()
        remonCall2?.close()
        localRemonCallForPreview?.close()
        super.onDestroy()
    }
    
    private fun createRemonObject( index: ChannelIndex) {
        var remonCall: RemonClient?
        lateinit var editText: EditText

        when( index ){
            ChannelIndex.FIRST -> {
                remonCall = remonCall1
                editText = binding.etChannelName1
            }

            ChannelIndex.SECOND -> {
                remonCall = remonCall2
                editText = binding.etChannelName2
            }

            else -> return
        }

        if ( remonCall == null ) {
            if (editText.text.isEmpty()) {
                Snackbar.make(binding.rootLayout, index.name + " 채널명을 입력하세요.", Snackbar.LENGTH_SHORT)
                    .show()
                return
            }

            inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
            editText.clearFocus()

            // RemonCall 초기화
            remonCall = initRemonCall(index)

            // RemonCall 연결
            if( remonCall is RemonCall ) {
                remonCall.connect(editText.text.toString())
            } else if( remonCall is RemonCast) {
                remonCall.create( editText.text.toString())
            }

        } else {
            remonCall.close()
        }
    }

    // RemonCall 초기화
    // Builder 를 사용해 각 설정 정보를 정의
    private fun initRemonCall( index:ChannelIndex ) : RemonClient? {
        when( index ) {
            ChannelIndex.FIRST -> {
                remonCall1 = RemonCall.builder()
                    .context(this)
                    .serviceId("SERVICEID1")
                    .key("1234567890")
                    .videoCodec("VP8")
                    .videoWidth(640)
                    .videoHeight(480)
                    .localView(binding.surfRendererLocal1)
                    .remoteView(binding.surfRendererRemote1)
                    .build()

                // SDK 의 이벤트 콜백을 정의합니다.
                initRemonCallback( ChannelIndex.FIRST )
                return remonCall1
            }

            ChannelIndex.SECOND -> {
                remonCall2 = RemonCall.builder()
                    .context(this)
                    .serviceId("SERVICEID1")
                    .key("1234567890")
                    .videoCodec("VP8")
                    .videoWidth(640)
                    .videoHeight(480)
                    .localView(binding.surfRendererLocal2)
                    .remoteView(binding.surfRendererRemote2)
                    .build()

                initRemonCallback( ChannelIndex.SECOND )
                return remonCall2
            }

            ChannelIndex.PREVIEW -> {
                localRemonCallForPreview = RemonCall.builder()
                    .context(this)
                    .serviceId("SERVICEID1")
                    .key("1234567890")
                    .videoCodec("VP8")
                    .videoWidth(640)
                    .videoHeight(480)
                    .localView( binding.surfRendererPreview )
                    .build()

                localRemonCallForPreview?.onInit {
                    localRemonCallForPreview?.showLocalVideo()
                }

                return null
            }
        }
    }

    // 각 콜백 정의
    private fun initRemonCallback(index:ChannelIndex) {
        val remonCall : RemonClient? = when( index ) {
            ChannelIndex.FIRST -> remonCall1
            ChannelIndex.SECOND -> remonCall2
            else -> return
        }

        // RemonCall, RemonCast 의 초기화가 완료된 후 호출되는 콜백입니다.
        remonCall?.onInit {
        }

        // 서버 접속 및 채널 생성이 완료된 이후 호출되는 콜백입니다.
        if ( remonCall is RemonCall ) {
            remonCall.onConnect {
                Snackbar.make(binding.rootLayout, "채널($it)에 연결되었습니다.", Snackbar.LENGTH_SHORT).show()
                connectionMap[index] = true
                updateView(index)
            }
        } else if( remonCall is RemonCast ) {
            remonCall.onCreate {
                Snackbar.make(binding.rootLayout, "채널($it)을 생성하였습니다.", Snackbar.LENGTH_SHORT).show()
                connectionMap[index] = true
                updateView(index)
            }
        }


        // 다른 사용자와 Peer 연결이 완료된 이후 호출되는 콜백입니다.
        remonCall?.onComplete {
            Snackbar.make(binding.rootLayout, "다른 유저와 연결되었습니다.", Snackbar.LENGTH_SHORT).show()
            updateView( index )
        }

        if( remonCall is RemonCall ) {
            // 상대방이 연결을 끊거나, close() 호출후 종료가 완료되면 호출됩니다.
            // CloseType.MINE : 자신이 close() 를 통해 종료한 경우
            // CloseType.OTHER : 상대방이 close() 를 통해 종료한 경우
            // CloseType.OTHER_UNEXPECTED : 상대방이 끊어져서 연결이 종료된 경우
            // CloseType.UNKNOWN : 오류로인해 연결 종료, 기타 이유 불명확
            remonCall.onClose { closeType: CloseType ->
                when( closeType ) {
                    CloseType.OTHER -> Snackbar.make(binding.rootLayout, "상대방이 연결을 종료하였습니다.", Snackbar.LENGTH_SHORT).show()
                    CloseType.OTHER_UNEXPECTED -> Snackbar.make(binding.rootLayout, "상대방과의 연결이 종료되었습니다.", Snackbar.LENGTH_SHORT).show()
                    else -> Snackbar.make(binding.rootLayout, "연결이 종료되었습니다.", Snackbar.LENGTH_SHORT).show()
                }

                connectionMap[index] = false
                updateView(index)

                when (index) {
                    ChannelIndex.FIRST -> remonCall1 = null
                    ChannelIndex.SECOND -> remonCall2 = null
                    else -> {}
                }
            }

        } else if( remonCall is RemonCast ) {
            remonCall.onClose {
                connectionMap[index] = false
                updateView(index)

                when (index) {
                    ChannelIndex.FIRST -> remonCall1 = null
                    ChannelIndex.SECOND -> remonCall2 = null
                    else -> {}
                }
            }
        }
        // 에러가 발생할 때 호출되는 콜백을 정의합니다.
        // onError 호출 후 연결이 종료된 경우 onClose 가 이어서 호출 됩니다.
        // onError 에서는 에러를 저장하거나 로깅 등의 작업을 진행하시고,
        // onClose 에서 ux 처리를 진행하셔야 합니다.
        remonCall?.onError { e ->
            Log.e("SimpleDualCall", "error="+e.description)

        }

        if( remonCall is RemonCall ) {
            // 연결된 peer 에서 전달한 메시지를 받으면 호출되는 콜백입니다.
            remonCall.onMessage { msg ->
                Snackbar.make(
                    binding.rootLayout,
                    "[" + index.name + "] " + msg,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }


    // 연결된 이후 간단하게 레이아웃을 변경하는 예제입니다.
    // SDK가 아닌 서비스와 관련한 부분으로 참고용도로 살펴보시기 바랍니다.
    private fun updateAllViews() {
        updateView( ChannelIndex.FIRST)
        updateView( ChannelIndex.SECOND )
    }

    private fun updateView( index: ChannelIndex ) {
        lateinit var remoteLayout:RelativeLayout
        lateinit var localLayout:RelativeLayout
        lateinit var button: Button

        when( index) {
            ChannelIndex.FIRST -> {
                remoteLayout = binding.layoutRemote1
                localLayout = binding.layoutLocal1
                button = binding.btnConnect1
            }

            ChannelIndex.SECOND -> {
                remoteLayout = binding.layoutRemote2
                localLayout = binding.layoutLocal2
                button = binding.btnConnect2
            }

            else -> return
        }

        if( index == currentSelectedChannel ) {
            remoteLayout.setBackgroundColor(Color.parseColor( "#900090"))
        } else {
            remoteLayout.setBackgroundColor(Color.parseColor("#000000"))
        }


        val isConnected = connectionMap[index]
        // 로컬 이미지뷰내의 리모트몬스터 이미지 끄기
        for (j in 0 until localLayout.childCount) {
            val view = localLayout.getChildAt(j)

            if (view is ImageView) {
                if(isConnected == true) {
                    view.visibility = View.INVISIBLE
                } else {
                    view.visibility = View.VISIBLE
                }
            }
        }

        if( isConnected == true ) {
            binding.layoutMessage.visibility = View.VISIBLE
            button.text = "끊기"
        } else {
            binding.layoutMessage.visibility = View.GONE
            button.text = "연결"
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
            "android.permission.BLUETOOTH"
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
                Snackbar.make( binding.rootLayout, "권한을 체크하세요.", Snackbar.LENGTH_SHORT).show()

                binding.etChannelName1.isEnabled = false
                binding.etChannelName2.isEnabled = false
                binding.btnConnect1.isEnabled = false
                binding.btnConnect2.isEnabled = false
            } else {
                binding.etChannelName1.isEnabled = true
                binding.etChannelName2.isEnabled = true
                binding.btnConnect1.isEnabled = true
                binding.btnConnect2.isEnabled = true
            }
        }
    }
}
