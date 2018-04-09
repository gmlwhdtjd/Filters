package com.helloworld.bartender.FilterableCamera.Filters;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Size;

import com.helloworld.bartender.FilterableCamera.FCameraGLUtils;
import com.helloworld.bartender.FilterableCamera.FCameraRenderBuffer;
import com.helloworld.bartender.R;

import java.nio.FloatBuffer;

import static java.lang.Math.exp;

/**
 * Created by Song on 2018-02-23.
 */

public class RetroFilter extends FCameraFilter {

    private static int mPreviewProgram = 0;
    private static int mImageProgram = 0;

    private static int mPreviewProgram2 = 0;
    private static int mImageProgram2 = 0;

    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;

    private static FCameraRenderBuffer CAMERA_RENDER_BUF;
    private static final int BUF_ACTIVE_TEX_UNIT = GLES20.GL_TEXTURE8;

    protected int getPreviewProgramID() {
        return mPreviewProgram;
    }
    protected void setPreviewProgramID(int id) {
        mPreviewProgram = id;
    }

    protected int getImageProgramID() {
        return mImageProgram;
    }
    protected void setImageProgramID(int id) {
        mImageProgram = id;
    }

    public static void clear(Target target) {
        switch (target) {
            case PREVIEW:
                if (mPreviewProgram != 0)
                    GLES20.glDeleteProgram(mPreviewProgram);
                mPreviewProgram = 0;
                if (mPreviewProgram2 != 0)
                GLES20.glDeleteProgram(mPreviewProgram2);
                mPreviewProgram2 = 0;
                break;
            case IMAGE:
                if (mImageProgram != 0)
                    GLES20.glDeleteProgram(mImageProgram);
                mImageProgram = 0;
                if (mImageProgram2 != 0)
                    GLES20.glDeleteProgram(mImageProgram2);
                mImageProgram2 = 0;
                break;
        }

        if(CAMERA_RENDER_BUF != null) {
            CAMERA_RENDER_BUF.clear();
            CAMERA_RENDER_BUF = null;
        }
    }

    private float[] rgb = {0.0f, 0.0f, 0.0f};
    private float colorRatio;
    private float brightness;
    private float saturation;
    private float blur;
    private float aberration;
    private float focus;
    private float noiseSize;
    private float noiseIntensity;

    private float[] mask = new float[25];
    private float u[] = {-2.0f, -1.0f, 0.0f, 1.0f, 2.0f};
    private float v[] = {-2.0f, -1.0f, 0.0f, 1.0f, 2.0f};

