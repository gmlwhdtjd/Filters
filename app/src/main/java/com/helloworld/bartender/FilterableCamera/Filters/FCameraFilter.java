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
    private int mFragmentShaderId;

    interface ValueType {
        int getPageNumber();
    }

    FCameraFilter(Context context, int vertexShaderId, int fragmentshaderId) {
        mContext = context;
        mVertexShaderId = vertexShaderId;
        mFragmentShaderId = fragmentshaderId;
    }

    public int getProgram() {
        return FCameraGLUtils.buildProgram(mContext, mVertexShaderId, mFragmentShaderId);
    }

    abstract public void onDraw(int program, Size viewSize);

    abstract public void setValueWithType(ValueType type, int value);
    abstract public int getValueWithType(ValueType type);
}
