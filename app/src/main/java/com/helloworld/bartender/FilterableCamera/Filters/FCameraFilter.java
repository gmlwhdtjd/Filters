package com.helloworld.bartender.FilterableCamera.Filters;

import android.content.Context;
import android.util.Size;

import com.helloworld.bartender.FilterableCamera.FCameraGLUtils;

/**
 * Created by Song on 2018-02-23.
 */

public abstract class FCameraFilter {

    private Context mContext;

    private int mVertexShaderId;
    private int mFragmentshaderId;

    FCameraFilter(Context context, int vertexShaderId, int fragmentshaderId) {
        mContext = context;
        mVertexShaderId = vertexShaderId;
        mFragmentshaderId = fragmentshaderId;
    }

    public int getProgram() {
        return FCameraGLUtils.buildProgram(mContext, mVertexShaderId, mFragmentshaderId);
    }

    abstract public void onDraw(int program, Size viewSize);
}
