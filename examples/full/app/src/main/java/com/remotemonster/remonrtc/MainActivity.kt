/*
 * @author    Lucas Choi <sw.maker.dm@gmail.com>
 * Copyright (c) 2017 RemoteMonster, inc. All Right Reserved.
 *
 */

package com.remotemonster.remonrtc

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.remon.remondroid.RemonObserver
import com.remon.remondroid.RemonSingleFactory
import com.remon.remondroid.RemonState
import com.remon.remondroid.data.Channel
import com.remon.remondroid.data.ChannelStatus
import io.vrinda.kotlinpermissions.PermissionCallBack
import io.vrinda.kotlinpermissions.PermissionsActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*


class MainActivity : PermissionsActivity(), NavigationView.OnNavigationItemSelectedListener {

    val REMON_KEY:String = "e3ee6933a7c88446ba196b2c6eeca6762c3fdceaa6019f03"
    val REMON_SERVICE_ID :String = "simpleapp"
    public var isConnected:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        isConnected = false
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

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        requstPermissions()
    }

    fun requstPermissions(){
        val permissionsArr = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.CAMERA)
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


    lateinit var mRemon:RemonSingleFactory
    fun createRemon() {
        runOnUiThread {
            remoteVideoLayout.setPosition(0, 0, 100, 100);
            localVideoLayout.setPosition(0, 0, 100, 100);
            remoteVideoLayout.requestLayout();
            localVideoLayout.requestLayout();
        }

        var config = com.remon.remondroid.Config()
        config.setKey(REMON_KEY);
        config.setServiceId(REMON_SERVICE_ID);
        config.setLocalView(localVideoView);
        config.setRemoteView(remoteVideoView);

        mRemon = RemonSingleFactory()
        mRemon.createRemon(this@MainActivity , config, object: RemonObserver(){
            override fun onSearchChannels(channels: MutableList<Channel>?) {
                super.onSearchChannels(channels)
                setChannelList(channels)
            }

            override fun onClose() {
                super.onClose()
                Thread({
                    createRemon()
                }).start()

            }

            override fun onDisconnectChannel() {
                super.onDisconnectChannel()
                remonCloseProcess()
            }

            override fun onStateChange(state: RemonState?) {
                print("Remon STATE"+state.toString())
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
                            localVideoLayout.setPosition(70, 70, 28, 29);
                            localVideoLayout.requestLayout();
                        }
                    }
                    RemonState.FAIL -> remonCloseProcess()
                    RemonState.EXIT -> remonCloseProcess()
                }
            }
        });
    }

    fun setChannelList(channels: MutableList<Channel>?) {
        runOnUiThread {
            lvChannelList.adapter = ChannelListAdapter(this@MainActivity, channels, mRemon , tvChannelName, btnStartRemon, isConnected)
        }
    }

    private class ChannelListAdapter(context: Context, channels: MutableList<Channel>?, mRemon: RemonSingleFactory,
                                     tvChannelName: TextView, btnStartRemon: Button, connected: Boolean) : BaseAdapter() {
        internal var channelList = channels
        internal var remon = mRemon
        internal var tvChannelName = tvChannelName
        internal var btnStartRemon = btnStartRemon
        internal var isConnected = connected
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
        public val tvChannelItemName: TextView
        public val btnChannelJoin: Button
        init {
            this.tvChannelItemName = item?.findViewById(R.id.tvChannelItemName) as TextView
            this.btnChannelJoin = item?.findViewById(R.id.btnChannelJoin) as Button
        }
    }

    fun remonCloseProcess() {
        runOnUiThread {
            localVideoLayout.setPosition(0, 0, 100, 100);
            localVideoLayout.requestLayout();

            mRemon.softClose()
            isConnected = false
            remoteVideoLayout.visibility = View.INVISIBLE
            btnStartRemon.text = "START REMON"
            tvChannelName.text = ""
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    //    @Override
    //    public boolean onCreateOptionsMenu(Menu menu) {
    //        getMenuInflater().inflate(R.menu.main, menu);
    //        return true;
    //    }

    //    @Override
    //    public boolean onOptionsItemSelected(MenuItem item) {
    //        int id = item.getItemId();
    //
    //        //noinspection SimplifiableIfStatement
    //        if (id == R.id.action_settings) {
    //            return true;
    //        }
    //
    //        return super.onOptionsItemSelected(item);
    //    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        //        int id = item.getItemId();
        //
        //        if (id == R.id.nav_camera) {
        //            // Handle the camera action
        //        } else if (id == R.id.nav_gallery) {
        //
        //        } else if (id == R.id.nav_slideshow) {
        //
        //        } else if (id == R.id.nav_manage) {
        //
        //        } else if (id == R.id.nav_share) {
        //
        //        } else if (id == R.id.nav_send) {
        //
        //        }
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPause() {
        if(isConnected && mRemon !=null) mRemon.close()
        super.onPause()
    }
}
