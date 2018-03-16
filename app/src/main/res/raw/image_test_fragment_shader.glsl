#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform vec3 iResolution;
uniform vec3 noiseLevel;
uniform float iGlobalTime;
uniform sampler2D sTexture;
varying vec2 texCoord;
uniform float mask[25];
uniform vec3 randomRGB;

struct Filter_Var {
    vec3 rgb;
    float colorRatio;
    float brightness;
    float saturation;
    float blur;
    float aberration;
    float focus;
    float noiseSize;
    float noiseIntensity;
};

uniform Filter_Var variables;

float PHI = 1.61803398874989484820459 * 00000.1; // Golden Ratio
float PI  = 3.14159265358979323846264 * 00000.1; // PI
float SRT = 1.41421356237309504880169 * 10000.0; // Square Root of Two

float gold_noise(vec2 coordinate, float seed)
{
    return fract(sin(dot(coordinate*seed, vec2(PHI, PI)))*SRT);
}

float rand(vec2 co) {
       return fract(dot(co.xy,vec2(PHI, PI)) * SRT * iGlobalTime);
}
float rand2(vec2 co) {
       return fract(sin(dot(co.xy, vec2(PHI, PI))) * SRT * iGlobalTime * noiseLevel.y);
}
float rand3(vec2 co) {
       return fract(sin(dot(co.xy, vec2(PHI, PI))) * SRT * iGlobalTime * noiseLevel.z);
}

float min(float a, float b, float c) {
    float result;
    if(a>=b) result = b;
    else    result = a;
    if(result>=c)   result = c;

    return result;
}

float max(float a, float b, float c) {
    float result;
    if(a>=b) result = a;
    else    result = b;
    if(result<=c)   result = c;

    return result;
}

vec3 RGBtoHSL (vec3 RGB) {

    vec3 HSL;

    float var_R = RGB.r;
    float var_G = RGB.g;
    float var_B = RGB.b;

    float var_Min = min( var_R, var_G, var_B );   //Min. value of RGB
    float var_Max = max( var_R, var_G, var_B );   //Max. value of RGB
    float del_Max = var_Max - var_Min;            //Delta RGB value

    HSL.z = ( var_Max + var_Min )/ 2.0;

    if ( del_Max == 0.0 )                     //This is a gray, no chroma...
    {
        HSL.x = 0.0;
        HSL.y = 0.0;
    }
    else                                    //Chromatic data...
    {
       if ( HSL.z < 0.5 ) HSL.y = del_Max / ( var_Max + var_Min );
       else           HSL.y = del_Max / ( 2.0 - var_Max - var_Min );

       float del_R = ( ( ( var_Max - var_R ) / 6.0 ) + ( del_Max / 2.0 ) ) / del_Max;
       float del_G = ( ( ( var_Max - var_G ) / 6.0 ) + ( del_Max / 2.0 ) ) / del_Max;
       float del_B = ( ( ( var_Max - var_B ) / 6.0 ) + ( del_Max / 2.0 ) ) / del_Max;

       if      ( var_R == var_Max ) HSL.x = del_B - del_G;
       else if ( var_G == var_Max ) HSL.x = ( 1.0 / 3.0 ) + del_R - del_B;
       else if ( var_B == var_Max ) HSL.x = ( 2.0 / 3.0 ) + del_G - del_R;

        if ( HSL.x < 0.0 ) HSL.x += 1.0;
        if ( HSL.x > 1.0 ) HSL.x -= 1.0;
    }

    return HSL;
}

float Hue_2_RGB (float v1, float v2, float vH) {
    float k;
       if ( vH < 0.0 ) vH += 1.0;
       if( vH > 1.0 ) vH -= 1.0;
       if ( ( 6.0 * vH ) < 1.0 ) {
           k = v1 + ( v2 - v1 ) * 6.0 * vH;
           return k;
       }
       if ( ( 2.0 * vH ) < 1.0 ) {
           k = v2;
           return k;
       }
       if ( ( 3.0 * vH ) < 2.0 ) {
           k = v1 + ( v2 - v1 ) * ( ( 2.0 / 3.0 ) - vH ) * 6.0;
           return k;
       }
       k = v1;
       return k;
}

