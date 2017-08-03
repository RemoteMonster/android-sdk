package com.remotemonster.remonvideochat

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.content_main.*

/**
 * Created by lucas on 2017-08-02.
 */
abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    var mChatList:ArrayList<MessageInfo> = ArrayList()
    private var mAdapter:ChatListAdapter? = null
    protected fun InitChatList(){
        mChatList = ArrayList<MessageInfo>()
        mChatList.clear()
        mAdapter = ChatListAdapter(this@BaseActivity, mChatList)
        lvChatList.adapter = mAdapter
    }

    protected fun addChatMsg(isSendMsg:Boolean, msg:String){
        runOnUiThread {
            var messageInfo = MessageInfo()
            messageInfo.mMessage = msg
            messageInfo.isSendMsg = isSendMsg

            mChatList.add(messageInfo)
            mAdapter?.setData(mChatList)
            lvChatList.setSelection(mAdapter!!.count -1)
        }
    }

    private class ChatListAdapter(context: Context, mChatList:ArrayList<MessageInfo>) : BaseAdapter(){
        internal var mChatList = mChatList
        private val mChatInflator: LayoutInflater

        fun setData(chatList:ArrayList<MessageInfo>) {
            mChatList = chatList
            notifyDataSetChanged()
        }

        init{
            this.mChatInflator = LayoutInflater.from(context)
        }

        override fun getCount(): Int {
            return mChatList.size
        }

        override fun getItem(position: Int): Any {
            return mChatList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View?
            val vh: ChatItemHolder
            if (convertView == null) {
                view = this.mChatInflator.inflate(R.layout.list_item_chat, parent, false)
                vh = ChatItemHolder(view)
                view.tag = vh
            } else {
                view = convertView
                vh = view.tag as ChatItemHolder
            }

            if (mChatList[position].isSendMsg) {
                /* 내가 보낸 메시지*/
                vh.llReceiveMsg.visibility = View.INVISIBLE
                vh.llSendMsg.visibility = View.VISIBLE
                vh.tvSendMsg.text = mChatList[position].mMessage
            } else {
                /* 상대방이 보낸 메시지 */
                vh.llReceiveMsg.visibility = View.VISIBLE
                vh.llSendMsg.visibility = View.INVISIBLE
                vh.tvReceiveMsg.text = mChatList[position].mMessage

            }
            return view!!
        }
    }


    private class ChatItemHolder(row: View?) {
        public val llReceiveMsg: LinearLayout
        public val llSendMsg: LinearLayout
        public val tvReceiveMsg: TextView
        public val tvSendMsg: TextView

        init {
            this.llReceiveMsg = row?.findViewById(R.id.llReceiveMsg) as LinearLayout
            this.llSendMsg = row?.findViewById(R.id.llSendMsg) as LinearLayout
            this.tvReceiveMsg = row?.findViewById(R.id.tvReceiveMsg) as TextView
            this.tvSendMsg = row?.findViewById(R.id.tvSendMsg) as TextView
        }
    }

    inner class MessageInfo {
        public var mMessage: String = ""
        public var isSendMsg: Boolean = false

    }
}