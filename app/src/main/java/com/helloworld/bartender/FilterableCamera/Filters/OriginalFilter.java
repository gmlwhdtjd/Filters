package com.helloworld.bartender.FilterableCamera.Filters;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Size;

import com.helloworld.bartender.R;

import java.nio.FloatBuffer;

import static java.lang.Math.exp;

/**
 * Created by Song on 2018-02-23.
 */

public class OriginalFilter extends FCameraFilter {
    private float [] rgb = {0.0f, 0.0f, 0.0f};
    private long id;
    private String filter_name;
    private float blur;
    private float aberration;
    private float focus;
    private float noiseSize;
    private float noiseIntensity;



    private String type;

    private float[] mask = new float[49];
    private float u[] = { -3.0f, -2.0f, -1.0f, 0.0f, 1.0f, 2.0f, 3.0f };
    private float v[] = { -3.0f, -2.0f, -1.0f, 0.0f, 1.0f, 2.0f, 3.0f };

    private void setMask(float sigma) {
        float result = 0.0f;
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                mask[(i*7)+j] = (float) (exp(-((u[i]*u[i]) + (v[j]*v[j])) / (2.0f * sigma*sigma)) / (2.0f * 3.14159265358979323846f *sigma*sigma));
                result += mask[(i*7)+j];
            }
        }
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                mask[(i*7)+j] /= result;
            }
        }
    }

    public float getBlur() {
        return blur;
    }
    public float getAberration() {
        return aberration;
    }
    public float getFocus() {
        return focus;
    }
    public float getNoiseSize() {
        return noiseSize;
    }
    public float getNoiseIntensity() {
        return noiseIntensity;
    }
    public long getId() {return id;}
    public String getFilter_name() {return filter_name;}
    public String getType() {
        return type;
    }
    public void setBlur(float num) {
        blur = num;
        setMask(blur);
    }


    public void setType(String type) {
        this.type = type;
    }
    public void setAberration(float num) {
        aberration = num;
    }
    public void setFocus(float num) {
        focus = num;
    }
    public void setNoiseSize(float num) {
            noiseSize = num;
        }
    public void setNoiseIntensity(float num) {
            noiseIntensity = num;
        }
    public void setId(long id) {this.id = id;}
    public void setFilter_name(String filter_name) {this.filter_name = filter_name;}

    private float[] nl = {0.1f, 0.1f, 0.1f, 0.0f};
    private final long START_TIME = System.currentTimeMillis();

    public OriginalFilter(){}

    public OriginalFilter(Context context) {
        super(context, R.raw.filter_vertex_shader, R.raw.filter_fragment_shader);

        setFilter_name("default filter");
        setBlur(0.1f);
        setAberration(0.0f);
        setFocus(1.0f);
        setNoiseSize(1.0f);
        setNoiseIntensity(1.0f);
        setType("originalType");
    }

    public OriginalFilter(String type,String filter_name, float blur, float focus, float aberration, float noiseSize, float noiseIntensity) {
        setFilter_name(filter_name);
        setBlur(blur);
        setAberration(focus);
        setFocus(aberration);
        setNoiseSize(noiseSize);
        setNoiseIntensity(noiseIntensity);
        setType(type);
    }

    @Override
    public void onDraw(int program, Size viewSize) {
        int iResolutionLocation = GLES20.glGetUniformLocation(program, "iResolution");
        GLES20.glUniform3fv(iResolutionLocation, 1, FloatBuffer.wrap(new float[]{(float) viewSize.getWidth(), (float) viewSize.getWidth(), 1.0f}));

        int noiseLevelLocation = GLES20.glGetUniformLocation(program, "noiseLevel");
        GLES20.glUniform4fv(noiseLevelLocation, 1, FloatBuffer.wrap(nl));

        float time = ((float) (System.currentTimeMillis() - START_TIME)) / 1000.0f;
        int iGlobalTimeLocation = GLES20.glGetUniformLocation(program, "iGlobalTime");
        GLES20.glUniform1f(iGlobalTimeLocation, time);

        int rgbLocation = GLES20.glGetUniformLocation(program, "variables.rgb");
        GLES20.glUniform3fv(rgbLocation, 1, FloatBuffer.wrap(rgb));

        int blurLocation = GLES20.glGetUniformLocation(program, "variables.blur");
        GLES20.glUniform1f(blurLocation, blur);

        int abeLocation = GLES20.glGetUniformLocation(program, "variables.aberration");
        GLES20.glUniform1f(abeLocation, aberration);

        int focusLocation = GLES20.glGetUniformLocation(program, "variables.focus");
        GLES20.glUniform1f(focusLocation, focus);

        int nsLocation = GLES20.glGetUniformLocation(program, "variables.noiseSize");
        GLES20.glUniform1f(nsLocation, noiseSize);

        int niLocation = GLES20.glGetUniformLocation(program, "variables.noiseIntensity");
        GLES20.glUniform1f(niLocation, noiseIntensity);

        int maskLocation = GLES20.glGetUniformLocation(program, "mask");
        GLES20.glUniform1fv(maskLocation, 49, FloatBuffer.wrap(mask));

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

}
