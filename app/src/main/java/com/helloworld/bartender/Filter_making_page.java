package com.helloworld.bartender;


import android.graphics.BlurMaskFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import com.helloworld.bartender.FilterableCamera.FCamera;
import com.helloworld.bartender.FilterableCamera.FCameraRenderer;
import com.helloworld.bartender.FilterableCamera.OriginalFilter;

public class Filter_making_page extends AppCompatActivity {

    private SeekBar sbBlur;
    private SeekBar sbFocus;
    private SeekBar sbAberation;
    private SeekBar sbNoiseSize;
    private SeekBar sbNoiseIntensity;

    private TextView txtBlur;
    private TextView txtFocus;
    private TextView txtAberation;
    private TextView txtNoiseSize;
    private TextView txtNoiseIntensity;

    public float BlurVal;
    public float FocusVal;
    public float AberationVal;
    public float NoiseSizeVal;
    public float NoiseIntensityVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_making_page);

        sbBlur = (SeekBar) findViewById(R.id.sbBlur);
        sbFocus = (SeekBar) findViewById(R.id.sbFocus);
        sbAberation = (SeekBar) findViewById(R.id.sbAberation);
        sbNoiseSize = (SeekBar) findViewById(R.id.sbNoiseSize);
        sbNoiseIntensity = (SeekBar) findViewById(R.id.sbNoiseIntensity);

        txtBlur = (TextView)findViewById(R.id.blurVal);
        txtFocus = (TextView)findViewById(R.id.focusVal);
        txtAberation = (TextView)findViewById(R.id.aberationVal);
        txtNoiseSize = (TextView)findViewById(R.id.noiseSizeVal);
        txtNoiseIntensity = (TextView)findViewById(R.id.noiseIntensityVal);

        sbBlur.setProgress(Math.round(OriginalFilter.FilterVar.getBlur()*100));
        txtBlur.setText(Float.toString(OriginalFilter.FilterVar.getBlur()));

        sbFocus.setProgress(Math.round(OriginalFilter.FilterVar.getFocus()*100));
        txtFocus.setText(Float.toString(OriginalFilter.FilterVar.getFocus()));

        sbAberation.setProgress(Math.round(OriginalFilter.FilterVar.getAberration()*100));
        txtAberation.setText(Float.toString(OriginalFilter.FilterVar.getAberration()));

        sbNoiseSize.setProgress(Math.round(OriginalFilter.FilterVar.getNoiseSize()*100-25));
        txtNoiseSize.setText(Float.toString(OriginalFilter.FilterVar.getNoiseSize()));

        sbNoiseIntensity.setProgress(Math.round(OriginalFilter.FilterVar.getNoiseIntensity()*100));
        txtNoiseIntensity.setText(Float.toString(OriginalFilter.FilterVar.getNoiseIntensity()));

        sbBlur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                BlurVal = (float)seekBar.getProgress()/100;
                OriginalFilter.FilterVar.setBlur(BlurVal);
                update(txtBlur,BlurVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                BlurVal = (float)seekBar.getProgress()/100;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                BlurVal = (float)seekBar.getProgress()/100;
            }
        });

        sbFocus.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                FocusVal = (float)seekBar.getProgress()/100;
                OriginalFilter.FilterVar.setFocus(FocusVal);
                update(txtFocus,FocusVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                FocusVal = (float)seekBar.getProgress()/100;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                FocusVal = (float)seekBar.getProgress()/100;
            }
        });

        sbAberation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                AberationVal = (float)seekBar.getProgress()/100;
                OriginalFilter.FilterVar.setAberration(AberationVal);
                update(txtAberation,AberationVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                AberationVal = (float)seekBar.getProgress()/100;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AberationVal = (float)seekBar.getProgress()/100;
            }
        });

        sbNoiseSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                NoiseSizeVal = (float)(seekBar.getProgress()+25)/100;
                OriginalFilter.FilterVar.setNoiseSize(NoiseSizeVal);
                update(txtNoiseSize,NoiseSizeVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                NoiseSizeVal = (float)(seekBar.getProgress()+25)/100;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                NoiseSizeVal = (float)(seekBar.getProgress()+25)/100;
            }
        });

        sbNoiseIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                NoiseIntensityVal = (float)seekBar.getProgress()/100;
                OriginalFilter.FilterVar.setNoiseIntensity(NoiseIntensityVal);
                update(txtNoiseIntensity,NoiseIntensityVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                NoiseIntensityVal = (float)seekBar.getProgress()/100;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
               NoiseIntensityVal = (float)seekBar.getProgress()/100;
            }
        });

 }

 public void update(TextView txt,float num){
        txt.setText(new StringBuilder().append(num));
 }


}
