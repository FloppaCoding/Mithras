#version 400

in VERTEX_DATA {
    vec4 vertexColor;
} fs_in;

out vec4 fragColor;

void main() {
    fragColor = fs_in.vertexColor;
}