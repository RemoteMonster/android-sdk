/*
 * @author    Lucas Choi <lucas@remotemonster.com>
 * Copyright (c) 2017 RemoteMonster, inc. All Right Reserved.
 */

package com.remotemonster.remonvideochat;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Class description goes here.
 *
 * @author Lucas Choi <lucas@remotemonster.com>
 * @version 2017-05-17.
 */
public class BackPressEditText extends AppCompatEditText
{
    private OnBackPressListener _listener;


    public BackPressEditText(Context context)
    {
        super(context);
    }


    public BackPressEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    public BackPressEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }


    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && _listener != null)
        {
            _listener.onBackPress();
        }

        return super.onKeyPreIme(keyCode, event);
    }


    public void setOnBackPressListener(OnBackPressListener $listener)
    {
        _listener = $listener;
    }

    public interface OnBackPressListener
    {
        public void onBackPress();
    }
}
