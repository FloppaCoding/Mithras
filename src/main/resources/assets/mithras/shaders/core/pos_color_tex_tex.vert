#version 400

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec2 UV1;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out VERTEX_DATA {
    vec4 vertexColor;
    vec2 texCoord0;
    vec2 texCoord1;
} vs_out;


void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vs_out.vertexColor = Color;
    vs_out.texCoord0 = UV0;
    vs_out.texCoord1 = UV1;
}
