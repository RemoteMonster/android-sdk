package com.remotemonster.example.simpleconference

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.remotemonster.example.simpleconference.databinding.ActivityRoomBinding
import com.remotemonster.sdk.RemonConference
import com.remotemonster.sdk.RemonParticipant
import org.webrtc.SurfaceViewRenderer
import kotlin.math.roundToInt


// 이 샘플은 안드로이드 SDK 2.6.8 이상 버전이 필요합니다.
class RoomActivity : AppCompatActivity() {
    val TAG = "RoomActivity"

    // layout
    private var mConstraintSet = ConstraintSet()
    private var mDefaultConstraintSet = ConstraintSet()
    private var mCurrentSelectedIndex: Int = 0

    private lateinit var mLayouts: Array<ViewGroup>
    private lateinit var mBinding:ActivityRoomBinding
    private lateinit var mSurfaceViewArray: Array<SurfaceViewRenderer>

    private lateinit var mRoomName:String


    // 뷰어 목록
    private var mConference = RemonConference()


    private var enableLocalAudio = true


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        // bind activity layout
        mBinding = DataBindingUtil.setContentView( this, R.layout.activity_room)
        mRoomName = this.intent.getStringExtra("ROOM") ?: ""

        // ui 초기화
        this.initUI()
        this.initButtons()

