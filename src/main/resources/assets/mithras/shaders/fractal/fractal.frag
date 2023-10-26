#version 450

layout(origin_upper_left) in vec4 gl_FragCoord;

uniform vec2 corner;
uniform float width;
uniform vec2 ScreenSize;

out vec4 fragColor;

vec2 f(in vec2 z, in vec2 c) {
    return vec2(z.x*z.x - z.y*z.y + c.x, 2.0*z.x*z.y + c.y);
}

float iterations(in vec2 c) {

    vec2 z = vec2(0.0);
    float n = 0.0;
    for(int i = 0; i<200; i++) {
        z = f(z,c);
        if (dot(z,z) > 4.) break;
        n += 1.0;
    }

    n = n - log2(log2(dot(z,z))) + 4.0;

    return n/200.0;
}

vec3 color(in float it) {
    return  0.5 + 0.5*cos( 3.0 + it*31.4 + vec3(0.0,0.6,1.0));
}

void main() {
    vec2 uv = gl_FragCoord.xy / ScreenSize.x * width + corner;
    float it = iterations(uv);

    vec3 col = color(it);

    fragColor = vec4(col, 1.0);
}
