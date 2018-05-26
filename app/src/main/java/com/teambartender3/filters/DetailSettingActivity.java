package com.teambartender3.filters;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class DetailSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int pageCode = getIntent().getIntExtra("pageCode", 0);
        setContentView(R.layout.activity_setting_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView mDetailText = findViewById(R.id.detailText);
        TextView mTitleText = findViewById(R.id.detail_title);

        AdView adView = (AdView) findViewById(R.id.detail_adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);

        RelativeLayout detailRelativeLayout = findViewById(R.id.detail_relative);

        mDetailText.setMovementMethod(new ScrollingMovementMethod());
        switch (pageCode) {
            case 0:

                mTitleText.setText(getString(R.string.title_open_license));
                mDetailText.setText(R.string.open_license_text);
                getSupportActionBar().setTitle(getString(R.string.title_open_license));
                break;
            case 1:

                mTitleText.setText(getString(R.string.title_terms));
                mDetailText.setText(R.string.terms_service_text);
                getSupportActionBar().setTitle(getString(R.string.title_terms));
                break;

            case 2:
                WebView mWebView = new WebView(getApplicationContext());
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.addRule(RelativeLayout.ABOVE, R.id.detail_adView);
                mWebView.setLayoutParams(params);
                detailRelativeLayout.addView(mWebView);
                WebSettings mWebSettings = mWebView.getSettings();
                mWebSettings.setJavaScriptEnabled(true);
                mWebView.loadUrl("https://gmlwhdtjd.github.io/filters-privacy-policy/");

                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
