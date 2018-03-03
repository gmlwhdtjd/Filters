package com.helloworld.bartender.FilterableCamera.Filters;

import android.content.Context;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.util.Size;

import com.helloworld.bartender.R;

import java.nio.FloatBuffer;
import java.util.Random;

import static java.lang.Math.exp;

/**
 * Created by Song on 2018-02-23.
 */

public class OriginalFilter extends FCameraFilter {
    private float[] rgb = {0.0f, 0.0f, 0.0f};
    private float blur;
    private float aberration;
    private float focus;
    private float noiseSize;
    private float noiseIntensity;

    private float[] mask = new float[25];
    private float u[] = { -2.0f, -1.0f, 0.0f, 1.0f, 2.0f};
    private float v[] = { -2.0f, -1.0f, 0.0f, 1.0f, 2.0f};

    private void setMask(float sigma) {
        float result = 0.0f;

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                mask[(i*5)+j] = (float) (exp(-((u[i]*u[i]) + (v[j]*v[j])) / (2.0f * sigma*sigma)) / (2.0f * 3.14159265358979323846f *sigma*sigma));
                result += mask[(i*5)+j];
            }
        }
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                mask[(i*5)+j] /= result;
            }
        }
    }

    public enum ValueType implements FCameraFilter.ValueType {
        BLUR,
        FOCUS,
        ABERRATION,
        NOISE_SIZE,
        NOISE_INTENSITY;

        @Override
        public int getPageNumber() {
            switch (this) {
                case BLUR:
                case FOCUS:
                    return 0;
                case ABERRATION:
                    return 1;
                case NOISE_SIZE:
                case NOISE_INTENSITY:
                    return 2;
                default:
                    return -1;
            }
        }
    }

    @Override
    public void setValueWithType(FCameraFilter.ValueType type, int value) {
        if (type instanceof ValueType) {
            ValueType valueType = (ValueType) type;
            switch (valueType) {
                case BLUR:
                    blur = (float) value / 25 + 0.0001f;
                    setMask(blur);
                    break;
                case FOCUS:
                    focus = (float) value / 100;
                    break;
                case ABERRATION:
                    aberration = (float) value / 100;
                    break;
                case NOISE_SIZE:
                    noiseSize = (float) value / 100;
                    break;
                case NOISE_INTENSITY:
                    noiseIntensity = (float) value / 100;
                    break;
            }
        } else
            throw new IllegalArgumentException("type is not OriginalFilter.ValueType");
    }

    @Override
    public int getValueWithType(FCameraFilter.ValueType type) {
        if (type instanceof ValueType) {
            ValueType valueType = (ValueType) type;
            switch (valueType) {
                case BLUR:
                    return (int) (blur * 25);
                case FOCUS:
                    return (int) (focus * 100);
                case ABERRATION:
                    return (int) (aberration * 100);
                case NOISE_SIZE:
                    return (int) (noiseSize * 100);
                case NOISE_INTENSITY:
                    return (int) (noiseIntensity * 100);
                default:
                    return 0;
            }
        } else
            throw new IllegalArgumentException("type is not OriginalFilter.ValueType");
    }
  
    private float[] nl = {(float)Math.random(), (float)Math.random(), (float)Math.random(), 0.0f};
    private final long START_TIME = System.currentTimeMillis();

    public OriginalFilter(Context context, Integer id) {
        this(context, id, "default", 0, 0, 0, 0, 0);
    }

    public OriginalFilter(Context context, Integer id, String name, int blur, int focus, int aberration, int noiseSize, int noiseIntensity) {
        super(context, R.raw.filter_vertex_shader, R.raw.filter_fragment_shader, id);
        
      setName(name);
        setValueWithType(ValueType.BLUR, blur);
        setValueWithType(ValueType.FOCUS, focus);
        setValueWithType(ValueType.ABERRATION, aberration);
        setValueWithType(ValueType.NOISE_SIZE, noiseSize);
        setValueWithType(ValueType.NOISE_INTENSITY, noiseIntensity);
    }

    @Override
    public void onDraw(int program, Size viewSize) {

        int iResolutionLocation = GLES20.glGetUniformLocation(program, "iResolution");
        GLES20.glUniform3fv(iResolutionLocation, 1, FloatBuffer.wrap(new float[]{(float) viewSize.getWidth(), (float) viewSize.getHeight(), 1.0f}));

        int noiseLevelLocation = GLES20.glGetUniformLocation(program, "noiseLevel");
        GLES20.glUniform3fv(noiseLevelLocation, 1, FloatBuffer.wrap(new float[]{(float) Math.random(), (float) Math.random(), (float) Math.random()}));


        float time = ((float) (System.currentTimeMillis() - START_TIME)) / 1000.0f;
        int iGlobalTimeLocation = GLES20.glGetUniformLocation(program, "iGlobalTime");
        GLES20.glUniform1f(iGlobalTimeLocation, time);

        int rgbLocation = GLES20.glGetUniformLocation(program, "variables.rgb");
        GLES20.glUniform3fv(rgbLocation, 1, FloatBuffer.wrap(rgb));

        int blurLocation = GLES20.glGetUniformLocation(program, "variables.BLUR");
        GLES20.glUniform1f(blurLocation, blur);

        int abeLocation = GLES20.glGetUniformLocation(program, "variables.ABERRATION");
        GLES20.glUniform1f(abeLocation, aberration);

        int focusLocation = GLES20.glGetUniformLocation(program, "variables.FOCUS");
        GLES20.glUniform1f(focusLocation, focus);

        int nsLocation = GLES20.glGetUniformLocation(program, "variables.NOISE_SIZE");
        GLES20.glUniform1f(nsLocation, noiseSize);

        int niLocation = GLES20.glGetUniformLocation(program, "variables.NOISE_INTENSITY");
        GLES20.glUniform1f(niLocation, noiseIntensity);

        int maskLocation = GLES20.glGetUniformLocation(program, "mask");
        GLES20.glUniform1fv(maskLocation, 25, FloatBuffer.wrap(mask));

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

}
