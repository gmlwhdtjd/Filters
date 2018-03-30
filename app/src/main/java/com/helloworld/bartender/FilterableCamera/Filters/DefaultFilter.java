package com.helloworld.bartender.FilterableCamera.Filters;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Size;

import com.helloworld.bartender.R;

/**
 * Created by huijonglee on 2018. 3. 30..
 */

public class DefaultFilter extends FCameraFilter {

    private static int mPreviewProgram = 0;
    private static int mImageProgram = 0;

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
                break;
            case IMAGE:
                if (mImageProgram != 0)
                    GLES20.glDeleteProgram(mImageProgram);
                mImageProgram = 0;
                break;
        }
    }

    public enum ValueType implements FCameraFilter.ValueType {
        ;

        @Override
        public String getPageName(Context context) {
            return "default";
        }

        @Override
        public String getValueName(Context context) {
            return "default";
        }

    }

    public DefaultFilter(Context context, Integer id, String name) {
        super(context, R.raw.filter_vertex_shader, R.raw.filter_default_fshader, id);

        setName(name);
    }

    @Override
    public void setValueWithType(FCameraFilter.ValueType type, int value) {
    }

    @Override
    public int getValueWithType(FCameraFilter.ValueType type) {
        return 0;
    }

    @Override
    public void onDraw(int program, Size viewSize) {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
