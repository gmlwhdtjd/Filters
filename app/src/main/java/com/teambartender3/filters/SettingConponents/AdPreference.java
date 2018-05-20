package com.teambartender3.filters.SettingConponents;

/**
 * Created by samer on 2018-05-21.
 */
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.teambartender3.filters.R;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class AdPreference extends Preference {

    public AdPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AdPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);

        MobileAds.initialize(getContext(),getContext().getString(R.string.admob_id) );

        Activity activity = (Activity) getContext();

        AdView adView  = new AdView(getContext());
//        adView.setAdUnitId(getContext().getString(R.string.banner_ad_unit_id));
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        adView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);


       AdRequest.Builder adRequestBuilder = new AdRequest.Builder();

        //테스트 용
   //     adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);

        adView.loadAd(adRequestBuilder.build());

        ((LinearLayout) view).addView(adView);
        return view;
    }
}
