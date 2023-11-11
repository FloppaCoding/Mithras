#version 460

in VERTEX_DATA {
    vec4 vertexColor;
    vec2 texCoord0;
} fs_in;

uniform sampler2D Sampler0;
uniform float AAWidth;

out vec4 fragColor;

uniform int ColorEffect;
#include "chroma.glsl"

void main() {
    float sdf = texture(Sampler0, fs_in.texCoord0).r;
    float alpha = smoothstep(0.5-AAWidth, 0.5 + AAWidth, sdf);
    vec4 color = fs_in.vertexColor;
    color.a *= alpha;
    if(ColorEffect == 1) {
        color.rgb = chroma_color();
    }
    fragColor = color;
}