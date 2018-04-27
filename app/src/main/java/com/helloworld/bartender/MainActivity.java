package com.helloworld.bartender;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.hardware.SensorEvent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.helloworld.bartender.Edit.EditView;
import com.helloworld.bartender.FilterList.FilterListView;
import com.helloworld.bartender.FilterableCamera.FCamera;
import com.helloworld.bartender.FilterableCamera.FCameraCapture;
import com.helloworld.bartender.FilterableCamera.FCameraPreview;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.RetroFilter;
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

    // 버튼
    private ImageButton cameraSwitchingBtt;
    private ImageButton cameraFlashBtt;
    private ImageButton cameraTimerBtt;
    private ImageButton settingBtt;

    private ImageButton galleryBtt;
    private ImageButton cameraCaptureBtt;
    private ImageButton editBtt;

    // CustomViews
    private FilterListView filterListView;
    private EditView editView;

    // 카메라 센서
    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;
    private Sensor mSensor;
    private double angleXY;
    private int direction = 0;
    private int priorDirection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //rate this app
        RateThisApp.onCreate(this);
        RateThisApp.showRateDialogIfNeeded(this);
        RateThisApp.Config config = new RateThisApp.Config(1, 2);
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
        filterListView = findViewById(R.id.filterListView);

        // 상단 버튼 세팅
        cameraSwitchingBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        cameraFlashBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        cameraTimerBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        settingBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsPrefActivity.class));
            }
        });


        //하단 버튼 세팅
        galleryBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent AlbumIntent = new Intent(Intent.ACTION_VIEW);

                AlbumIntent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);

                AlbumIntent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivity(AlbumIntent);
            }
        });

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

                        fCamera.takePicture();

                        cameraCaptureBtt.setClickable(true);
                    }
                }.start();
            }
        });

        editBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editView.changeState();
            }
        });

        // 초기 필터 세팅
        setCameraFilter(filterListView.getHorizontalAdapter().getDefaultFilter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_UI);
        SharedPreferences sp = this.getSharedPreferences(getString(R.string.gallery_pref),0);
        String path = sp.getString(getString(R.string.key_gallery_name),"Picture");
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

    public void setCameraFilter(final FCameraFilter filter){
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
        }
        else if (filter instanceof RetroFilter) {
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

    public FCameraCapture getFCameraCapture(){
        return fCameraCapture;
    }

    private class sensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {

            double x = event.values[0];
            double y = event.values[1];

            Animation rotAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_effect);


            angleXY = Math.atan2(x, y) * 180/Math.PI;

            if(angleXY >= -30 && angleXY <= 30) {
                direction = 0;
                if(priorDirection != direction) {
                    changeDirection(priorDirection, direction);
                    priorDirection = direction;
                }
                Log.d("asdf", "onSensorChanged: " + direction);
            }
            else if(angleXY >= 60 && angleXY <= 120) {
                direction = 3;
                if(priorDirection != direction) {
                    changeDirection(priorDirection, direction);
                    priorDirection = direction;
                }
            }
            else if(angleXY >= -120 && angleXY <= -60) {
                direction = 1;
                if(priorDirection != direction) {
                    changeDirection(priorDirection, direction);
                    priorDirection = direction;
                }
                Log.d("asdf", "onSensorChanged: " + direction);
            }
            else if(angleXY >= 150 || angleXY <= -150) {
                direction = 2;
                if(priorDirection != direction) {
                    changeDirection(priorDirection, direction);
                    priorDirection = direction;
                }
                Log.d("asdf", "onSensorChanged: " + direction);
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
        if(dmp == 1 || dmp == -3)
            to = from - 90;
        else if(dmp == -1 || dmp == 3)
            to = from + 90;
        else if(dmp == 2 || dmp == -2)
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
}
