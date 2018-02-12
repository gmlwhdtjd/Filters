#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform vec3 iResolution;
uniform vec4 noiseLevel;
uniform float iGlobalTime;
uniform samplerExternalOES sTexture;
varying vec2 texCoord;

struct Filter_Var {
    vec3 rgb;
    float blur;
    float aberration;
    float focus;
    float noiseSize;
    float noiseIntensity;
};

uniform Filter_Var variables;

float rand(vec2 co) {
       return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453 * iGlobalTime);
}
float rand2(vec2 co) {
      return fract(cos(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453 * iGlobalTime);
}
float rand3(vec2 co) {
      return fract(sin(dot(co.xy, vec2(93.6249, 63.014))) * 92740.1048 * iGlobalTime);
}

void main ()
{
    float git;
    vec2 dis = gl_FragCoord.xy/iResolution.xy;
    vec2 pixelize = dis;
    dis *= -1.0;
    /////////////////////////////////

    dis -= 0.5;
    vec2 res = vec2(40.0, 40.0);
    vec3 rValue = texture2D(sTexture, texCoord-((dis/res).yx*variables.aberration)).rgb;
    vec3 gValue = texture2D(sTexture, texCoord).rgb;
    vec3 bValue = texture2D(sTexture, texCoord+((dis/res).yx*variables.aberration)).rgb;

    ////////////////////////////////색수차
    float distance = 0.0;
    distance += (dis.x>0.0)?dis.x:(-1.0)*dis.x;
    distance += (dis.y>0.0)?dis.y:(-1.0)*dis.y;

    float ResS = 150.0;
    float ResT = 150.0;

    vec2 stp0 = vec2(1.0/ResS, 0.0);
    vec2 st0p = vec2(0.0, 1.0/ResT);
    vec2 stpp = vec2(1.0/ResS, 1.0/ResT);
    vec2 stpm = vec2(1.0/ResS, -1.0/ResT);

    vec3 target = vec3(0.0, 0.0, 0.0);

    vec3 i00 = texture2D(sTexture, texCoord).rgb;
    int k=5;
    float result = 0.0;
    float mm = 1.0;         //next pixel
    float count = variables.blur;      //0.0 ~ 1.1 blur level
    target += (i00)*1.0;
    result += 1.0;
    for(int i=1; i<k; i++) {
        vec3 im1m1 = texture2D(sTexture, texCoord-((stpp*distance)*mm)).rgb;
        vec3 ip1p1 = texture2D(sTexture, texCoord+((stpp*distance)*mm)).rgb;
        vec3 im1p1 = texture2D(sTexture, texCoord-((stpm*distance)*mm)).rgb;
        vec3 ip1m1 = texture2D(sTexture, texCoord+((stpm*distance)*mm)).rgb;
        vec3 im10 = texture2D(sTexture, texCoord-((stp0*distance)*mm)).rgb;
        vec3 ip10 = texture2D(sTexture, texCoord+((stp0*distance)*mm)).rgb;
        vec3 i0m1 = texture2D(sTexture, texCoord-((st0p*distance)*mm)).rgb;
        vec3 i0p1 = texture2D(sTexture, texCoord+((st0p*distance)*mm)).rgb;
        target += (im10 + ip10 + i0m1 + i0p1 + im1m1 + ip1m1 + ip1p1 + im1p1) * count;
        result += count * 8.0;
        count *= count;
        mm+=1.0;
    }
    //count = ((1.0-count)/(1.0-0.5))*4.0 + 2.0;
    target /= result;
    target += vec3(rValue.r, gValue.g, bValue.b);
    target /= 2.0;

    gl_FragColor = vec4(target, 1.0);
    /*   noise
*/
    float blocksize = 500.0 * variables.noiseSize;
    vec2 block = floor(pixelize*blocksize);
    //gl_FragColor = texture2D(sTexture, texCoord);
    float randomDeltaR = (rand(block) * 2.0) - 1.0;
    float randomDeltaG = (rand2(block) * 2.0) - 1.0;
    float randomDeltaB = (rand3(block) * 2.0) - 1.0;
    float kk = variables.noiseIntensity;
    gl_FragColor.r += noiseLevel.r * randomDeltaR * kk;
    gl_FragColor.g += noiseLevel.g * randomDeltaG * kk;
    gl_FragColor.b += noiseLevel.b * randomDeltaB * kk;
    gl_FragColor.a += noiseLevel.a * randomDeltaR * kk;
    //*/
}

/*
컬러
블러
포커스
색수차? = 옛날 사진 느낌
노이즈
*/