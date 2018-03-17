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
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * A {@link GLSurfaceView} that can be adjusted to a specified aspect ratio.
 */
public class FCameraPreview extends GLSurfaceView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    private CameraViewRenderer mRenderer;
    private Callback mCallback;

    public FCameraPreview(Context context) {
        this(context, null);
    }

    public FCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRenderer = new CameraViewRenderer();

        this.setEGLContextClientVersion ( 2 );
        this.setRenderer(mRenderer);
        this.setRenderMode ( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
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
            // Todo : 화면 사이즈 관련 수정 필요
            // 화면 사이즈 관련해서 화면의 길이는 짧으나 카메라의 길이가 긴 디바이스의 경우
            // 화면 좌우에 레터박스가 생길 수 있으므로 이에 대한 처리가 필요함
            setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
        }
    }

    /**
     * Created by huijonglee on 2018. 1. 22..
     */
    private class CameraViewRenderer implements GLSurfaceView.Renderer {
        private FCameraPreviewRender mPreviewRender;

        private AtomicBoolean filterChanged =  new AtomicBoolean(false);
        private FCameraFilter mCameraFilter = null;

        private SurfaceTexture mInputSurfaceTexture;

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
            mSurfaceUpdated = false;
            mInitState.set(false);
            filterChanged.set(true);
            mPreviewRender.clear();
        }

        private void setFilter(FCameraFilter filter) {
            mCameraFilter = filter;
            filterChanged.set(true);
        }

        private void setCameraCharacteristics(CameraCharacteristics characteristics) throws NullPointerException {
            int orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

            if (facing == CameraCharacteristics.LENS_FACING_FRONT)
                mPreviewRender.setBuffers(orientation, FCameraPreviewRender.flip_RL);
            else
                mPreviewRender.setBuffers(orientation, FCameraPreviewRender.flip_NON);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mPreviewRender = new FCameraPreviewRender();
            mPreviewRender.initRender();

            mInputSurfaceTexture = mPreviewRender.getInputSurfaceTexture();
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

            mPreviewRender.setViewSize(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            if (!mInitState.get())
                return;

            if (filterChanged.getAndSet(false))
                mPreviewRender.setFilter(mCameraFilter);


            synchronized (mOnFrameAvailableListener) {
                if (mSurfaceUpdated) {
                    mInputSurfaceTexture.updateTexImage();
                    mSurfaceUpdated = false;
                }
            }

            mPreviewRender.onDraw();
        }
    }

    /**
     * Created by huijonglee on 2018. 1. 24..
     */
    public interface Callback {
        void onSurfaceCreated(int width, int height);
    }
}