        // 기본 객체를 설정합니다.
        initRemonConference()
    }

    override fun onBackPressed() {
        mConference.leave()

        super.onBackPressed()
    }



    private fun initRemonConference() {

        // 컨퍼런스를 위한 마스터 객체를 생성합니다.
        mConference.create { participant:RemonParticipant ->
            participant.context = this
            participant.serviceId = "SERVICEID1"
            participant.key="1234567890"
            participant.videoWidth=480
            participant.videoHeight=640
            participant.videoCodec = "VP8"
            participant.logLevel=0
            // 마스터유저의 localView 지정
            participant.localView = mSurfaceViewArray[0]



        }.then { channelName:String ->
            // 마스터유저가 송출 채널에 접속하면 호출됩니다.
            Log.d(TAG, "onCreate: channelName=$channelName")
        }.close {
            // 마스터유저가 끊어진 경우 호출됩니다.
            // 그룹통화에서 끊어진 것이므로, 다른 유저와의 연결도 모두 끊어집니다.
            Log.d(TAG, "onClose")
        }.error {
            Log.e(TAG, "error=" + it.description)
            Snackbar.make( mBinding.rootLayout, "error="+it.errorCode+",msg="+ it.description, Snackbar.LENGTH_SHORT).show()
        }


        // 해당 방에 참여합니다.
        // 새로운 유저가 참여하면 호출되는 콜백을 on 메쏘드로 등록합니다.
        mConference.join( mRoomName )
            .on { channelName:String, index:Int, participant:RemonParticipant ->
                // 다른 사용자가 접속한 경우 호출됩니다.
                // 그룹통화는 특정 인원의 slot이 존재하고, 참여한 사용자의 slot 번호가 index로 전달됩니다.
                Log.d(TAG, "User has connected : $channelName")

                // 새로운 유저가 접속하면, 해당 유저의 채널에 접속하기 위해 전달된 builder를 설정해야 합니다.
                // serviceid, key, url 정보는 master 정보로 설정되며
                // 필요한 경우 이곳에서 변경 가능 합니다.
                participant.context = this
                participant.remoteView = mSurfaceViewArray[index]
                participant.onComplete {
                    this.updateViews();
                }
            }.then {
                // 수신 채널에 접속하면 호출

            }.close {
                print("[ConferenceViewController] onClose")
                this.updateViews()

            }.error {
                // 다른 사용자와위 연결에  오류 발생시 호출됩니다.
                this.updateViews()
                Snackbar.make( mBinding.rootLayout, "error="+it.errorCode+",msg="+ it.description, Snackbar.LENGTH_SHORT).show()
            }


    }

    fun initButtons() {
        // 종료 버튼
        mBinding.imvExit.setOnClickListener {

            mConference.leave()
            finish()
        }


        // audio 활성화/비활성화
        mBinding.tbAudio.setOnClickListener {
            enableLocalAudio = !enableLocalAudio

            var participant = mConference.getClient(0)
            participant.setLocalAudioEnabled(enableLocalAudio)
        }

    }

    /////////////////////////////////////////////////////////////////////////////////


    fun initUI() {
        mConstraintSet = ConstraintSet()
        mDefaultConstraintSet = ConstraintSet()

        mConstraintSet.clone(mBinding.constraintLayout)
        mDefaultConstraintSet.clone(mBinding.constraintLayout)

        mLayouts = arrayOf(
            mBinding.layout0,
            mBinding.layout1,
            mBinding.layout2,
            mBinding.layout3,
            mBinding.layout4,
            mBinding.layout5
        )

        mBinding.layout0.setOnClickListener { changeView(0) }
        mBinding.layout1.setOnClickListener { changeView(1) }
        mBinding.layout2.setOnClickListener { changeView(2) }
        mBinding.layout3.setOnClickListener { changeView(3) }
        mBinding.layout4.setOnClickListener { changeView(4) }
        mBinding.layout5.setOnClickListener { changeView(5) }


        mSurfaceViewArray = arrayOf(
            mBinding.surfRendererLocal,
            mBinding.surfRendererRemote1,
            mBinding.surfRendererRemote2,
            mBinding.surfRendererRemote3,
            mBinding.surfRendererRemote4,
            mBinding.surfRendererRemote5
        )

    }






    fun convertDpToPixel(dp: Float): Int {
        val metrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return px.roundToInt()
    }


    // ui constraint를 변경하는 예입니다.
    fun changeView(index: Int) {
        mCurrentSelectedIndex = index
        val layoutID = mLayouts[mCurrentSelectedIndex].id

        // 축소
        mConstraintSet.clone(mDefaultConstraintSet)

        if (layoutID != R.id.layout0) {
            mConstraintSet.constrainWidth(R.id.layout0, convertDpToPixel(160f))
            mConstraintSet.connect(
                R.id.layout0,
                ConstraintSet.TOP,
                R.id.constraintLayout,
                ConstraintSet.TOP,
                0
            )
            mConstraintSet.connect(
                R.id.layout0,
                ConstraintSet.START,
                R.id.constraintLayout,
                ConstraintSet.START,
                0
            )
            mConstraintSet.clear(R.id.layout0, ConstraintSet.END)
            mConstraintSet.clear(R.id.layout0, ConstraintSet.BOTTOM)


            // 확대
            mConstraintSet.constrainWidth(layoutID, ConstraintSet.MATCH_CONSTRAINT)

            mConstraintSet.connect(
                layoutID,
                ConstraintSet.TOP,
                R.id.constraintLayout,
                ConstraintSet.TOP,
                0
            )
            mConstraintSet.connect(
                layoutID,
                ConstraintSet.START,
                R.id.constraintLayout,
                ConstraintSet.START,
                0
            )
            mConstraintSet.connect(
                layoutID,
                ConstraintSet.END,
                R.id.constraintLayout,
                ConstraintSet.END,
                0
            )
            mConstraintSet.connect(
                layoutID,
                ConstraintSet.BOTTOM,
                R.id.constraintLayout,
                ConstraintSet.BOTTOM,
                0
            )

        }


        val transition = ChangeBounds()
        transition.duration = 200L

        TransitionManager.beginDelayedTransition(mBinding.constraintLayout, transition)
        mConstraintSet.applyTo(mBinding.constraintLayout)


        var view: SurfaceViewRenderer?

        for (i in mLayouts.indices) {
            view = mSurfaceViewArray[i]


            if (i != mCurrentSelectedIndex) {
                view.setZOrderMediaOverlay(true)
                mLayouts[i].bringToFront()

            } else {
               view.setZOrderMediaOverlay(false)
            }
        }

        mBinding.constraintLayout.invalidate()
    }


    private fun updateImage(root: ViewGroup, visible: Int) {
        for (j in 0 until root.childCount) {
            val view = root.getChildAt(j)

            if (view is ImageView) {
                view.setVisibility(visible)
            }
        }
    }


    fun updateViews() {


        for (i in 1..5) {
            val root = mLayouts[i]

            val client = mConference.getClient(i)
            if (client == null) { // 등록된 멤버들중에서 COMPLETE 안된 멤버가 있는지 검색
                mSurfaceViewArray[i].visibility = View.INVISIBLE
                updateImage(root, View.VISIBLE)
            } else {
                mSurfaceViewArray[i].visibility = View.VISIBLE
                updateImage(root, View.INVISIBLE)
            }

        }



    }

}