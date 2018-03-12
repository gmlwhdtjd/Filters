package com.helloworld.bartender.FilterableCamera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.tedpermission.PermissionListener;
import com.helloworld.bartender.tedpermission.TedPermission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

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

public class FCameraCapturer {
    private static final String TAG = "FCameraCapturer";

    private Context mContext;

    private HandlerThread renderThread;
    private Handler renderHandler;

    private EGL10 egl10;
    private EGLDisplay eglDisplay;
    private EGLContext eglContext;
    private EGLSurface eglSurface;

    private FCameraRenderer mCameraRender;

    private SurfaceTexture mInputSurfaceTexture;

    private Size mImageSize;
    private CameraCharacteristics mCameraCharacteristics;

    private AtomicBoolean filterChanged = new AtomicBoolean(false);
    private FCameraFilter mCameraFilter = null;

    private Semaphore initLock = new Semaphore(0);
    private boolean mSurfaceUpdated = false;

    private PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(mContext, "Permission Granted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(mContext, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    private final SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener
            = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
            mSurfaceUpdated = true;
            onDraw();
        }
    };

    public FCameraCapturer(Context context) {
        mContext = context;
    }

    public void setFilter(FCameraFilter filter) {
        mCameraFilter = filter;
        filterChanged.set(true);
    }

    void setCameraCharacteristics(@NonNull CameraCharacteristics characteristics, Size largest) {
        mCameraCharacteristics = characteristics;
        mImageSize = new Size(largest.getWidth(), largest.getHeight());

        init();
    }

    void onResume() {
        renderThread = new HandlerThread("renderThread");
        renderThread.start();
        renderHandler = new Handler(renderThread.getLooper());
    }

    void onPause() {
        filterChanged.set(true);
        initLock.tryAcquire();

        renderThread.quitSafely();
        try {
            renderThread.join();
            renderThread = null;
            renderHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Surface getInputSurface() {
        initLock.acquireUninterruptibly();
        Surface tmp = new Surface(mInputSurfaceTexture);
        initLock.release();

        return tmp;
    }

    private void init() {
        renderHandler.post(
                new Runnable() {
            @Override
            public void run() {

                int orientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                if (orientation == 90 || orientation == 270)
                    mImageSize = new Size(mImageSize.getHeight(), mImageSize.getWidth());

                initGL(mImageSize.getWidth(), mImageSize.getHeight());

                mCameraRender = new FCameraRenderer();
                mCameraRender.initRender();
                mCameraRender.setViewSize(mImageSize.getWidth(), mImageSize.getHeight());

                Integer facing = mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == CameraCharacteristics.LENS_FACING_FRONT)
                    mCameraRender.setBuffers(orientation, FCameraRenderer.flip_RL | FCameraRenderer.flip_UD);
                else
                    mCameraRender.setBuffers(orientation, FCameraRenderer.flip_UD);

                mInputSurfaceTexture = mCameraRender.getInputSurfaceTexture();
                if (orientation == 90 || orientation == 270)
                    mInputSurfaceTexture.setDefaultBufferSize(mImageSize.getHeight(), mImageSize.getWidth());
                else
                    mInputSurfaceTexture.setDefaultBufferSize(mImageSize.getWidth(), mImageSize.getHeight());

                mInputSurfaceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);

                initLock.release();
            }
        });
    }

    private void onDraw() {
        renderHandler.post(new Runnable() {
            @Override
            public void run() {
                if (filterChanged.getAndSet(false))
                    mCameraRender.setFilter(mCameraFilter);

                synchronized (mOnFrameAvailableListener) {
                    if (mSurfaceUpdated) {
                        mInputSurfaceTexture.updateTexImage();
                        mSurfaceUpdated = false;
                    }
                }

                mCameraRender.onDraw();

                saveImage();

                egl10.eglSwapBuffers(eglDisplay, eglSurface);
            }
        });
    }

    private void saveImage() {
        ByteBuffer mImageBuffer = ByteBuffer.allocate(mImageSize.getWidth() * mImageSize.getHeight() * 4);

        GLES20.glReadPixels(0, 0, mImageSize.getWidth(), mImageSize.getHeight(), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mImageBuffer);

        Bitmap bitmap = Bitmap.createBitmap(mImageSize.getWidth(), mImageSize.getHeight(), Bitmap.Config.ARGB_8888);

        bitmap.copyPixelsFromBuffer(mImageBuffer);

        try {
            //Permission Check
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                TedPermission.with(mContext)
                        .setPermissionListener(permissionlistener)
                        .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .check();
                return;
            }
            // TODO : 카운터를 추가해서 1초안에 여러장을 찍을 경우를 대비한다.
            // TODO : 저장 위치 설정.
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File mFile = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                            File.separator +"IMG_"+ timeStamp + ".jpg");

            FileOutputStream fos = new FileOutputStream(mFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            Log.d(TAG, mFile.toString());

            mContext.sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mFile)) );
        }
        catch (FileNotFoundException e) {
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
        egl10.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl10.eglDestroyContext(eglDisplay, eglContext);
        egl10.eglDestroySurface(eglDisplay, eglSurface);
        egl10.eglTerminate(eglDisplay);

        eglDisplay = EGL10.EGL_NO_DISPLAY;
        eglSurface = EGL10.EGL_NO_SURFACE;
        eglContext = EGL10.EGL_NO_CONTEXT;
    }
}
