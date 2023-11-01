#version 150

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out VERTEX_DATA {
    vec4 vertexColor;
} fs_out;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    fs_out.vertexColor = Color;
}
