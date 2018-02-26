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
    private float blur;
    private float aberration;
    private float focus;
    private float noiseSize;
    private float noiseIntensity;

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

    public void setBlur(float num) {
        blur = num;
        setMask(blur);
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

    private float[] nl = {0.1f, 0.1f, 0.1f, 0.0f};
    private final long START_TIME = System.currentTimeMillis();

    public OriginalFilter(Context context) {
        super(context, R.raw.filter_vertex_shader, R.raw.filter_fragment_shader);

        setBlur(1.0f);
        setAberration(0.0f);
        setFocus(0.0f);
        setNoiseSize(1.0f);
        setNoiseIntensity(0.0f);
    }

    @Override
    public void onDraw(int program, Size viewSize) {

        int iResolutionLocation = GLES20.glGetUniformLocation(program, "iResolution");
        GLES20.glUniform3fv(iResolutionLocation, 1, FloatBuffer.wrap(new float[]{(float) viewSize.getWidth(), (float) viewSize.getHeight(), 1.0f}));

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
