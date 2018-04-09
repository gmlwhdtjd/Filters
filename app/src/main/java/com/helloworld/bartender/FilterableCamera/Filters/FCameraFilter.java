package com.helloworld.bartender.FilterableCamera.Filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Size;

import com.helloworld.bartender.FilterableCamera.FCameraGLUtils;

import java.nio.FloatBuffer;

/**
 * Created by Song on 2018-02-23.
 */

public abstract class FCameraFilter {

    public Context mContext;

    private final Integer mId;
    private String mName;

    private int mVertexShaderId;
    private int mFragmentShaderId;

    protected abstract int getPreviewProgramID();
    protected abstract void setPreviewProgramID(int id);

    protected abstract int getImageProgramID();
    protected abstract void setImageProgramID(int id);

    public enum Target {
        PREVIEW,
        IMAGE
    }

    interface ValueType {
        String getPageName(Context context);
        String getValueName(Context context);
    }

    FCameraFilter(Context context,
                  int vertexShaderId, int fragmentshaderId,
                  Integer id) {
        mContext = context;
        mVertexShaderId = vertexShaderId;
        mFragmentShaderId = fragmentshaderId;
        mId = id;
    }

    public Context getContext() {
        return mContext;
    }
    public String getName() {
        return mName;
    }
    public Integer getId() {
        return mId;
    }
    public void setName(String name) {
        mName = name;
    }

    int getProgram(Target target) {
        switch (target) {
            case PREVIEW:
                if (getPreviewProgramID() == 0)
                    setPreviewProgramID(FCameraGLUtils.buildProgram(mContext, mVertexShaderId, mFragmentShaderId));
                return getPreviewProgramID();
            case IMAGE:
                if (getImageProgramID() == 0)
                    setImageProgramID(FCameraGLUtils.buildProgram(mContext, mVertexShaderId, mFragmentShaderId));
                return getImageProgramID();
            default:
                return 0;
        }
    }

    public void onDrawFilter(int textureId, FloatBuffer vertexBuffer, FloatBuffer texCoordBuffer, Target target, Size viewSize) {
        int program = getProgram(target);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(program);

        int ph = GLES20.glGetAttribLocation(program, "vPosition");
        int tch = GLES20.glGetAttribLocation(program, "vTexCoord");

        GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4 * 2, vertexBuffer);
        GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4 * 2, texCoordBuffer);

        GLES20.glEnableVertexAttribArray(ph);
        GLES20.glEnableVertexAttribArray(tch);

        onDraw(program, target, textureId, viewSize);

        GLES20.glFlush();
    }

    public void onDrawFilter(Bitmap bitmap, FloatBuffer vertexBuffer, FloatBuffer texCoordBuffer, Target target, Size viewSize) {
        GLES20.glViewport(0, 0, viewSize.getWidth(), viewSize.getHeight());

        int textureId = FCameraGLUtils.genTexture();
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        onDrawFilter(textureId, vertexBuffer, texCoordBuffer, target, viewSize);
    }

    abstract protected void onDraw(int program, Target target, int textureId, Size viewSize);

    abstract public void setValueWithType(ValueType type, int value);
    abstract public int getValueWithType(ValueType type);
}
