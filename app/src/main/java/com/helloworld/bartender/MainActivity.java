package com.helloworld.bartender;

import android.content.Context;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.hardware.SensorEvent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;

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
import com.helloworld.bartender.SettingConponents.VersionChecker.MarketVersionChecker;
import com.kobakei.ratethisapp.RateThisApp;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.pedant.SweetAlert.SweetAlertDialog;
import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.prefs.PreferencesManager;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.view.MaterialIntroView;

public class MainActivity extends AppCompatActivity {

    // 카메라 관련
    private FCameraPreview fCameraPreview;
    private FCameraCapture fCameraCapture;
    private FCamera fCamera;

    private int cameraTimerState = 0;

    // 카메라 애니메이션 관련
    private FrameLayout cameraFrame;
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

    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;
    private Sensor mSensor;
    private double angleXY;
    private int direction = 0;
    private int priorDirection = 0;

    private String device_version;

    private static final String MAIN_FIRST_INTRO = "main_intro1";
    private static final String MAIN_SECOND_INTRO = "main_intro2";


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //rate this app
        RateThisApp.onCreate(this);
        RateThisApp.showRateDialogIfNeeded(this);
        RateThisApp.Config config = new RateThisApp.Config(1, 3);
        RateThisApp.init(config);

        // 카메라 센서
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorEventListener = new sensorEventListener();
        mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_UI);


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
            }

            @Override
            public void onStartPreview() {
                Animation open = AnimationUtils.loadAnimation(MainActivity.this, R.anim.open_effect);
                openEffectImg.startAnimation(open);
                setButtonLock(false);
                mButtonLock.set(false);
            }

            @Override
            public void onTouchToFocus(MotionEvent event) {
                ImageView focusImg = new ImageView(MainActivity.this);
                focusImg.setImageDrawable(getDrawable(R.drawable.focue_ring));

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(
                        (int) event.getX() - focusImg.getDrawable().getIntrinsicWidth() / 2,
                        (int) event.getY() - focusImg.getDrawable().getIntrinsicHeight() / 2,
                        0, 0);

                cameraFrame.addView(focusImg, params);

                Animation focusAni = AnimationUtils.loadAnimation(MainActivity.this, R.anim.focus_effect);
                focusAni.setAnimationListener(new FocusAnimationListener(focusImg));
                focusImg.startAnimation(focusAni);
            }

            @Override
            public void onCapture() {
                Animation captuer = AnimationUtils.loadAnimation(MainActivity.this, R.anim.capture_effect);
                captureEffectImg.startAnimation(captuer);
            }

            @Override
            public void onCaptured() {
                setButtonLock(false);
                mButtonLock.set(false);
            }

            @Override
            public void onClose() {
            }
        });

        // 카메라 애니메이션 관련
        cameraFrame = findViewById(R.id.cameraFrame);
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
                if (!mButtonLock.getAndSet(true)) {
                    setButtonLock(true);
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
            }
        });
        cameraSwitchingBtt.setOnTouchListener(OnTouchEffectListener);

        cameraFlashBtt.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                switch (fCamera.getFlashSetting()) {
                    case AUTO:
                        fCamera.setFlashSetting(FCamera.Flash.ON);
                        break;
                    case OFF:
                        fCamera.setFlashSetting(FCamera.Flash.AUTO);
                        break;
                    case ON:
                        fCamera.setFlashSetting(FCamera.Flash.OFF);
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

        settingBtt.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
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
                if (!mButtonLock.getAndSet(true)) {
                    setButtonLock(true);
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
                        }
                    }.start();
                }
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

