package com.helloworld.bartender.FilterableCamera;

import android.content.Context;

import com.helloworld.bartender.R;

/**
 * Created by Song on 2018-02-23.
 */

public abstract class FCameraFilter {

    private Context mContext;

    private int vertexShaderId;
    private int fragmentshaderId;

    public FCameraFilter(Context context) {
        mContext = context;
        setShaderIds();
    }

    abstract void setShaderIds();
    abstract void onDraw(int Width, int Height);

    int getProgram() {
        return FCameraGLUtils.buildProgram(mContext, R.raw.filter_vertex_shader, R.raw.filter_fragment_shader);
    }
}
