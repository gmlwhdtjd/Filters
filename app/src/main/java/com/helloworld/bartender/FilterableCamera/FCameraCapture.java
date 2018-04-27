package com.helloworld.bartender.FilterableCamera;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;

import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.RetroFilter;
import com.helloworld.bartender.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;
import static android.opengl.EGL14.EGL_OPENGL_ES2_BIT;
import static javax.microedition.khronos.egl.EGL10.EGL_PBUFFER_BIT;

/**
 * Created by huijonglee on 2018. 1. 27..
 */

public class FCameraCapture {
    private static final String TAG = "FCameraCapture";

    private Context mContext;

    private HandlerThread renderThread;
    private Handler renderHandler;

    private EGL10 egl10;
    private EGLDisplay eglDisplay;
    private EGLContext eglContext;
    private EGLSurface eglSurface;

    //shader variable
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoordBuffer;

    private Size mImageSize;
    private CameraCharacteristics mCameraCharacteristics;

    private FCameraFilter mCameraFilter = null;

    private Semaphore initLock = new Semaphore(0);
    private Semaphore bitmapFilteringLock;

    private Bitmap resultBitmap;

    private String mSaveDirectory;

    public FCameraCapture(Context context) {
        mContext = context;
    }

    public void setFilter(final FCameraFilter filter) {
        mCameraFilter = filter;
    }