//        //디버그 용
//        new PreferencesManager(this.getApplicationContext()).resetAll();

        showIntro(mFilterListView.getFilterListBtt(), MAIN_FIRST_INTRO, getString(R.string.main_first), Focus.ALL,materialIntroListener,ShapeType.CIRCLE);

    }


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_UI);
        SharedPreferences sp = this.getSharedPreferences(getString(R.string.gallery_pref), 0);
        String path = sp.getString(getString(R.string.key_gallery_name), "Picture");
        fCameraCapture.setSaveDirectory(path);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(mSensorEventListener);
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

    private class sensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {

            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];
            Animation rotAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_effect);


            angleXY = Math.atan2(x, y) * 180 / Math.PI;

            if (z < 8.0 && z > -8.0) {
                if (angleXY >= -30 && angleXY <= 30) {
                    direction = 0;
                    if (priorDirection != direction) {
                        changeDirection(priorDirection, direction);
                        priorDirection = direction;
                    }
                } else if (angleXY >= 60 && angleXY <= 120) {
                    direction = 3;
                    if (priorDirection != direction) {
                        changeDirection(priorDirection, direction);
                        priorDirection = direction;
                    }
                } else if (angleXY >= -120 && angleXY <= -60) {
                    direction = 1;
                    if (priorDirection != direction) {
                        changeDirection(priorDirection, direction);
                        priorDirection = direction;
                    }
                } else if (angleXY >= 150 || angleXY <= -150) {
                    direction = 2;
                    if (priorDirection != direction) {
                        changeDirection(priorDirection, direction);
                        priorDirection = direction;
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    public int getDirection() {
        return direction;
    }

    public void changeDirection(int prior, int direction) {
        float from = prior * (-90);
        float to = 0;
        float dmp = direction - prior;
        if (dmp == 1 || dmp == -3)
            to = from - 90;
        else if (dmp == -1 || dmp == 3)
            to = from + 90;
        else if (dmp == 2 || dmp == -2)
            to = from + 180;
        RotateAnimation rotAnim = new RotateAnimation(from, to, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotAnim.setDuration(500);
        rotAnim.setFillAfter(true);
        rotAnim.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.anim.accelerate_decelerate_interpolator));
        cameraSwitchingBtt.startAnimation(rotAnim);
        cameraFlashBtt.startAnimation(rotAnim);
        cameraTimerBtt.startAnimation(rotAnim);
        settingBtt.startAnimation(rotAnim);
        rotAnim = new RotateAnimation(from, to, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotAnim.setDuration(500);
        rotAnim.setFillAfter(true);
        rotAnim.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.anim.accelerate_decelerate_interpolator));
        galleryBtt.startAnimation(rotAnim);
        editBtt.startAnimation(rotAnim);
    }

    @Override
    public void onBackPressed() {
        if (editView.IsOpen()) {
            editView.close();
        } else if (mHorizontal_adapter.isPopupMenuOpen()) {
            mHorizontal_adapter.dismissPopup();
        } else {
            //앱 종료를 묻는 팝업
            SweetAlertDialog finishDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText("Bye")
                    .setCancelText(getString(R.string.exit_popup_cancel))
                    .setConfirmText(getString(R.string.exit_popup_quit))
                    .setContentText(getString(R.string.exit_popup_content));
            finishDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    finish();
                }
            });

            finishDialog.show();

        }
    }

    public View.OnTouchListener OnTouchEffectListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!mButtonLock.get()) {
                ImageButton imageButton = (ImageButton) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        imageButton.setColorFilter(Color.GRAY);
                        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(imageButton,
                                "scaleX", 0.8f);
                        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(imageButton,
                                "scaleY", 0.8f);
                        scaleUpX.setDuration(70);
                        scaleUpY.setDuration(70);

                        AnimatorSet scaleUp = new AnimatorSet();
                        scaleUp.play(scaleUpX).with(scaleUpY);
                        scaleUp.start();
                        return false;
                    case MotionEvent.ACTION_UP:

                    case MotionEvent.ACTION_CANCEL:
                        imageButton.clearColorFilter();
                        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(imageButton,
                                "scaleX", 1.0f);
                        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(imageButton,
                                "scaleY", 1.0f);
                        scaleDownX.setDuration(70);
                        scaleDownY.setDuration(70);

                        AnimatorSet scaleDown = new AnimatorSet();
                        scaleDown.play(scaleDownX).with(scaleDownY);

                        scaleDown.start();

                }
            }
            return false;
        }
    };

    private class FocusAnimationListener implements Animation.AnimationListener {

        private View mView;

        public FocusAnimationListener(View view) {
            mView = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            if (mView != null) {
                cameraFrame.removeView(mView);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    public View.OnTouchListener OnCameraBtnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!mButtonLock.get()) {
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
            }
            return false;
        }
    };

    private void checkVersion() {
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

            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.update_popup_title_new))
                    .setContentText(getString(R.string.update_message))
                    .showCancelButton(true)
                    .setCancelText(getString(R.string.update_popup_cancel))
                    .setConfirmText(getString(R.string.update_popup_confirm))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            Intent marketLaunch = new Intent(
                                    Intent.ACTION_VIEW);
                            marketLaunch.setData(Uri
                                    .parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()));
                            startActivity(marketLaunch);
                        }
                    })
                    .show();
        }
    }

    private AtomicBoolean mButtonLock = new AtomicBoolean(false);

    private void setButtonLock(boolean lock) {
        cameraSwitchingBtt.setClickable(!lock);
        cameraFlashBtt.setClickable(!lock);
        cameraTimerBtt.setClickable(!lock);
        settingBtt.setClickable(!lock);

        galleryBtt.setClickable(!lock);
        cameraCaptureBtt.setClickable(!lock);
        editBtt.setClickable(!lock);
    }

    //이곳에 리스너 파라미터 추가
    public void showIntro(View view, final String id, String text, Focus focusType, final MaterialIntroListener materialIntroListener, ShapeType shape) {
        new MaterialIntroView.Builder(this)
                .enableDotAnimation(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(focusType)
                .setDelayMillis(100)
                .setIdempotent(true)
                .enableFadeAnimation(true)
                .enableIcon(true)
                .performClick(false)
                .setInfoText(text)
                .setTarget(view)
                .enableDotAnimation(true)
                .setListener(materialIntroListener)
                .setUsageId(id)
                .setShape(shape)
                .dismissOnBackPress(true)
                .show();
     
    }

    MaterialIntroListener materialIntroListener = new MaterialIntroListener() {
        @Override
        public void onUserClicked(String id) {

            if(id.equals(MAIN_FIRST_INTRO)) {
                showIntro(editBtt, MAIN_SECOND_INTRO, getString(R.string.main_second), Focus.NORMAL, this, ShapeType.CIRCLE);
            }

        }

    };
}
