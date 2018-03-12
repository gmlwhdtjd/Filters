package com.helloworld.bartender.FilterableCamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Size;

import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by huijonglee on 2018. 1. 30..
 */
class FCameraRenderer {

    private int[] mTextureIds;

    //shader variable
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoordBuffer;

    private Size mViewSize;

    private FCameraFilter mFilter;
    private int mProgram;
    private boolean initStatus = false;
    private Context mContext;

    FCameraRenderer() {
    }

    void initRender() {
        setBuffers(0, flip_NON);

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    void setFilter(FCameraFilter filter) {
        mFilter = filter;
        mProgram = mFilter.getProgram();
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
        mViewSize = new Size(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    void onDraw() {
        if (initStatus && mFilter != null) {
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

            mFilter.onDraw(mProgram, mViewSize);

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
}
