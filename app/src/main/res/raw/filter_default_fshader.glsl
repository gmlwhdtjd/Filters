precision mediump float;

varying vec2        texCoord;
uniform sampler2D   sTexture;

void main() {
    gl_FragColor = texture2D(sTexture, texCoord);
}