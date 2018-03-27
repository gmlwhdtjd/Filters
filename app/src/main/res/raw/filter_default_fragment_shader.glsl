#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2                texCoord;
uniform samplerExternalOES  sTexture;

void main() {
    //gl_FragColor = texture2D(sTexture, texCoord);
    vec4 rgb = texture2D(sTexture, texCoord);

    gl_FragColor = vec4(1.0, rgb.g, rgb.b, 1.0);
}