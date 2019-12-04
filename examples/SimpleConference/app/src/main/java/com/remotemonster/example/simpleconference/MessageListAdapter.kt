package com.remotemonster.example.simpleconference


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView

import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.databinding.ViewDataBinding
import com.remotemonster.example.simpleconference.databinding.LayoutMessageBinding


class MessageListAdapter(private var mContext: Context) : BaseAdapter() {
    private var mList: List<Message>? = null


    fun setList(list: List<Message>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        if ( mList == null ) return 0

        return mList!!.size
    }

    override fun getItem(i: Int): Any {
        return mList!![i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        val binding: LayoutMessageBinding?
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_message, null)
            binding = DataBindingUtil.bind<ViewDataBinding>(view!!) as LayoutMessageBinding?
            view.tag = binding
        } else {
            binding = view.tag as LayoutMessageBinding
        }

        binding?.userName?.text = mList!![i].user
        binding?.userMessage?.text = mList!![i].message
        return binding!!.root
    }

    companion object {

        @JvmStatic
        @BindingAdapter("list")
        fun bindList(view: ListView, list: ObservableArrayList<Message>) {
            val adapter = view.adapter as MessageListAdapter
            adapter.setList(list)
        }
    }


}