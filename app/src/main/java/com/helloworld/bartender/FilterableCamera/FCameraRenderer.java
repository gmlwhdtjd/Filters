package com.helloworld.bartender.FilterableCamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by huijonglee on 2018. 1. 30..
 */
public class FCameraRenderer {
    public static class FilterVar {
        public float [] rgb = {0.0f, 0.0f, 0.0f};
        private static float blur = 0.0f;
        private static float aberration = 0.0f;

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

    private static final String TAG = "FCameraRenderer";
    private Context mContext;

    private int[] mTextureIds;

    //shader variable
    private int[] iResolution;
    private float[] nl = {0.1f, 0.1f, 0.1f, 0.0f};
    private final long START_TIME = System.currentTimeMillis();
    private FilterVar filtervar = new FilterVar();
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoordBuffer;

    private int mProgram;

    private boolean initStatus = false;

    FCameraRenderer(Context context) {
        mContext = context;
    }

    void initRender(int vertexShaderID, int fragmentShaderID) {
        setBuffers(0, flip_NON);

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        mProgram = buildProgram(vertexShaderID, fragmentShaderID);
    }

    SurfaceTexture getInputSurfaceTexture() {
        mTextureIds = new int[1];
        GLES20.glGenTextures(1, mTextureIds, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureIds[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        initStatus = true;
        return new SurfaceTexture(mTextureIds[0]);
    }

    static final int flip_NON = 0x00; // non flipping
    static final int flip_UD = 0x01; // Up Down flipping
    static final int flip_RL = 0x10; // Right Left flipping

    private void swapElement(float[] a, int i, int j) {
        float tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    /**
     * set vertex and texcoord buffer
     * this method must be called before onDraw
     *
     * @param cameraOrientation it should be 0, 90, 180, 270
     * @param flipping use flip_NON, flip_UD, flip_RL
     */
    void setBuffers(int cameraOrientation, int flipping) {
        if ( mVertexBuffer != null)
            mVertexBuffer.clear();

        float[] vertexBufferData
                = {1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f};
        mVertexBuffer
                = ByteBuffer.allocateDirect(8 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.put(vertexBufferData);
        mVertexBuffer.position(0);

        if ( mTexCoordBuffer != null)
            mTexCoordBuffer.clear();

        float[] texCoordData;
        switch (cameraOrientation){
            case 0:
                texCoordData = new float[] {1.0f, 1.0f,  0.0f, 1.0f,  1.0f, 0.0f,  0.0f, 0.0f};
                break;
            case 90:
                texCoordData = new float[] {1.0f, 0.0f,  1.0f, 1.0f,  0.0f, 0.0f,  0.0f, 1.0f};
                break;
            case 180:
                texCoordData = new float[] {0.0f, 0.0f,  1.0f, 0.0f,  0.0f, 1.0f,  1.0f, 1.0f};
                break;
            case 270:
                texCoordData = new float[] {0.0f, 1.0f,  0.0f, 0.0f,  1.0f, 1.0f,  1.0f, 0.0f};
                break;
            default:
                throw new RuntimeException("Invalid value of \"cameraOrientation\"");
        }

        if ((flipping & flip_UD) == flip_UD){
            swapElement(texCoordData, 0, 4);
            swapElement(texCoordData, 1, 5);
            swapElement(texCoordData, 2, 6);
            swapElement(texCoordData, 3, 7);
        }

        if ((flipping & flip_RL) == flip_RL){
            swapElement(texCoordData, 0, 2);
            swapElement(texCoordData, 1, 3);
            swapElement(texCoordData, 4, 6);
            swapElement(texCoordData, 5, 7);
        }

        mTexCoordBuffer
                = ByteBuffer.allocateDirect(8 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTexCoordBuffer.put(texCoordData);
        mTexCoordBuffer.position(0);
    }

    void setViewSize(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        iResolution = new int[2];
        iResolution[0] = -width;
        iResolution[1] = -height;
    }

    void onDraw() {
        if (initStatus) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgram);

            // TODO : Rename
            int ph = GLES20.glGetAttribLocation(mProgram, "vPosition");
            int tch = GLES20.glGetAttribLocation(mProgram, "vTexCoord");

            GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4 * 2, mVertexBuffer);
            GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4 * 2, mTexCoordBuffer);

            GLES20.glEnableVertexAttribArray(ph);
            GLES20.glEnableVertexAttribArray(tch);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureIds[0]);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgram, "sTexture"), 0);


            int iResolutionLocation = GLES20.glGetUniformLocation(mProgram, "iResolution");
            GLES20.glUniform3fv(iResolutionLocation, 1, FloatBuffer.wrap(new float[]{(float) iResolution[0], (float) iResolution[1], 1.0f}));

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

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLES20.glFlush();
        }
    }

    void clear() {
        if (mVertexBuffer != null)
            mVertexBuffer.clear();
        if (mTexCoordBuffer != null)
            mTexCoordBuffer.clear();
        initStatus = false;
    }

    private int buildProgram(int VertexShaderID, int FragmentShaderID) {
        String vss="";
        String fss="";

        try
        {
            fss=getStringFromRawFile(mContext,FragmentShaderID);
            vss=getStringFromRawFile(mContext,VertexShaderID);

        }catch (IOException e) {
            Log.e(TAG, "loadFromShadersFromAssets() failed. Check paths to assets.\n" + e.getMessage());
        }

        int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vshader, vss);
        GLES20.glCompileShader(vshader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile vshader");
            Log.e(TAG, GLES20.glGetShaderInfoLog(vshader));
            GLES20.glDeleteShader(vshader);
            vshader = 0;
        }

        int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fshader, fss);
        GLES20.glCompileShader(fshader);
        GLES20.glGetShaderiv(fshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile fshader");
            Log.e(TAG, GLES20.glGetShaderInfoLog(fshader));
            GLES20.glDeleteShader(fshader);
            fshader = 0;
        }

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vshader);
        GLES20.glAttachShader(program, fshader);
        GLES20.glLinkProgram(program);

        return program;
    }

    private String getStringFromRawFile(Context context, int id) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(id);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();

        String line;
        while((line = reader.readLine()) != null) {
            builder.append(line + "\n");
        }
        inputStream.close();

        return builder.toString();
    }
}
