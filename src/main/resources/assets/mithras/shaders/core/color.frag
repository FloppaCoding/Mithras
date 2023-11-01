#version 400

in VERTEX_DATA {
    vec4 vertexColor;
} fs_in;

out vec4 fragColor;

void main() {
    vec4 color = fs_in.vertexColor;
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color;
}