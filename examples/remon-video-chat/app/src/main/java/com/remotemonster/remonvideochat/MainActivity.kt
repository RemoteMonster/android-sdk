/*
 * @author    Lucas Choi <sw.maker.dm@gmail.com>
 * Copyright (c) 2017 RemoteMonster, inc. All Right Reserved.
 *
 */

package com.remotemonster.remonvideochat

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.remon.remondroid.RemonObserver
import com.remon.remondroid.RemonSingleFactory
import com.remon.remondroid.RemonState
import com.remon.remondroid.data.Channel
import com.remon.remondroid.data.ChannelStatus
import io.vrinda.kotlinpermissions.PermissionCallBack
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.chat_input_box.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.webrtc.RendererCommon
import java.io.IOException


class MainActivity : PermissionsActivity(), NavigationView.OnNavigationItemSelectedListener {
    val REMON_KEY:String = "e3ee6933a7c88446ba196b2c6eeca6762c3fdceaa6019f03"
    val REMON_SERVICE_ID :String = "simpleapp"
    val BRING_GALLERY_REQUESTCODE = 1021
    var GotoGallery:Boolean = false

    var isConnected:Boolean = false
    var isRunning:Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        GotoGallery = false
        isConnected = false
        isRunning = true
        val toggle = object : ActionBarDrawerToggle(
                this, drawer_layout,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerClosed(drawerView: View?) {
                super.onDrawerClosed(drawerView)
            }

            override fun onDrawerOpened(drawerView: View?) {
                btnStartRemon.setOnClickListener(View.OnClickListener { v: View? -> showChannelNameDiaolog() })
                imvSearch.setOnClickListener(View.OnClickListener { v: View? -> mRemon.searchChannels("") })
                super.onDrawerOpened(drawerView)
            }
        }
        drawer_layout.setDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        requstPermissions()

