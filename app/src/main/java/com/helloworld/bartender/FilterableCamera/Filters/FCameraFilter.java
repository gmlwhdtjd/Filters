package com.helloworld.bartender.FilterableCamera.Filters;

import android.content.Context;
import android.util.Size;

import com.helloworld.bartender.FilterableCamera.FCameraGLUtils;

/**
 * Created by Song on 2018-02-23.
 */

public abstract class FCameraFilter {

    public Context mContext;

    private final Integer mId;
    private String mName;

    private int mVertexShaderId;
    private int mFragmentShaderId;

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

    public int getProgram() {
        return FCameraGLUtils.buildProgram(mContext, mVertexShaderId, mFragmentShaderId);
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

    abstract public void onDraw(int program, Size viewSize);

    abstract public void setValueWithType(ValueType type, int value);
    abstract public int getValueWithType(ValueType type);
}
