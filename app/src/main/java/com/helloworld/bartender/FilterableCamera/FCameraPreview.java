/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.helloworld.bartender.FilterableCamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.RetroFilter;
import com.helloworld.bartender.MainActivity;
import com.helloworld.bartender.R;

import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * A {@link GLSurfaceView} that can be adjusted to a specified aspect ratio.
 */
public class FCameraPreview extends GLSurfaceView {

    private FCamera mFCamera;

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    private int mDiffWidth = 0;
    private int mDiffHeight = 0;

    private previewRenderer mRenderer;
    private Callback mCallback;

    public FCameraPreview(Context context) {
        this(context, null);
    }

    public FCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRenderer = new previewRenderer();

        this.setEGLContextClientVersion ( 2 );
        this.setRenderer(mRenderer);
        this.setRenderMode ( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
    }

    void setFCamera(FCamera fCamera){
        mFCamera = fCamera;
    }

    public int getDifferenceHeight() {
        return mDiffHeight;
    }

    public int getDifferenceWidth() {
        return mDiffWidth;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        mRenderer.onPause();
        super.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("test", "onTouchEvent: down");
                if (mFCamera != null) {
                    mFCamera.touchToFocus(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d("test", "onTouchEvent: up");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("test", "onTouchEvent: move");
                break;
            default:
        }

        return super.onTouchEvent(event);
    }

    public void setFilter(FCameraFilter filter) {
        mRenderer.setFilter(filter);
    }

    public void setCameraCharacteristics(CameraCharacteristics characteristics) {
        mRenderer.setCameraCharacteristics(characteristics);
    }

    public final void setCallback(Callback callback){
        mCallback = callback;
    }

    public final SurfaceTexture getInputSurfaceTexture() {
        return mRenderer.mInputSurfaceTexture;
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width > height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
                mDiffWidth = 0;
                mDiffHeight = height - width * mRatioHeight / mRatioWidth;
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
                mDiffWidth = width - height * mRatioWidth / mRatioHeight;
                mDiffHeight = 0;
            }
        }
    }

    /**
     * Created by huijonglee on 2018. 1. 22..
     */
    private class previewRenderer implements GLSurfaceView.Renderer {

        private int mProgram;   // default program to convert TEXTURE_EXTERNAL to TEXTURE_2D

        //shader variable
        private FloatBuffer mVertexBuffer;
        private FloatBuffer mTexCoordBuffer;

        private FloatBuffer mExternalVertexBuffer;
        private FloatBuffer mExternalTexCoordBuffer;

        private FCameraRenderBuffer CAMERA_RENDER_BUF;
        private static final int BUF_ACTIVE_TEX_UNIT = GLES20.GL_TEXTURE8;

        private int mCameraTextureId = 0;
        private SurfaceTexture mInputSurfaceTexture;

        private Size mViewSize;

        private FCameraFilter mCameraFilter = null;

        private AtomicBoolean mInitState = new AtomicBoolean(false);

        private boolean mSurfaceUpdated = false;

        private final SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener
                = new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mSurfaceUpdated = true;
                FCameraPreview.this.requestRender();
            }
        };

        private void onPause() {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    GLES20.glDeleteProgram(mProgram);
                    mProgram = 0;

                    if (CAMERA_RENDER_BUF != null) {
                        CAMERA_RENDER_BUF.clear();
                        CAMERA_RENDER_BUF = null;
                    }

                    // TODO : Clear Filter
                    OriginalFilter.clear(FCameraFilter.Target.PREVIEW);
                    RetroFilter.clear(FCameraFilter.Target.PREVIEW);
                }
            });
            mSurfaceUpdated = false;
            mInitState.set(false);
        }

        private void setCameraCharacteristics(CameraCharacteristics characteristics) throws NullPointerException {
            int orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

            if (facing != CameraCharacteristics.LENS_FACING_FRONT) {
                mExternalVertexBuffer = FCameraGLUtils.getDefaultVertexBuffers(orientation, FCameraGLUtils.CAMERA_FLIP_RL);
                mExternalTexCoordBuffer = FCameraGLUtils.getDefaultmTexCoordBuffers(orientation, FCameraGLUtils.CAMERA_FLIP_RL);
            }
            else {
                mExternalVertexBuffer = FCameraGLUtils.getDefaultVertexBuffers(orientation, FCameraGLUtils.CAMERA_FLIP_NON);
                mExternalTexCoordBuffer = FCameraGLUtils.getDefaultmTexCoordBuffers(orientation,FCameraGLUtils.CAMERA_FLIP_NON);
            }
        }

        private void setFilter(FCameraFilter filter) {
            mCameraFilter = filter;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

            mProgram = FCameraGLUtils.buildProgram(getContext(), R.raw.filter_default_vshader, R.raw.filter_sampler_external_fshader);

            mCameraTextureId = FCameraGLUtils.genTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);

            mVertexBuffer = FCameraGLUtils.getDefaultVertexBuffers(0, FCameraGLUtils.CAMERA_FLIP_NON);
            mTexCoordBuffer = FCameraGLUtils.getDefaultmTexCoordBuffers(0,FCameraGLUtils.CAMERA_FLIP_NON);

            mExternalVertexBuffer = FCameraGLUtils.getDefaultVertexBuffers(0, FCameraGLUtils.CAMERA_FLIP_RL);
            mExternalTexCoordBuffer = FCameraGLUtils.getDefaultmTexCoordBuffers(0,FCameraGLUtils.CAMERA_FLIP_RL);

            mInputSurfaceTexture = new SurfaceTexture(mCameraTextureId);
            mInputSurfaceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, final int width, final int height) {
            if(!mInitState.getAndSet(true)) {

                if(mCallback != null) {
                    FCameraPreview.this.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mCallback.onSurfaceCreated(width, height);
                        }
                    });
                }
            }
            GLES20.glViewport(0, 0, width, height);
            mViewSize = new Size(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            if (!mInitState.get())
                return;

            synchronized (mOnFrameAvailableListener) {
                if (mSurfaceUpdated) {
                    mInputSurfaceTexture.updateTexImage();
                    mSurfaceUpdated = false;
                }
            }

            // Create camera render buffer
            if (CAMERA_RENDER_BUF == null) {
                CAMERA_RENDER_BUF = new FCameraRenderBuffer(mViewSize.getWidth(), mViewSize.getHeight(), BUF_ACTIVE_TEX_UNIT);
            }
            else if(CAMERA_RENDER_BUF.getWidth() != mViewSize.getWidth() ||
                    CAMERA_RENDER_BUF.getHeight() != mViewSize.getHeight()) {
                CAMERA_RENDER_BUF.clear();
                CAMERA_RENDER_BUF = new FCameraRenderBuffer(mViewSize.getWidth(), mViewSize.getHeight(), BUF_ACTIVE_TEX_UNIT);
            }

            // Use shaders
            GLES20.glUseProgram(mProgram);

            int ph = GLES20.glGetAttribLocation(mProgram, "vPosition");
            int tch = GLES20.glGetAttribLocation(mProgram, "vTexCoord");

            GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4 * 2, mExternalVertexBuffer);
            GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4 * 2, mExternalTexCoordBuffer);

            GLES20.glEnableVertexAttribArray(ph);
            GLES20.glEnableVertexAttribArray(tch);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mCameraTextureId);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgram, "sTexture"), 0);

            if (mCameraFilter != null && CAMERA_RENDER_BUF != null) {
                // Render to texture
                CAMERA_RENDER_BUF.bind();
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
                CAMERA_RENDER_BUF.unbind();
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

                mCameraFilter.onDrawFilter(CAMERA_RENDER_BUF.getTexId(), mVertexBuffer, mTexCoordBuffer, FCameraFilter.Target.PREVIEW, mViewSize);
            }
        }
    }

    /**
     * Created by huijonglee on 2018. 1. 24..
     */
    public interface Callback {
        void onSurfaceCreated(int width, int height);
    }
}