        InitViewEvent()
    }

    fun InitViewEvent(){
        btnChat.setOnClickListener(View.OnClickListener { v: View? ->
            ViewModeFromChat("CHAT_INPUT_MODE")

        })

        imvChatSend.setOnClickListener(View.OnClickListener { v: View? ->
            if (!etChatInput.text.toString().equals("")) {
                mRemon.sendMessage("str",etChatInput.text.toString())
                addChatMsg(true, etChatInput.text.toString())
                etChatInput.setText("")
            }
        })

        btnImg.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = MediaStore.Images.Media.CONTENT_TYPE
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            startActivityForResult(intent, BRING_GALLERY_REQUESTCODE)
            GotoGallery = true
        })

        etChatInput.setOnBackPressListener(object : BackPressEditText.OnBackPressListener{
            override fun onBackPress() {
                ViewModeFromChat("CHAT_CLOSE_MODE")
            }
        })

        imvChatInputFold.setOnClickListener(View.OnClickListener { v: View? -> ViewModeFromChat("CHAT_CLOSE_MODE")})

        imvImgClose.setOnClickListener(View.OnClickListener { v: View? -> runOnUiThread {
            imvSharedImg.setImageBitmap(null)
            rlSharedImg.visibility = View.INVISIBLE
        }})
    }

    fun ViewModeFromChat(state:String) {
        runOnUiThread {
            when(state){
                "CHAT_INPUT_MODE" ->{
                    rlChatInputBox.visibility = View.VISIBLE
                    showSoftKey()
                    etChatInput.requestFocus()

                    llBtnBox.visibility = View.INVISIBLE
                }

                "CHAT_CLOSE_MODE" ->{
                    etChatInput.setText("")
                    etChatInput.hint = "Write Message"
                    rlChatInputBox.visibility = View.INVISIBLE
                    hideSoftKey()

                    llBtnBox.visibility = View.VISIBLE
                }
            }
        }
    }

    var mInputMethodManager: InputMethodManager? = null
    fun hideSoftKey() {
        if (etChatInput != null) {
            if (mInputMethodManager != null) {
                mInputMethodManager!!.hideSoftInputFromWindow(etChatInput.windowToken, 0)
                etChatInput.clearFocus()
            }
        }
    }
    fun showSoftKey() {
        if (mInputMethodManager != null) {
            mInputMethodManager!!.toggleSoftInput(0, 0)
        }
    }

    fun requstPermissions(){
        val permissionsArr = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        for (permission in permissionsArr) {
            if (ActivityCompat.checkSelfPermission(applicationContext, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeed.add(permission)
            }
        }
        if (permissionsNeed.size > 0) {
            requestPermissions(permissionsArr, object : PermissionCallBack {
                override fun permissionGranted() {
                    createRemon()
                    super.permissionGranted()
                }

                override fun permissionDenied() {
                    super.permissionDenied()

                    val finishDlg = AlertDialog.Builder(this@MainActivity).create()
                    finishDlg.setTitle("Permissions")
                    finishDlg.setMessage("권한이 없으므로 앱을 종료합니다.")
                    finishDlg.setButton(AlertDialog.BUTTON_POSITIVE, "OK", {
                        dialogInterface, i ->
                        dialogInterface.dismiss()
                        this@MainActivity.finish()
                    })
                    finishDlg.show()
                }
            })
        } else {
            createRemon()
        }
    }

    fun showChannelNameDiaolog(){
        if (isConnected == false) {
            val channelDlg = AlertDialog.Builder(this@MainActivity).create()
            channelDlg.setTitle("Connect channel")
            channelDlg.setMessage("Please enter channel")

            var etChannel = EditText(this@MainActivity)
            etChannel.setSingleLine()

            channelDlg.setView(etChannel)

            channelDlg.setButton(AlertDialog.BUTTON_POSITIVE, "OK", {
                dialogInterface, i ->
                tvChannelName.text = etChannel.text
                mRemon.connectChannel(etChannel.text.toString());
                isConnected = true
                btnStartRemon.text = "CLOSE"
                drawer_layout.closeDrawers()
            })
            channelDlg.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", {
                dialogInterface, i ->
                dialogInterface.dismiss()
            })
            channelDlg.show()
        } else {
            remonCloseProcess()
        }
    }




    var mRemon:RemonSingleFactory = RemonSingleFactory()
    fun createRemon() {
        runOnUiThread {
            remoteVideoLayout.setPosition(0, 0, 100, 100)
            localVideoLayout.setPosition(0, 0, 100, 100)
            remoteVideoLayout.requestLayout()
            localVideoLayout.requestLayout()
        }

        var config = com.remon.remondroid.Config()
        config.setKey(REMON_KEY)
        config.setServiceId(REMON_SERVICE_ID)
        config.setSocketFrameSize(2048000)
        config.setLocalView(localVideoView)
        config.setRemoteView(remoteVideoView)

        mRemon = RemonSingleFactory()
        mRemon.createRemon(this@MainActivity , config, object: RemonObserver(){
            override fun onMessage(code: String?, msg: String?) {
                if (code.equals("str")) {
                    addChatMsg(false, msg as String)
                } else {
                    runOnUiThread {
                        rlSharedImg.visibility = View.VISIBLE
                        imvSharedImg.visibility = View.VISIBLE
                        imvImgClose.visibility = View.VISIBLE
                        imvSharedImg.setImageBitmap(ImageProcessor.Base64ToBitmap(msg))
                    }
                }
            }

            override fun onSearchChannels(channels: MutableList<Channel>?) {
                super.onSearchChannels(channels)
                setChannelList(channels)
            }

            override fun onClose() {
                super.onClose()
                if (isRunning) {
                    Thread({
                        createRemon()
                    }).start()

                }
            }

            override fun onDisconnectChannel() {
                super.onDisconnectChannel()
                remonCloseProcess()
            }

            override fun onStateChange(state: RemonState?) {
                if (this@MainActivity.isRunning) {
                    super.onStateChange(state)
                    when (state) {
                        RemonState.INIT -> {
                            mRemon.showLocalVideo()
                            mRemon.searchChannels("")
                            runOnUiThread {
                                localVideoLayout.visibility = View.VISIBLE
                            }
                        }
                        RemonState.WAIT -> print("WAIT")
                        RemonState.CONNECT->{
                            runOnUiThread {
                                isConnected = true
                                remoteVideoLayout.visibility = View.VISIBLE
                                remoteVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                                remoteVideoView.requestLayout()

                                localVideoLayout.setPosition(70, 70, 28, 29)
                                localVideoLayout.requestLayout()

                                lvChatList.visibility = View.VISIBLE
                                llBtnBox.visibility = View.VISIBLE

                                InitChatList()
                            }
                        }
                        RemonState.FAIL -> remonCloseProcess()
                        RemonState.EXIT -> remonCloseProcess()
                    }
                }
            }
        });
    }

    fun setChannelList(channels: MutableList<Channel>?) {
        runOnUiThread {
            lvChannelList.adapter = ChannelListAdapter(this@MainActivity, channels, mRemon , tvChannelName, btnStartRemon, isConnected , drawer_layout)
        }
    }

    private class ChannelListAdapter(context: Context, channels: MutableList<Channel>?, mRemon: RemonSingleFactory,
                                     tvChannelName: TextView, btnStartRemon: Button, connected: Boolean, drawer_layout: DrawerLayout) : BaseAdapter() {
        internal var channelList = channels
        internal var remon = mRemon
        internal var tvChannelName = tvChannelName
        internal var btnStartRemon = btnStartRemon
        internal var isConnected = connected
        internal var drawer_layout = drawer_layout;
        internal var context = context;

        private val mInflator: LayoutInflater

        init {
            this.mInflator = LayoutInflater.from(context)
        }

        override fun getCount(): Int {
            return channelList!!.size
        }

        override fun getItem(position: Int): Any {
            return channelList!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            val view: View?
            val vh: ListItemHolder
            if (convertView == null) {
                view = this.mInflator.inflate(R.layout.list_item_channel, parent, false)
                vh = ListItemHolder(view)
                view.tag = vh
            } else {
                view = convertView
                vh = view.tag as ListItemHolder
            }

            vh.tvChannelItemName.text = channelList!![position].id
            if (channelList!![position].status == ChannelStatus.COMPLETE) {
                vh.btnChannelJoin.text = "FULL"
            } else {
                vh.btnChannelJoin.text = "JOIN"
                vh.btnChannelJoin.setOnClickListener(View.OnClickListener { v: View? ->
                    if (isConnected) {
                        Toast.makeText(context, "Close후에 시도해 주세요.", Toast.LENGTH_LONG).show()
                    } else {
                        drawer_layout.closeDrawers()
                        tvChannelName.text = channelList!![position].id
                        remon.connectChannel(channelList!![position].id);
                        btnStartRemon.text = "CLOSE"
                    }
                })
            }
            return view
        }
    }

    private class ListItemHolder(item: View?) {
        val tvChannelItemName: TextView
        val btnChannelJoin: Button
        init {
            this.tvChannelItemName = item?.findViewById(R.id.tvChannelItemName) as TextView
            this.btnChannelJoin = item?.findViewById(R.id.btnChannelJoin) as Button
        }
    }

    fun remonCloseProcess() {
        runOnUiThread {
            localVideoLayout.setPosition(0, 0, 100, 100)
            localVideoLayout.requestLayout()
            lvChatList.visibility = View.INVISIBLE
            llBtnBox.visibility = View.INVISIBLE

            imvSharedImg.setImageBitmap(null)
            rlSharedImg.visibility = View.INVISIBLE

            mRemon.softClose()
            isConnected = false
            remoteVideoLayout.visibility = View.INVISIBLE
            btnStartRemon.text = "START REMON"
            tvChannelName.text = ""
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPause() {
        if (!GotoGallery) {
            if(isConnected && mRemon !=null) mRemon.close()
        }
        super.onPause()
    }

    override fun onDestroy() {
        if(mRemon !=null) {
            isRunning = false
            mRemon.close()
        }
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BRING_GALLERY_REQUESTCODE) {
            if (data != null) {
                var bitmap:Bitmap? = null
                try {
                    /* 이미지 경로를 얻어온다*/
                    val selectedImage = data.data
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    bitmap = ImageProcessor.resizeBitmapImageFn(bitmap, 1000)
                    bitmap = ImageProcessor.getRotatedBitmap(ImageProcessor.getRealPathFromURI(this@MainActivity, selectedImage), bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (bitmap != null) {
                    runOnUiThread {
                        rlSharedImg.visibility = View.VISIBLE
                        imvSharedImg.visibility = View.VISIBLE
                        imvImgClose.visibility = View.VISIBLE
                        imvSharedImg.setImageBitmap(bitmap)
                    }

                    var bitmapStr:String = ImageProcessor.bitmapToBase64(bitmap)
                    var transStr:String = bitmapStr.replace("\n","")
                    mRemon.sendMessage("img",transStr)
                }
            }
            GotoGallery = false
        }
    }
}
