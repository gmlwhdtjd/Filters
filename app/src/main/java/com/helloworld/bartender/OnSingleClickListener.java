package com.helloworld.bartender;

import android.os.SystemClock;
import android.view.View;

/**
 * Created by 김현식 on 2018-01-29.
 * 중복 클릭 방지
 */

public abstract class OnSingleClickListener implements View.OnClickListener {
    private static final long MIN_CLICK_INTERVAL=500;
    private long mLastClickTime;

    public abstract void onSingleClick(View v);

    public final void onClick(View v){
        long currentClickTime = SystemClock.uptimeMillis();
        long elapsedTime = currentClickTime-mLastClickTime;
        mLastClickTime =currentClickTime;

        if(elapsedTime<=MIN_CLICK_INTERVAL){
            return;
        }
        onSingleClick(v);
    }
}
