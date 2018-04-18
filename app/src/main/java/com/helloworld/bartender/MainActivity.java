package com.helloworld.bartender;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.helloworld.bartender.Edit.EditView;
import com.helloworld.bartender.FilterList.FilterListView;
import com.helloworld.bartender.FilterList.HorizontalAdapter.horizontal_adapter;
import com.helloworld.bartender.FilterableCamera.FCamera;
import com.helloworld.bartender.FilterableCamera.FCameraCapture;
import com.helloworld.bartender.FilterableCamera.FCameraPreview;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.RetroFilter;
import com.helloworld.bartender.SingleClickListener.OnSingleClickListener;
import com.helloworld.bartender.VersionChecker.MarketVersionChecker;
import com.kobakei.ratethisapp.RateThisApp;

import java.io.File;

//TODO: back 키 이벤트 처리하기, 필터값 수정,삭제,저장,적용, 필터 아이콘 클릭시 체크 유지

public class MainActivity extends AppCompatActivity {

    // 카메라 관련
    private FCameraPreview fCameraPreview;
    private FCameraCapture fCameraCapture;
    private FCamera fCamera;

    private int cameraTimerState = 0;

    // 카메라 애니메이션 관련
    private TextView timerTextView;
    private ImageView captureEffectImg;
    private ImageView openEffectImg;
    private Animation cameraCaptureInnerAnim;

    // 버튼
    private ImageButton cameraSwitchingBtt;
    private ImageButton cameraFlashBtt;
    private ImageButton cameraTimerBtt;
    private ImageButton settingBtt;

    private ImageButton galleryBtt;
    private ImageButton cameraCaptureBtt;
    private ImageButton editBtt;

    // CustomViews
    private FilterListView mFilterListView;
    private horizontal_adapter mHorizontal_adapter;
    private EditView editView;

    private String device_version;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //rate this app
        RateThisApp.onCreate(this);
        RateThisApp.showRateDialogIfNeeded(this);
        RateThisApp.Config config = new RateThisApp.Config(1, 2);
        RateThisApp.init(config);

        // 카메라 관련
        fCameraPreview = findViewById(R.id.cameraView);
        fCameraCapture = new FCameraCapture(this);
        fCameraCapture.setSaveDirectory(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + File.separator + getString(R.string.app_name));

        fCamera = new FCamera(this, fCameraPreview, fCameraCapture);
        fCamera.setCallback(new FCamera.Callback() {
            @Override
            public void onOpened() {
                switch (fCamera.getFlashSetting()) {
                    case AUTO:
                        cameraFlashBtt.setImageResource(R.drawable.ic_camera_flash_auto);
                        break;
                    case OFF:
                        cameraFlashBtt.setImageResource(R.drawable.ic_camera_flash_off);
                        break;
                    case ON:
                        cameraFlashBtt.setImageResource(R.drawable.ic_camera_flash_on);
                        break;
                }
                Animation open = AnimationUtils.loadAnimation(MainActivity.this, R.anim.open_effect);
                openEffectImg.startAnimation(open);
            }

            @Override
            public void onStartPreview() {

            }

            @Override
            public void onCapture() {
                Animation captuer = AnimationUtils.loadAnimation(MainActivity.this, R.anim.capture_effect);
                captureEffectImg.startAnimation(captuer);
            }

            @Override
            public void onClose() {
            }
        });

        // 카메라 애니메이션 관련
        timerTextView = findViewById(R.id.timerNumberText);
        captureEffectImg = findViewById(R.id.captureEffectImg);
        openEffectImg = findViewById(R.id.openEffectImg);
        cameraCaptureInnerAnim = AnimationUtils.loadAnimation(this, R.anim.capture_inner_effect);

        // 버튼
        cameraSwitchingBtt = findViewById(R.id.cameraSwitchingBtt);
        cameraFlashBtt = findViewById(R.id.cameraFlashBtt);
        cameraTimerBtt = findViewById(R.id.cameraTimerBtt);
        settingBtt = findViewById(R.id.settingBtt);

        galleryBtt = findViewById(R.id.galleryBtt);
        cameraCaptureBtt = findViewById(R.id.cameraCaptureBtt);
        editBtt = findViewById(R.id.editBtt);

