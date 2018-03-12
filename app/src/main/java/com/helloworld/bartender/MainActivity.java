package com.helloworld.bartender;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.helloworld.bartender.FilterableCamera.FCamera;
import com.helloworld.bartender.FilterableCamera.FCameraCapturer;
import com.helloworld.bartender.FilterableCamera.FCameraView;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;


//TODO: back 키 이벤트 처리하기, 필터값 수정,삭제,저장,적용, 필터 아이콘 클릭시 체크 유지

public class MainActivity extends AppCompatActivity {

    // 카메라 관련
    private FCameraView fCameraView;
    private FCameraCapturer fCameraCapturer;
    private FCamera fCamera;

    private int cameraFlashState = 0;
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

    // EditView
    private EditView editView;

    FCameraFilter cameraFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 카메라 관련
        fCameraView = findViewById(R.id.cameraView);
        fCameraCapturer = new FCameraCapturer(this);
        fCamera = new FCamera(this, getLifecycle(), fCameraView, fCameraCapturer);

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

        // EditView
        editView = findViewById(R.id.editView);

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

        // TODO : settingActivity
        settingBtt.setOnClickListener(null);


        //하단 버튼 세팅
        galleryBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent AlbumIntent = new Intent(Intent.ACTION_VIEW);

                AlbumIntent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);

                AlbumIntent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivity(AlbumIntent);

//              dbHelper.saveFilter(new Item("last",0.5f,0.5f,0.5f,0.5f,0.5f));
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
        cameraFilter = new OriginalFilter(this, 1);
        setCameraFilter(cameraFilter);
    }

    public void setCameraFilter(final FCameraFilter filter){
        fCameraView.setFilter(filter);
        fCameraCapturer.setFilter(filter);
        editView.setFilter(filter);
        changeCaptureInnerColor(filter);
      
        editView.setOnSaveListener(new EditView.OnSaveListener() {
            @Override
            public void onSaved() {
                changeCaptureInnerColor(filter);
            }
        });
    }

    public void changeCaptureInnerColor(FCameraFilter filter) {
        if (filter instanceof OriginalFilter) {
            ImageView cameraCaptureInnerImg = findViewById(R.id.cameraCaptureInnerImg);
            float[] hsb = new float[3];
            Color.RGBToHSV(
                    filter.getValueWithType(OriginalFilter.ValueType.RGB_R),
                    filter.getValueWithType(OriginalFilter.ValueType.RGB_G),
                    filter.getValueWithType(OriginalFilter.ValueType.RGB_B),
                    hsb);
            hsb[1] = hsb[1] < 0.2f ? hsb[1] : 0.2f;
            hsb[2] = 0.90f;
            cameraCaptureInnerImg.setColorFilter(Color.HSVToColor(200, hsb));
        }
    }
}