    public void setSaveDirectory(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        mSaveDirectory = file.getAbsolutePath();
        SharedPreferences pref = mContext.getSharedPreferences(mContext.getString(R.string.gallery_pref),0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(mContext.getString(R.string.key_gallery_name),mSaveDirectory);
        editor.commit();
    }

    void setCameraCharacteristics(@NonNull CameraCharacteristics characteristics, Size largest) {
        mCameraCharacteristics = characteristics;
        mImageSize = new Size(largest.getWidth(), largest.getHeight());

        init();
    }

    void onResume() {
        bitmapFilteringLock = new Semaphore(0);
        renderThread = new HandlerThread("renderThread");
        renderThread.start();
        renderHandler = new Handler(renderThread.getLooper());
    }

    void onPause() {
        initLock.tryAcquire();

        clear();

        renderThread.quitSafely();
        try {
            renderThread.join();
            renderThread = null;
            renderHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        renderHandler.post(new Runnable() {
            @Override
            public void run() {

                int orientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                if (orientation == 90 || orientation == 270)
                    mImageSize = new Size(mImageSize.getHeight(), mImageSize.getWidth());

                initGL(mImageSize.getWidth(), mImageSize.getHeight());

                Integer facing = mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != CameraCharacteristics.LENS_FACING_FRONT) {
                    mVertexBuffer = FCameraGLUtils.getDefaultVertexBuffers(orientation, FCameraGLUtils.CAMERA_FLIP_RL | FCameraGLUtils.CAMERA_FLIP_UD);
                    mTexCoordBuffer = FCameraGLUtils.getDefaultmTexCoordBuffers(orientation, FCameraGLUtils.CAMERA_FLIP_RL| FCameraGLUtils.CAMERA_FLIP_UD);
                }
                else {
                    mVertexBuffer = FCameraGLUtils.getDefaultVertexBuffers(orientation, FCameraGLUtils.CAMERA_FLIP_UD);
                    mTexCoordBuffer = FCameraGLUtils.getDefaultmTexCoordBuffers(orientation, FCameraGLUtils.CAMERA_FLIP_UD);
                }

                initLock.release();
            }
        });
    }

    void clear() {
        renderHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO : Clear Filter
                OriginalFilter.clear(FCameraFilter.Target.IMAGE);
                RetroFilter.clear(FCameraFilter.Target.IMAGE);

                shutdownEGL();
            }
        });
    }

    void onDrawFilter(final Bitmap bitmap) {
        renderHandler.post(new Runnable() {
            @Override
            public void run() {

                mCameraFilter.onDrawFilter(bitmap, mVertexBuffer, mTexCoordBuffer, FCameraFilter.Target.IMAGE, mImageSize);

                saveImage(mImageSize);

                egl10.eglSwapBuffers(eglDisplay, eglSurface);
            }
        });
    }

    public Bitmap bitmapFiltering(final FCameraFilter filter, final Bitmap bitmap) {
        renderHandler.post(new Runnable() {
            @Override
            public void run() {
                FloatBuffer vertexBuffer = FCameraGLUtils.getDefaultVertexBuffers(0, FCameraGLUtils.CAMERA_FLIP_NON);
                FloatBuffer texCoordBuffer = FCameraGLUtils.getDefaultmTexCoordBuffers(0, FCameraGLUtils.CAMERA_FLIP_NON);

                Size imageSize = new Size(bitmap.getWidth(), bitmap.getHeight());

                filter.onDrawFilter(bitmap, vertexBuffer, texCoordBuffer, FCameraFilter.Target.IMAGE, imageSize);

                ByteBuffer mImageBuffer = ByteBuffer.allocate(imageSize.getWidth() * imageSize.getHeight() * 4);

                GLES20.glReadPixels(0, 0, imageSize.getWidth(), imageSize.getHeight(), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mImageBuffer);

                resultBitmap = Bitmap.createBitmap(imageSize.getWidth(), imageSize.getHeight(), Bitmap.Config.ARGB_8888);

                resultBitmap.copyPixelsFromBuffer(mImageBuffer);

                egl10.eglSwapBuffers(eglDisplay, eglSurface);

                bitmapFilteringLock.release();
            }
        });

        bitmapFilteringLock.acquireUninterruptibly();

        return resultBitmap;
    }

    private void saveImage(Size imageSize){
        ByteBuffer mImageBuffer = ByteBuffer.allocate(imageSize.getWidth() * imageSize.getHeight() * 4);

        GLES20.glReadPixels(0, 0, imageSize.getWidth(), imageSize.getHeight(), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mImageBuffer);

        Bitmap bitmap = Bitmap.createBitmap(imageSize.getWidth(), imageSize.getHeight(), Bitmap.Config.ARGB_8888);

        bitmap.copyPixelsFromBuffer(mImageBuffer);

        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String millisecondStamp = String.format("%04d", System.currentTimeMillis() % 10000);
            File file = new File(mSaveDirectory + File.separator + "IMG_" + timeStamp + "_" + millisecondStamp + ".jpg");

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            Log.d(TAG, file.toString());

            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    private void initGL(int width, int height) {
        egl10 = (EGL10) EGLContext.getEGL();

        eglDisplay = egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed " +
                    android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }

        int[] version = new int[2];
        if (!egl10.eglInitialize(eglDisplay, version)) {
            throw new RuntimeException("eglInitialize failed " +
                    android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }

        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] configSpec = {
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL10.EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE
        };

        EGLConfig eglConfig = null;
        if (!egl10.eglChooseConfig(eglDisplay, configSpec, configs, 1, configsCount)) {
            throw new IllegalArgumentException("eglChooseConfig failed " +
                    android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        } else if (configsCount[0] > 0) {
            eglConfig = configs[0];
        }
        if (eglConfig == null) {
            throw new RuntimeException("eglConfig not initialized");
        }

        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
        eglContext = egl10.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);

        int[] surfaceAttr = {
                EGL10.EGL_WIDTH, width,
                EGL10.EGL_HEIGHT, height,
                EGL10.EGL_NONE
        };
        eglSurface = egl10.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttr);

        if (eglSurface == null || eglSurface == EGL10.EGL_NO_SURFACE) {
            throw new RuntimeException("eglCreatePbufferSurface failed " +
                    android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }

        if (!egl10.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw new RuntimeException("eglMakeCurrent failed " +
                    android.opengl.GLUtils.getEGLErrorString(egl10.eglGetError()));
        }
    }

    private void shutdownEGL() {
        if (eglDisplay == null || eglSurface == null || eglContext == null)
            return;

        egl10.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl10.eglDestroyContext(eglDisplay, eglContext);
        egl10.eglDestroySurface(eglDisplay, eglSurface);
        egl10.eglTerminate(eglDisplay);

        eglDisplay = EGL10.EGL_NO_DISPLAY;
        eglSurface = EGL10.EGL_NO_SURFACE;
        eglContext = EGL10.EGL_NO_CONTEXT;
    }
}
