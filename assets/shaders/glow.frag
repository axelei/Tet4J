#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec2 u_texelSize;
uniform float u_time;
uniform float u_hue;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec4 color = texture2D(u_texture, v_texCoords) * v_color;
    vec2 ts = u_texelSize;

    if (color.a > 0.05) {
        gl_FragColor = color;
        return;
    }

    float pulse = 1.75 + sin(u_time * 2.0) * 0.75;
    float glow = 0.0;

    for (int i = 1; i <= 6; i++) {
        float r = float(i * 5) * pulse;
        float w = 1.0 / float(i);
        glow += texture2D(u_texture, v_texCoords + vec2( r, 0.0) * ts).a * w;
        glow += texture2D(u_texture, v_texCoords + vec2(-r, 0.0) * ts).a * w;
        glow += texture2D(u_texture, v_texCoords + vec2( 0.0, r) * ts).a * w;
        glow += texture2D(u_texture, v_texCoords + vec2( 0.0,-r) * ts).a * w;
    }

    glow = clamp(glow * 0.5, 0.0, 1.0);

    if (glow > 0.01) {
        vec3 glowRgb = hsv2rgb(vec3(u_hue, 1.0, 1.0));
        gl_FragColor = vec4(glowRgb, glow);
    } else {
        discard;
    }
}
