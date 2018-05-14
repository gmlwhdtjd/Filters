precision mediump float;
uniform vec2 randxy;
uniform sampler2D sTexture;
uniform sampler2D sNoiseTexture;
varying vec2 texCoord;

struct Filter_Var {
    vec3 rgb;
    float colorRatio;
    float brightness;
    float saturation;
    float aberration;
    float noiseIntensity;
};

uniform Filter_Var variables;

vec3 RGBtoHSV(vec3 c)
{
    vec4 K = vec4(0.0, -0.333, 0.666, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float m = min(q.w, q.y);
    float d = q.x - m;
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 HSVtoRGB(vec3 c)
{
    vec4 K = vec4(1.0, 0.666, 0.333, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main ()
{
    vec2 dis = texCoord.xy;
    dis -= 0.5;
    dis *= variables.aberration;
    vec3 target = vec3(texture2D(sTexture, texCoord-dis).r,
                        texture2D(sTexture, texCoord).g,
                        texture2D(sTexture, texCoord+dis).b);

    vec2 noisexy = texture2D(sNoiseTexture, fract(texCoord*2.0)+randxy).xy;
    vec3 noisetexture = texture2D(sNoiseTexture, noisexy).rgb;
    target += (noisetexture*2.0-1.0)*variables.noiseIntensity;

    target += variables.brightness;
    target = (1.0-variables.colorRatio)*target + variables.colorRatio*variables.rgb;

    target = RGBtoHSV(target);
    target.y *= 2.0*variables.saturation;
    target = HSVtoRGB(target);

    target = clamp(target, 0.0, 1.0);

    gl_FragColor = vec4(target, 1.0);
}