        // CustomView
        editView = findViewById(R.id.editView);
        mFilterListView = findViewById(R.id.filterListView);
        mHorizontal_adapter = mFilterListView.getHorizontalAdapter();

        // 상단 버튼 세팅
        cameraSwitchingBtt.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                fCamera.switchCameraFacing();
                switch (fCamera.getFlashSetting()) {
                    case AUTO:
                        cameraFlashBtt.setImageResource(R.drawable.ic_camera_flash_auto);
                        break;
                    case OFF:
                        cameraFlashBtt.setImageResource(R.drawable.ic_camera_flash_off);
                        break;
                    case ON:
                        cameraFlashBtt.setImageResource(R.drawable.ic_camera_flash_on);
                        break;
                }
            }
        });

        cameraSwitchingBtt.setOnTouchListener(OnTouchEffectListener);

        cameraFlashBtt.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                switch (fCamera.getFlashSetting()) {
                    case AUTO:
                        fCamera.setFlashSetting(FCamera.Flash.OFF);
                        break;
                    case OFF:
                        fCamera.setFlashSetting(FCamera.Flash.ON);
                        break;
                    case ON:
                        fCamera.setFlashSetting(FCamera.Flash.AUTO);
                        break;
                }
                switch (fCamera.getFlashSetting()) {
                    case AUTO:
                        cameraFlashBtt.setImageResource(R.drawable.ic_camera_flash_auto);
                        break;
                    case OFF:
                        cameraFlashBtt.setImageResource(R.drawable.ic_camera_flash_off);
                        break;
                    case ON:
                        cameraFlashBtt.setImageResource(R.drawable.ic_camera_flash_on);
                        break;
                }
            }
        });

        cameraFlashBtt.setOnTouchListener(OnTouchEffectListener);

        cameraTimerBtt.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                switch (cameraTimerState) {
                    case 0:
                        ((ImageButton) v).setImageResource(R.drawable.ic_camera_timer_3);
                        cameraTimerState = 3;
                        break;
                    case 3:
                        ((ImageButton) v).setImageResource(R.drawable.ic_camera_timer_5);
                        cameraTimerState = 5;
                        break;
                    case 5:
                        ((ImageButton) v).setImageResource(R.drawable.ic_camera_timer_10);
                        cameraTimerState = 10;
                        break;
                    case 10:
                        ((ImageButton) v).setImageResource(R.drawable.ic_camera_timer_off);
                        cameraTimerState = 0;
                        break;
                }
            }
        });

        cameraTimerBtt.setOnTouchListener(OnTouchEffectListener);

        settingBtt.setOnClickListener(new  OnSingleClickListener() {
            @Override
            public void  onSingleClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsPrefActivity.class));
            }
        });

        settingBtt.setOnTouchListener(OnTouchEffectListener);

        //하단 버튼 세팅
        galleryBtt.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent AlbumIntent = new Intent(Intent.ACTION_VIEW);

                AlbumIntent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);

                AlbumIntent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivity(AlbumIntent);
            }
        });
        galleryBtt.setOnTouchListener(OnTouchEffectListener);

        cameraCaptureBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraCaptureBtt.setClickable(false);
                new CountDownTimer(cameraTimerState * 1000 - 1, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timerTextView.setText(String.valueOf((millisUntilFinished / 1000) + 1));

                        Animation countDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.count_down_effect);
                        timerTextView.startAnimation(countDown);
                    }

                    @Override
                    public void onFinish() {
                        timerTextView.setText("");

                        ImageView cameraCaptureInnerImg = findViewById(R.id.cameraCaptureInnerImg);
                        cameraCaptureInnerImg.startAnimation(cameraCaptureInnerAnim);
                        fCamera.takePicture();

                        cameraCaptureBtt.setClickable(true);
                    }
                }.start();
            }
        });
        cameraCaptureBtt.setOnTouchListener(OnCameraBtnTouchListener);

        editBtt.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                editView.changeState();
            }
        });

        editBtt.setOnTouchListener(OnTouchEffectListener);

        // 초기 필터 세팅
        setCameraFilter(mHorizontal_adapter.getDefaultFilter());

        //버전 체크
        checkVersion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = this.getSharedPreferences(getString(R.string.gallery_pref), 0);
        String path = sp.getString(getString(R.string.key_gallery_name), "Picture");
        fCameraCapture.setSaveDirectory(path);
    }

    public void setCameraFilter(final FCameraFilter filter) {
        fCameraPreview.setFilter(filter);
        fCameraCapture.setFilter(filter);
        editView.setFilter(filter);

        changeCaptureInnerColor(filter);
        editView.setOnSaveListener(new EditView.OnSaveListener() {
            @Override
            public void onSaved() {
                changeCaptureInnerColor(filter);
            }
        });
    }

    private void changeCaptureInnerColor(FCameraFilter filter) {
        if (filter instanceof OriginalFilter) {
            ImageView cameraCaptureInnerImg = findViewById(R.id.cameraCaptureInnerImg);
            float[] hsv = new float[]{0.0f, 0.0f, 0.9f};
            cameraCaptureInnerImg.setColorFilter(Color.HSVToColor(200, hsv));
        } else if (filter instanceof RetroFilter) {
            ImageView cameraCaptureInnerImg = findViewById(R.id.cameraCaptureInnerImg);
            float[] hsv = new float[3];
            Color.RGBToHSV(
                    filter.getValueWithType(RetroFilter.ValueType.RGB_R),
                    filter.getValueWithType(RetroFilter.ValueType.RGB_G),
                    filter.getValueWithType(RetroFilter.ValueType.RGB_B),
                    hsv);
            hsv[1] = hsv[1] < 0.2f ? hsv[1] : 0.2f;
            hsv[2] = 0.90f;
            cameraCaptureInnerImg.setColorFilter(Color.HSVToColor(200, hsv));
        }
    }

    public FCameraCapture getFCameraCapture() {
        return fCameraCapture;
    }

    @Override
    public void onBackPressed() {
        if (editView.IsOpen()) {
            editView.changeState();
        } else if (mHorizontal_adapter.isPopupMenuOpen()) {
            mHorizontal_adapter.dismissPopup();
        } else {
            super.onBackPressed();
        }
    }

    public View.OnTouchListener OnTouchEffectListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageButton imageButton = (ImageButton) v;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    imageButton.setColorFilter(Color.GRAY);
                    return false;
                case MotionEvent.ACTION_UP:
                    imageButton.clearColorFilter();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    imageButton.clearColorFilter();

            }
            return false;
        }
    };

    public View.OnTouchListener OnCameraBtnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(cameraCaptureBtt,
                            "scaleX", 1.1f);
                    ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(cameraCaptureBtt,
                            "scaleY", 1.1f);
                    scaleUpX.setDuration(100);
                    scaleUpY.setDuration(100);

                    AnimatorSet scaleUp = new AnimatorSet();
                    scaleUp.play(scaleUpX).with(scaleUpY);
                    scaleUp.start();
                    return false;
                case MotionEvent.ACTION_UP:

                case MotionEvent.ACTION_CANCEL:
                    ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(cameraCaptureBtt,
                            "scaleX", 1.0f);
                    ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(cameraCaptureBtt,
                            "scaleY", 1.0f);
                    scaleDownX.setDuration(125);
                    scaleDownY.setDuration(125);

                    AnimatorSet scaleDown = new AnimatorSet();
                    scaleDown.play(scaleDownX).with(scaleDownY);

                    scaleDown.start();


            }
            return false;
        }
    };

    private void checkVersion(){
        try {
         device_version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String store_version = "1";
        try {
            store_version = MarketVersionChecker.getMarketVersion(this.getPackageName());
        } catch (Exception e) {
            Log.d("MarketNotExist", e.toString());
        }
        if (store_version.compareTo(device_version) > 0) {
            AlertDialog.Builder mDialog;
            mDialog = new AlertDialog.Builder(this);

            TextView message = new TextView(this);
            message.setText("새로운 버전이 업데이트 되었습니다.");
            message.setGravity(Gravity.CENTER);
            message.setTextSize(20.0f);
            message.setPadding(0, 15, 0, 0);

            mDialog.setTitle("안내")
                    .setView(message)
                    .setCancelable(true)
                    .setPositiveButton("업데이트 바로가기",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    Intent marketLaunch = new Intent(
                                            Intent.ACTION_VIEW);
                                    marketLaunch.setData(Uri
                                            .parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()));
                                    startActivity(marketLaunch);
                                }
                            })
                    .setNegativeButton("Cancel", null);
            AlertDialog alert = mDialog.create();
            alert.show();
        }
    }


}
