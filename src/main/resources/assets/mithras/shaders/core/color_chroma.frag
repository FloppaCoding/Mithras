#version 400

in VERTEX_DATA {
    vec4 vertexColor;
} fs_in;

out vec4 fragColor;

uniform int ColorEffect;
#include "chroma.glsl"

void main() {
    vec4 color = fs_in.vertexColor;
    if(ColorEffect == 1) {
        color.rgb = chroma_color();
    }
    fragColor = color;
}