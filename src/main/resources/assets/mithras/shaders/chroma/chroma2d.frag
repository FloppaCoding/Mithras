#version 400

in VERTEX_DATA {
    vec4 vertexColor;
} fs_in;

out vec4 fragColor;

#include "chroma.glsl"

void main() {
    fragColor = vec4(chroma_color(), fs_in.vertexColor.a);
}