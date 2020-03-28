package com.remotemonster.example.simpleconference

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.remotemonster.sdk.Config
import com.remotemonster.sdk.RemonConference
import com.remotemonster.sdk.RemonException
import com.remotemonster.sdk.util.Logger
import org.webrtc.SurfaceViewRenderer
import kotlin.math.roundToInt


// 이 샘플은 안드로이드 SDK 2.7.0 이상 버전이 필요합니다.
class RoomActivity : AppCompatActivity() {
    val TAG = "RoomActivity"

    // layout
    private var mConstraintSet = ConstraintSet()
    private var mDefaultConstraintSet = ConstraintSet()
    private var mCurrentSelectedIndex: Int = 0

    private lateinit var mLayouts: Array<ViewGroup>
    private lateinit var mBinding:ActivityRoomBinding
    private lateinit var mSurfaceViewArray: Array<SurfaceViewRenderer>
    private lateinit var mAvailableView:Array<Boolean>

    private lateinit var mRoomName:String

    private var mError:RemonException? = null

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


    /////////////////////////////////////////////////////////////////////////////////

    private fun initRemonConference() {
        mError = null


        // 공통적으로 사용될 기본 설정
        val config  = Config()
        config.context = this
        config.serviceId="SERVICEID1"
        config.key="1234567890"


        // 컨퍼런스를 위한 마스터 객체를 생성합니다.
        mConference.create ( mRoomName, config ) {
            // 마스터 유저(송출자,나자신) 초기화
            it.config.localView = mSurfaceViewArray[0]
            it.config.remoteView = null

            // 일부 RemonClient의 콜백을 사용하려면 아래와 같은 형태로 콜백을 등록합니다.
            // 마스터(나자신)의 경우 onClose, onError는 룸 콜백으로 전달되므로 추가로 등록할 필요는 없습니다.
            it.on( "onComplete") {

            }

            // 뷰 설정
            mAvailableView[0] = true

        }.on("onRoomCreated") {
            // 마스터 유저가 접속된 이후에 호출(실제 송출 시작)
            // TODO: 실제 유저 정보는 각 서비스에서 관리하므로, 서비스에서 채널과 실제 유저 매핑 작업 진행


            // tag 객체에 holder 형태로 객체를 지정해 사용할 수 있습니다.
            // 예제에서는 단순히 view의 index를 저장합니다.
            it.tag = 0
            Snackbar.make( mBinding.rootLayout, "ch:${it.id}" , Snackbar.LENGTH_SHORT).show()
        }.on("onUserJoined") {
            // 다른 사용자가 입장한 경우 초기화를 위해 호출됨
            // TODO: 실제 유저 매핑 : it.id 값으로 연결된 실제 유저를 얻습니다.


            // 뷰 설정
            val index = getAvailableView()
            if( index > 0 ) {
                it.config.localView = null
                it.config.remoteView = mSurfaceViewArray[index]
                it.tag = index
            }

            // 다른 사용자에 대한 RemonClient 콜백이 필요한 경우 아래와 같이 등록
            // 룸 콜백으로 참여, 퇴장 이벤트가 전달되므로 특별한 경우가 아니면 등록할 필요는 없습니다.
            it.on("onComplete") {

            }.on("onClose") {

            }.on( "onError" ) {

            }

            Snackbar.make( mBinding.rootLayout, "${it.id} 참여" , Snackbar.LENGTH_SHORT).show()
            this.updateViews()
        }.on("onUserLeaved") {
            // 다른 사용자가 퇴장한 경우
            // it.id 와 it.tag 를 참조해 어떤 사용자가 퇴장했는지 확인후 퇴장 처리를 합니다.
            val index = it.tag as Int
            mAvailableView[index] = false


            Snackbar.make( mBinding.rootLayout, "${it.id} 퇴장" , Snackbar.LENGTH_SHORT).show()
            this.updateViews()
        }.close {
            // 마스터유저가 끊어진 경우 호출됩니다.
            // 송출이 중단되면 그룹통화에서 끊어진 것이므로, 다른 유저와의 연결도 모두 끊어집니다.
            if(mError != null ) {
                // 에러로 종료됨
            } else {
                // 종료됨
            }
            Log.d(TAG, "onClose")
        }.error {
            // 송출 채널의 오류 발생시 호출됩니다.
            // 오류로 연결이 종료되면 error -> close 순으로 호출됩니다.
            mError = it
            Log.e(TAG, "error=" + it.description)
            Snackbar.make( mBinding.rootLayout, "error="+it.errorCode+",msg="+ it.description, Snackbar.LENGTH_SHORT).show()
        }
    }

    /////////////////////////////////////////////////////////////////////////////////


    private fun initButtons() {
        // 종료 버튼
        mBinding.imvExit.setOnClickListener {
            mConference.leave()
            finish()
        }


        // audio 활성화/비활성화
        mBinding.tbAudio.setOnClickListener {
            enableLocalAudio = !enableLocalAudio

            val participant = mConference.me
            participant.setLocalAudioEnabled(enableLocalAudio)
        }

    }




    private fun initUI() {
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

        mAvailableView = Array(mSurfaceViewArray.size) {false}
    }



    private fun getAvailableView(): Int {
        for( i in 0 until this.mAvailableView.size) {
            if(!mAvailableView[i]) {
                mAvailableView[i] = true
                return i
            }
        }
        return -1
    }


    private fun convertDpToPixel(dp: Float): Int {
        val metrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return px.roundToInt()
    }


    // ui constraint를 변경하는 예입니다.
    private fun changeView(index: Int) {
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

            if(!mAvailableView[i]) {
                mSurfaceViewArray[i].visibility = View.INVISIBLE
                updateImage(root, View.VISIBLE)
            } else {
                mSurfaceViewArray[i].visibility = View.VISIBLE
                updateImage(root, View.INVISIBLE)
            }
        }
    }

}