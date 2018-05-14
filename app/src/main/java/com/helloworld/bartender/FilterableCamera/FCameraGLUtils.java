package com.helloworld.bartender.FilterableCamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.NonNull;
import android.util.Log;

import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Song on 2018-02-23.
 */

public class FCameraGLUtils {
    private static final String TAG = "FCameraGLUtils";

    public static final int CAMERA_FLIP_NON = 0x00; // non flipping
    public static final int CAMERA_FLIP_UD = 0x01; // Up Down flipping
    public static final int CAMERA_FLIP_RL = 0x10; // Right Left flipping

    private static void swapElement(float[] a, int i, int j) {
        float tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    /**
     * set vertex and texcoord buffer
     * this method must be called before onDraw
     *
     * @param cameraOrientation it should be 0, 90, 180, 270
     * @param flipping use flip_NON, flip_UD, flip_RL
     */
    public static FloatBuffer getDefaultVertexBuffers(int cameraOrientation, int flipping) {
        float[] vertexBufferData
                = {   -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f,};
        FloatBuffer vertexBuffer
                = ByteBuffer.allocateDirect(8 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertexBufferData);
        vertexBuffer.position(0);

        return vertexBuffer;
    }

    public static FloatBuffer getDefaultmTexCoordBuffers(int cameraOrientation, int flipping) {
        float[] texCoordData;

        switch (cameraOrientation){
            case 0:
                texCoordData = new float[] { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f };
                break;
            case 90:
                texCoordData = new float[] { 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f };
                break;
            case 180:
                texCoordData = new float[] { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };
                break;
            case 270:
                texCoordData = new float[] { 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f };
                break;
            default:
                throw new RuntimeException("Invalid value of \"cameraOrientation\"");
        }

        if ((flipping & CAMERA_FLIP_UD) == CAMERA_FLIP_UD){
            swapElement(texCoordData, 0, 4);
            swapElement(texCoordData, 1, 5);
            swapElement(texCoordData, 2, 6);
            swapElement(texCoordData, 3, 7);
        }

        if ((flipping & CAMERA_FLIP_RL) == CAMERA_FLIP_RL){
            swapElement(texCoordData, 0, 2);
            swapElement(texCoordData, 1, 3);
            swapElement(texCoordData, 4, 6);
            swapElement(texCoordData, 5, 7);
        }

        FloatBuffer texCoordBuffer
                = ByteBuffer.allocateDirect(8 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        texCoordBuffer.put(texCoordData);
        texCoordBuffer.position(0);

        return texCoordBuffer;
    }

    public static int genTexture() {
        return genTexture(GLES20.GL_TEXTURE_2D);
    }

    public static int genTexture(int textureType) {
        int[] genBuf = new int[1];
        GLES20.glGenTextures(1, genBuf, 0);
        GLES20.glBindTexture(textureType, genBuf[0]);

        // Set texture default draw parameters
        if (textureType == GLES11Ext.GL_TEXTURE_EXTERNAL_OES) {
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        } else {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
        }

        return genBuf[0];
    }

    public static int loadTexture(final Context context, final int resourceId, int[] size) {
        final int texId = genTexture();

        if (texId != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling
            options.inJustDecodeBounds = true;

            // Just decode bounds
            BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Set return size
            size[0] = options.outWidth;
            size[1] = options.outHeight;

            // Decode
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        return texId;
    }

    public static int buildProgram(Context context, int VertexShaderID, int FragmentShaderID) {
        String vss="";
        String fss="";

        try
        {
            fss=getStringFromRawFile(context,FragmentShaderID);
            vss=getStringFromRawFile(context,VertexShaderID);

        }catch (IOException e) {
            Log.e(TAG, "loadFromShadersFromAssets() failed. Check paths to assets.\n" + e.getMessage());
        }

        int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vshader, vss);
        GLES20.glCompileShader(vshader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile vshader");
            Log.e(TAG, GLES20.glGetShaderInfoLog(vshader));
            GLES20.glDeleteShader(vshader);
            vshader = 0;
        }

        int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fshader, fss);
        GLES20.glCompileShader(fshader);
        GLES20.glGetShaderiv(fshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile fshader");
            Log.e(TAG, GLES20.glGetShaderInfoLog(fshader));
            GLES20.glDeleteShader(fshader);
            fshader = 0;
        }

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vshader);
        GLES20.glAttachShader(program, fshader);
        GLES20.glLinkProgram(program);

        GLES20.glDeleteShader(vshader);
        GLES20.glDeleteShader(fshader);

        return program;
    }

    @NonNull
    private static String getStringFromRawFile(Context context, int id) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(id);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();

        String line;
        while((line = reader.readLine()) != null) {
            builder.append(line + "\n");
        }
        inputStream.close();

        return builder.toString();
    }
}
