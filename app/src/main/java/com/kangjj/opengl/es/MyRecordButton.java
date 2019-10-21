package com.kangjj.opengl.es;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class MyRecordButton extends AppCompatTextView {

    private OnRecordListener mListener;

    public MyRecordButton(Context context) {
        super(context);
    }

    public MyRecordButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mListener == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                mListener.onStartRecording();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                mListener.onStopRecording();
                break;
        }
        return true;
    }

    public void setOnRecordListener(OnRecordListener mListener) {
        this.mListener = mListener;
    }

    public interface OnRecordListener {
        void onStartRecording();
        void onStopRecording();
    }
}
