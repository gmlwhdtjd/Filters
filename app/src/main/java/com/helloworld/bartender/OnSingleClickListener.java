package com.helloworld.bartender;

/**
 * Created by samer on 2018-04-15.
 */
import android.os.SystemClock;
import android.view.View;

public abstract class OnSingleClickListener implements View.OnClickListener {
    // 중복 클릭 방지 시간 설정
    private static final long MIN_CLICK_INTERVAL=600;
    private long mLastClickTime;
    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        long currentClickTime=SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime-mLastClickTime;

        // 중복 클릭인 경우
        if(elapsedTime<=MIN_CLICK_INTERVAL){
            return;
        }

        mLastClickTime=currentClickTime;

        // 중복 클릭아 아니라면 추상함수 호출
        onSingleClick(v);
    }

}