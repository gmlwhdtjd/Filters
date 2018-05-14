#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform sampler2D sTexture;
uniform vec2 iResolution;
varying vec2 texCoord;
uniform float mask[25];
uniform float focus;

void main ()
{
    vec3 target = vec3(0.0, 0.0, 0.0);

    float radius = 0.0;
    vec2 point = gl_FragCoord.xy/iResolution.x;
    point -= vec2(0.5, iResolution.y);
    radius = point.x*point.x+point.y*point.y;
    radius = min(1.0, radius/focus);

    vec2 stp0 = vec2(0.0025, 0.0)*radius;    //  x
    vec2 st0p = vec2(0.0, 0.0025)*radius;    //  y

    for(int i=0; i<5; i++) {
        for(int j=0; j<5; j++) {
            target += vec3(texture2D(sTexture, texCoord+((float(j)-2.0)*stp0)+((float(i)-2.0)*st0p)).rgb*mask[i*5+j]);
        }
    }

    gl_FragColor = vec4(target, 1.0);
}