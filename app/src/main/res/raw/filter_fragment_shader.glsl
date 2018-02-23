#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform vec3 iResolution;
uniform vec4 noiseLevel;
uniform float iGlobalTime;
uniform samplerExternalOES sTexture;
varying vec2 texCoord;
uniform float mask[49];
struct Filter_Var {
    vec3 rgb;
    float blur;
    float filterRatio;
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
    dis *= -1.0;
    dis -= 0.5;
    vec2 res = vec2(40.0, 40.0);
    vec3 rValue = texture2D(sTexture, texCoord-((dis/res).yx*variables.aberration)).rgb;
    vec3 gValue = texture2D(sTexture, texCoord).rgb;
    vec3 bValue = texture2D(sTexture, texCoord+((dis/res).yx*variables.aberration)).rgb;

    ////////////////////////////////색수차
    float distance = 0.0;
    distance += (dis.x>0.0)?dis.x:(-1.0)*dis.x;
    distance += (dis.y>0.0)?dis.y:(-1.0)*dis.y;
    distance *= variables.focus;

    float ResS = iResolution.x;
    float ResT = iResolution.y;

    vec2 stp0 = vec2(4.0/ResS, 0.0);    //  x
    vec2 st0p = vec2(0.0, 8.0/ResT);    //  y
    vec2 stpp = vec2(1.0/ResS, 1.0/ResT);
    vec2 stpm = vec2(1.0/ResS, -1.0/ResT);

    vec3 target = vec3(0.0, 0.0, 0.0);
    float fi=0.0;
    float fj = 0.0;
    for(int i=0; i<7; i++) {
        fj = 0.0;
        for(int j=0; j<7; j++) {
            target += texture2D(sTexture, texCoord+((fj-3.0)*stp0)+((fi-3.0)*st0p)).rgb*mask[i*7+j];
            fj+=1.0;
        }
        fi += 1.0;
    }
/*mask[i*7+j]
    vec3 i00 = texture2D(sTexture, texCoord).rgb;
    int k=50;
    float result = 0.0;
    float mm = 1.0;         //next pixel
    float count = variables.blur;      //0.0 ~ 1.0 blur level
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
    */
    //target += vec3(rValue.r, gValue.g, bValue.b);
    //target /= 2.0;

    vec3 HSL = RGBtoHSL(target);
    HSL += vec3(0.0, variables.rgb.g, variables.rgb.b);
    if(HSL.y > 1.0 )  HSL.y = 1.0;
    if(HSL.y < 0.0 )  HSL.y = 0.0;
    if(HSL.z > 1.0 )  HSL.z = 1.0;
    if(HSL.z < 0.0 )  HSL.z = 0.0;
    target = HSLtoRGB(HSL);
    float filterRatio = 0.0;
    vec3 HSLfilter = vec3(variables.rgb.r, 0.7, 0.7);
    target = (1.0-filterRatio)*target + filterRatio*HSLtoRGB(HSLfilter);
    gl_FragColor = vec4(target, 1.0);

    float blocksize = 500.0 * variables.noiseSize;
    vec2 block = floor(pixelize*blocksize);
    vec3 randomDelta = vec3((rand(block) * 2.0) - 1.0, (rand2(block) * 2.0) - 1.0, (rand3(block) * 2.0) - 1.0);
    gl_FragColor += 0.1*vec4(randomDelta, 0.0)*variables.noiseIntensity;

    //gl_FragColor = texture2D(sTexture, texCoord);
}

/*
컬러
블러
포커스
색수차? = 옛날 사진 느낌
노이즈
*/