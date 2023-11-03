#version 400

in vec3 Position;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out VERTEX_DATA {
    vec2 texCoord0;
} vs_out;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vs_out.texCoord0 = UV0;
}
