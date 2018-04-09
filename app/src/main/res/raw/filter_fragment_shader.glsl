#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform vec3 iResolution;
uniform vec3 noiseLevel;
uniform float iGlobalTime;
uniform sampler2D sTexture;
uniform sampler2D sNoiseTexture;
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
    float noiseSize;
    float noiseIntensity;
};

uniform Filter_Var variables;

vec3 RGBtoHSV(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float min = q.w>q.y?q.y:q.w;
    float d = q.x - min;
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 HSVtoRGB(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float HUEtoRGB (float v1, float v2, float vH) {
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

       RGB.r = HUEtoRGB( var_1, var_2, HSL.x + ( 1.0 / 3.0 ) );
       RGB.g = HUEtoRGB( var_1, var_2, HSL.x );
       RGB.b = HUEtoRGB( var_1, var_2, HSL.x - ( 1.0 / 3.0 ) );
    }

    return RGB;
}



void main ()
{

    vec2 dis = gl_FragCoord.xy/iResolution.xy;
    dis -= 0.5;
    vec2 res = vec2(40.0, 40.0);

    ////////////////////////////////색수차
    vec3 target = vec3(texture2D(sTexture, texCoord-(dis/res).yx*variables.aberration).r,
                        texture2D(sTexture, texCoord).g,
                        texture2D(sTexture, texCoord+(dis/res).yx*variables.aberration).b);

    target = texture2D(sTexture, texCoord).rgb;
    vec3 saturation = RGBtoHSV(target);
    saturation.y *= 2.0*variables.saturation;
    target = HSVtoRGB(saturation);
    vec3 brightness = vec3(0.0, 0.0, variables.brightness);
    target += HSLtoRGB(brightness);
    target = vec3(target.x>1.0?1.0:target.x, target.y>1.0?1.0:target.y, target.z>1.0?1.0:target.z);
    target = vec3(target.x<0.0?0.0:target.x, target.y<0.0?0.0:target.y, target.z<0.0?0.0:target.z);
    vec3 RGBfilter = vec3(variables.rgb.r, variables.rgb.g, variables.rgb.b);
    target = (1.0-variables.colorRatio)*target + variables.colorRatio*RGBfilter;

    vec4 noisetexture = texture2D(sNoiseTexture, fract(texCoord * iGlobalTime));
    target += vec3(noisetexture.r*2.0-1.0, noisetexture.g*2.0-1.0, noisetexture.b*2.0-1.0)*variables.noiseIntensity;

    gl_FragColor = vec4(target, 1.0);

}