vec3 HSLtoRGB(vec3 HSL) {
    vec3 RGB;
    if ( HSL.y == 0.0 )
    {
       RGB.r = HSL.z;
       RGB.g = HSL.z;
       RGB.b = HSL.z;
    }
    else
    {
        float var_2;
       if ( HSL.z < 0.5 ) var_2 = HSL.z * ( 1.0 + HSL.y );
       else           var_2 = ( HSL.z + HSL.y ) - ( HSL.y * HSL.z );

       float var_1 = 2.0 * HSL.z - var_2;

       RGB.r = Hue_2_RGB( var_1, var_2, HSL.x + ( 1.0 / 3.0 ) );
       RGB.g = Hue_2_RGB( var_1, var_2, HSL.x );
       RGB.b = Hue_2_RGB( var_1, var_2, HSL.x - ( 1.0 / 3.0 ) );
    }

    return RGB;
}



void main ()
{

    vec2 dis = gl_FragCoord.xy/iResolution.xy;
    vec2 pixelize = dis;
    dis -= 0.5;
    vec2 res = vec2(40.0, 40.0);

    ////////////////////////////////색수차
    float radius = 0.0;
    vec2 innerR = gl_FragCoord.xy/iResolution.x;
    innerR -= vec2(0.5, (iResolution.y/iResolution.x)/2.0);
    float x = innerR.x>0.0?innerR.x:-1.0*innerR.x;
    float y = innerR.y>0.0?innerR.y:-1.0*innerR.y;
    y = y>1.0?1.0:y;
    radius = x*x+y*y;
    radius = radius>variables.focus?1.0:radius/variables.focus;

    vec2 stp0 = vec2(1.0/400.0, 0.0)*radius;    //  x
    vec2 st0p = vec2(0.0, 1.0/400.0)*radius;    //  y

    vec3 target = vec3(0.0, 0.0, 0.0);

    float fi=0.0;
    float fj = 0.0;
    for(int i=0; i<5; i++) {
        fj = 0.0;
        for(int j=0; j<5; j++) {
            target += vec3(texture2D(sTexture, texCoord+((fj-2.0)*stp0)+((fi-2.0)*st0p)-(dis/res).yx*variables.aberration).r*mask[i*5+j],
                            texture2D(sTexture, texCoord+((fj-2.0)*stp0)+((fi-2.0)*st0p)).g*mask[i*5+j],
                            texture2D(sTexture, texCoord+((fj-2.0)*stp0)+((fi-2.0)*st0p)+(dis/res).yx*variables.aberration).b*mask[i*5+j]);
            fj+=1.0;
        }
        fi += 1.0;
    }

    vec3 saturation = vec3(0.0, variables.saturation, 0.0);
    vec3 brightness = vec3(0.0, 0.0, variables.brightness);
    target += HSLtoRGB(saturation);
    target += HSLtoRGB(brightness);
    target = vec3(target.x>1.0?1.0:target.x, target.y>1.0?1.0:target.y, target.z>1.0?1.0:target.z);
    target = vec3(target.x<0.0?0.0:target.x, target.y<0.0?0.0:target.y, target.z<0.0?0.0:target.z);
    vec3 RGBfilter = vec3(variables.rgb.r, variables.rgb.g, variables.rgb.b);
    target = (1.0-variables.colorRatio)*target + variables.colorRatio*RGBfilter;

//    float blocksize = 500.0 * variables.noiseSize;
//    vec3 randomDelta = vec3(gold_noise(pixelize, iGlobalTime+PHI));
//    vec4 noisetexture = texture2D(sNoiseTexture, fract(texCoord * iGlobalTime));
//    target += vec3(noisetexture.r*2.0-1.0, noisetexture.g*2.0-1.0, noisetexture.b*2.0-1.0)*variables.noiseIntensity;

    gl_FragColor = vec4(target, 1.0);

}

/*
컬러
블러
포커스
색수차? = 옛날 사진 느낌
노이즈
*/