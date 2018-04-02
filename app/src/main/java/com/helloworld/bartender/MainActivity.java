package com.helloworld.bartender;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.helloworld.bartender.Database.DatabaseHelper;
import com.helloworld.bartender.Edit.EditView;
import com.helloworld.bartender.FilterList.FilterListView;
import com.helloworld.bartender.FilterableCamera.FCamera;
import com.helloworld.bartender.FilterableCamera.FCameraCapture;
import com.helloworld.bartender.FilterableCamera.FCameraPreview;
import com.helloworld.bartender.FilterableCamera.Filters.DefaultFilter;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;
import com.kobakei.ratethisapp.RateThisApp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//TODO: back 키 이벤트 처리하기, 필터값 수정,삭제,저장,적용, 필터 아이콘 클릭시 체크 유지

public class MainActivity extends AppCompatActivity {

    // 카메라 관련
    private FCameraPreview fCameraPreview;
    private FCameraCapture fCameraCapture;
    private FCamera fCamera;

    private int cameraTimerState = 0;

    // 카메라 캡쳐 관련
    private TextView timerTextView;
    private ImageView captureEffectImg;

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

        fCamera = new FCamera(this, getLifecycle(), fCameraPreview, fCameraCapture);

        // 카메라 캡쳐 관련
        timerTextView = findViewById(R.id.timerNumberText);
        captureEffectImg = findViewById(R.id.captureEffectImg);

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

                        Animation captuer = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.capture_effect);
                        captureEffectImg.startAnimation(captuer);

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
        if (filter instanceof DefaultFilter) {
            ImageView cameraCaptureInnerImg = findViewById(R.id.cameraCaptureInnerImg);
            float[] hsv = new float[]{0.0f, 0.0f, 0.9f};
            cameraCaptureInnerImg.setColorFilter(Color.HSVToColor(200, hsv));
        }
        else if (filter instanceof OriginalFilter) {
            ImageView cameraCaptureInnerImg = findViewById(R.id.cameraCaptureInnerImg);
            float[] hsv = new float[3];
            Color.RGBToHSV(
                    filter.getValueWithType(OriginalFilter.ValueType.RGB_R),
                    filter.getValueWithType(OriginalFilter.ValueType.RGB_G),
                    filter.getValueWithType(OriginalFilter.ValueType.RGB_B),
                    hsv);
            hsv[1] = hsv[1] < 0.2f ? hsv[1] : 0.2f;
            hsv[2] = 0.90f;
            cameraCaptureInnerImg.setColorFilter(Color.HSVToColor(200, hsv));
        }
    }

    public FCameraCapture getFCameraCapture(){
        return fCameraCapture;
    }
}
