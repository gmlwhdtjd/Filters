package com.helloworld.bartender.FilterableCamera;

import android.content.Context;
import android.opengl.GLES20;

import com.helloworld.bartender.R;

import java.nio.FloatBuffer;

import static java.lang.Math.exp;

/**
 * Created by Song on 2018-02-23.
 */

public class OriginalFilter extends FCameraFilter {
    public static class FilterVar {
        public float [] rgb = {0.0f, 0.0f, 0.0f};
        private static float blur = 0.1f;
        private static float aberration = 0.0f;
        private static float[] mask = new float[49];
        private static float u[] = { -3.0f, -2.0f, -1.0f, 0.0f, 1.0f, 2.0f, 3.0f };
        private static float v[] = { -3.0f, -2.0f, -1.0f, 0.0f, 1.0f, 2.0f, 3.0f };

        public static float getBlur() {
            return blur;
        }

        public static float getAberration() {
            return aberration;
        }

        public static float getFocus() {
            return focus;
        }

        public static float getNoiseSize() {
            return noiseSize;
        }

        public static float getNoiseIntensity() {
            return noiseIntensity;
        }

        private static float focus = 1.0f;
        private static float noiseSize = 1.0f;
        private static float noiseIntensity = 1.0f;

        public static void setBlur(float num) {
            blur = num;
            setMask(blur);
        }
        public static void setMask(float sigma) {
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
        public static void setAberration(float num) {
            aberration = num;
        }
        public static void setFocus(float num) {
            focus = num;
        }
        public static void setNoiseSize(float num) {
            noiseSize = num;
        }
        public static void setNoiseIntensity(float num) {
            noiseIntensity = num;
        }

    }

    private int mProgram;
    private Context mContext;

    private float[] nl = {0.1f, 0.1f, 0.1f, 0.0f};
    private final long START_TIME = System.currentTimeMillis();

    private FilterVar filtervar = new FilterVar();

    public OriginalFilter(Context context) {
        super(context);

        // Build shaders
        mContext = context;
        FilterVar.setMask(filtervar.blur);
    }

    @Override
    public void onDraw(int Width, int Height) {
        int iResolutionLocation = GLES20.glGetUniformLocation(mProgram, "iResolution");
        GLES20.glUniform3fv(iResolutionLocation, 1, FloatBuffer.wrap(new float[]{(float) Width, (float) Height, 1.0f}));

        int noiseLevelLocation = GLES20.glGetUniformLocation(mProgram, "noiseLevel");
        GLES20.glUniform4fv(noiseLevelLocation, 1, FloatBuffer.wrap(nl));

        float time = ((float) (System.currentTimeMillis() - START_TIME)) / 1000.0f;
        int iGlobalTimeLocation = GLES20.glGetUniformLocation(mProgram, "iGlobalTime");
        GLES20.glUniform1f(iGlobalTimeLocation, time);

        int rgbLocation = GLES20.glGetUniformLocation(mProgram, "variables.rgb");
        GLES20.glUniform3fv(rgbLocation, 1, FloatBuffer.wrap(filtervar.rgb));

        int blurLocation = GLES20.glGetUniformLocation(mProgram, "variables.blur");
        GLES20.glUniform1f(blurLocation, filtervar.blur);

        int abeLocation = GLES20.glGetUniformLocation(mProgram, "variables.aberration");
        GLES20.glUniform1f(abeLocation, filtervar.aberration);

        int focusLocation = GLES20.glGetUniformLocation(mProgram, "variables.focus");
        GLES20.glUniform1f(focusLocation, filtervar.focus);

        int nsLocation = GLES20.glGetUniformLocation(mProgram, "variables.noiseSize");
        GLES20.glUniform1f(nsLocation, filtervar.noiseSize);

        int niLocation = GLES20.glGetUniformLocation(mProgram, "variables.noiseIntensity");
        GLES20.glUniform1f(niLocation, filtervar.noiseIntensity);

        int maskLocation = GLES20.glGetUniformLocation(mProgram, "mask");
        GLES20.glUniform1fv(maskLocation, 49, FloatBuffer.wrap(filtervar.mask));

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

}