    private void setMask(float sigma) {
        float result = 0.0f;

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                mask[(i * 5) + j] = (float) (exp(-((u[i] * u[i]) + (v[j] * v[j])) / (2.0f * sigma * sigma)) / (2.0f * 3.14159265358979323846f * sigma * sigma));
                result += mask[(i * 5) + j];
            }
        }
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                mask[(i * 5) + j] /= result;
            }
        }
    }

    public enum ValueType implements FCameraFilter.ValueType {
        COLOR_RATIO,
        RGB_R,
        RGB_G,
        RGB_B,
        BRIGHTNESS,
        SATURATION,
        BLUR,
        FOCUS,
        ABERRATION,
        NOISE_SIZE,
        NOISE_INTENSITY;

        @Override
        public String getPageName(Context context) {
            switch (this) {
                case COLOR_RATIO:
                case RGB_R:
                case RGB_G:
                case RGB_B:
                    return context.getString(R.string.OriginalFilter_Page_1);
                case BRIGHTNESS:
                case SATURATION:
                    return context.getString(R.string.OriginalFilter_Page_2);
                case BLUR:
                case FOCUS:
                    return context.getString(R.string.OriginalFilter_Page_3);
                case ABERRATION:
                    return context.getString(R.string.OriginalFilter_Page_4);
                case NOISE_SIZE:
                case NOISE_INTENSITY:
                    return context.getString(R.string.OriginalFilter_Page_5);
                default:
                    return "default";
            }
        }

        @Override
        public String getValueName(Context context) {
            switch (this) {
                case COLOR_RATIO:
                    return context.getString(R.string.OriginalFilter_COLOR_RATIO);
                case RGB_R:
                    return context.getString(R.string.OriginalFilter_RGB_R);
                case RGB_G:
                    return context.getString(R.string.OriginalFilter_RGB_G);
                case RGB_B:
                    return context.getString(R.string.OriginalFilter_RGB_B);
                case BRIGHTNESS:
                    return context.getString(R.string.OriginalFilter_BRIGHTNESS);
                case SATURATION:
                    return context.getString(R.string.OriginalFilter_SATURATION);
                case BLUR:
                    return context.getString(R.string.OriginalFilter_BLUR);
                case FOCUS:
                    return context.getString(R.string.OriginalFilter_FOCUS);
                case ABERRATION:
                    return context.getString(R.string.OriginalFilter_ABERRATION);
                case NOISE_SIZE:
                    return context.getString(R.string.OriginalFilter_NOISE_SIZE);
                case NOISE_INTENSITY:
                    return context.getString(R.string.OriginalFilter_NOISE_INTENSITY);
                default:
                    return "default";
            }
        }

    }

    @Override
    public void setValueWithType(FCameraFilter.ValueType type, int value) {
        if (type instanceof ValueType) {
            ValueType valueType = (ValueType) type;
            switch (valueType) {
                case COLOR_RATIO:
                    colorRatio = (float) value / 200;
                    break;
                case RGB_R:
                    rgb[0] = (float) value / 255;
                    break;
                case RGB_G:
                    rgb[1] = (float) value / 255;
                    break;
                case RGB_B:
                    rgb[2] = (float) value / 255;
                    break;
                case BRIGHTNESS:
                    brightness = (float) value / 100;
                    break;
                case SATURATION:
                    saturation = (float) value / 100;
                    break;
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
            throw new IllegalArgumentException("type is not RetroFilter.ValueType");
    }

    @Override
    public int getValueWithType(FCameraFilter.ValueType type) {
        if (type instanceof ValueType) {
            ValueType valueType = (ValueType) type;
            switch (valueType) {
                case COLOR_RATIO:
                    return (int) (colorRatio * 200);
                case RGB_R:
                    return (int) (rgb[0] * 255);
                case RGB_G:
                    return (int) (rgb[1] * 255);
                case RGB_B:
                    return (int) (rgb[2] * 255);
                case BRIGHTNESS:
                    return (int) (brightness * 100);
                case SATURATION:
                    return (int) (saturation * 100);
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
            throw new IllegalArgumentException("type is not RetroFilter.ValueType");
    }

    private int getProgram2(Target target) {
        switch (target) {
            case PREVIEW:
                if (mPreviewProgram2 == 0)
                    mPreviewProgram2 = FCameraGLUtils.buildProgram(mContext, R.raw.filter_default_vshader, R.raw.filter_retro_blur_fshader);
                return mPreviewProgram2;
            case IMAGE:
                if (mImageProgram2 == 0)
                    mImageProgram2 = FCameraGLUtils.buildProgram(mContext, R.raw.filter_default_vshader, R.raw.filter_retro_blur_fshader);
                return mImageProgram2;
            default:
                return 0;
        }
    }

    private void loadBuffer(Size viewSize) {
        if (vertexBuffer == null)
            vertexBuffer = FCameraGLUtils.getDefaultVertexBuffers(0, FCameraGLUtils.CAMERA_FLIP_NON);
        if (texCoordBuffer == null)
            texCoordBuffer = FCameraGLUtils.getDefaultmTexCoordBuffers(0,FCameraGLUtils.CAMERA_FLIP_NON);

        // Create camera render buffer
        if (CAMERA_RENDER_BUF == null) {
            CAMERA_RENDER_BUF = new FCameraRenderBuffer(viewSize.getWidth(), viewSize.getHeight(), BUF_ACTIVE_TEX_UNIT);
        }
        else if(CAMERA_RENDER_BUF.getWidth() != viewSize.getWidth() ||
                CAMERA_RENDER_BUF.getHeight() != viewSize.getHeight()) {
            CAMERA_RENDER_BUF.clear();
            CAMERA_RENDER_BUF = new FCameraRenderBuffer(viewSize.getWidth(), viewSize.getHeight(), BUF_ACTIVE_TEX_UNIT);
        }
    }

    private float[] nl = {(float) Math.random(), (float) Math.random(), (float) Math.random(), 0.0f};
    private final long START_TIME = System.currentTimeMillis();

    public RetroFilter(Context context, Integer id) {
        this(context, id,
                "Default",
                0, 255, 255, 255,
                0, 0,
                0, 0,
                0,
                0, 0);
    }

    public RetroFilter(Context context, Integer id, String name,
                       int colorRatio, int red, int green, int blue,
                       int brightness, int saturation,
                       int blur, int focus,
                       int aberration,
                       int noiseSize, int noiseIntensity) {
        super(context, R.raw.filter_default_vshader, R.raw.filter_retro_fshader, id);

        setName(name);
        setValueWithType(ValueType.RGB_R, red);
        setValueWithType(ValueType.RGB_G, green);
        setValueWithType(ValueType.RGB_B, blue);
        setValueWithType(ValueType.COLOR_RATIO, colorRatio);
        setValueWithType(ValueType.BRIGHTNESS, brightness);
        setValueWithType(ValueType.SATURATION, saturation);
        setValueWithType(ValueType.BLUR, blur);
        setValueWithType(ValueType.FOCUS, focus);
        setValueWithType(ValueType.ABERRATION, aberration);
        setValueWithType(ValueType.NOISE_SIZE, noiseSize);
        setValueWithType(ValueType.NOISE_INTENSITY, noiseIntensity);

    }

    @Override
    public void onDraw(int program, Target target, int textureId, Size viewSize) {

        loadBuffer(viewSize);

        int texture2Id = FCameraGLUtils.loadTexture(mContext, R.drawable.noise, new int[2]);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "sTexture"), 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2Id);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "sNoiseTexture"), 1);

        int iResolutionLocation = GLES20.glGetUniformLocation(program, "iResolution");
        GLES20.glUniform3fv(iResolutionLocation, 1, FloatBuffer.wrap(new float[]{(float) viewSize.getWidth(), (float) viewSize.getHeight(), 1.0f}));

        int noiseLevelLocation = GLES20.glGetUniformLocation(program, "noiseLevel");
        GLES20.glUniform3fv(noiseLevelLocation, 1, FloatBuffer.wrap(new float[]{(float) Math.random(), (float) Math.random(), (float) Math.random()}));


        float time = ((float) (System.currentTimeMillis() - START_TIME)) / 1000.0f;
        int iGlobalTimeLocation = GLES20.glGetUniformLocation(program, "iGlobalTime");
        GLES20.glUniform1f(iGlobalTimeLocation, time);

        int rgbLocation = GLES20.glGetUniformLocation(program, "variables.rgb");
        GLES20.glUniform3fv(rgbLocation, 1, FloatBuffer.wrap(rgb));

        int colorRatioLocation = GLES20.glGetUniformLocation(program, "variables.colorRatio");
        GLES20.glUniform1f(colorRatioLocation, colorRatio);

        int brightnessLocation = GLES20.glGetUniformLocation(program, "variables.brightness");
        GLES20.glUniform1f(brightnessLocation, brightness);

        int saturationLocation = GLES20.glGetUniformLocation(program, "variables.saturation");
        GLES20.glUniform1f(saturationLocation, saturation);

        int blurLocation = GLES20.glGetUniformLocation(program, "variables.blur");
        GLES20.glUniform1f(blurLocation, blur);

        int abeLocation = GLES20.glGetUniformLocation(program, "variables.aberration");
        GLES20.glUniform1f(abeLocation, aberration);

        int nsLocation = GLES20.glGetUniformLocation(program, "variables.noiseSize");
        GLES20.glUniform1f(nsLocation, noiseSize);

        int niLocation = GLES20.glGetUniformLocation(program, "variables.noiseIntensity");
        GLES20.glUniform1f(niLocation, noiseIntensity);

        if (CAMERA_RENDER_BUF != null) {
            // Render to texture
            CAMERA_RENDER_BUF.bind();
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            CAMERA_RENDER_BUF.unbind();
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            int program2 = getProgram2(target);

            ////////////////////////////////////

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(program2);

            int ph = GLES20.glGetAttribLocation(program2, "vPosition");
            int tch = GLES20.glGetAttribLocation(program2, "vTexCoord");

            GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4 * 2, vertexBuffer);
            GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4 * 2, texCoordBuffer);

            GLES20.glEnableVertexAttribArray(ph);
            GLES20.glEnableVertexAttribArray(tch);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, CAMERA_RENDER_BUF.getTexId());
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program2, "sTexture"), 0);

            int maskLocation = GLES20.glGetUniformLocation(program2, "mask");
            GLES20.glUniform1fv(maskLocation, 25, FloatBuffer.wrap(mask));

            int focusLocation = GLES20.glGetUniformLocation(program2, "focus");
            GLES20.glUniform1f(focusLocation, focus);

            int iResolutionLocation2 = GLES20.glGetUniformLocation(program2, "iResolution");
            GLES20.glUniform3fv(iResolutionLocation2, 1, FloatBuffer.wrap(new float[]{(float) viewSize.getWidth(), (float) viewSize.getHeight(), 1.0f}));

        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
