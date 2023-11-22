#version 450

in vec2 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ProjMat;

out VERTEX_DATA {
    vec4 vertexColor;
    vec2 texCoord0;
} vs_out;

void main() {
//    vec4 pos = ProjMat * vec4(Position, 0.0, 1.0);
    gl_Position = ProjMat * vec4(Position, 0.0, 1.0);

//    switch(gl_VertexID % 3) {
//        case 0:
//            gl_Position = vec4(-0.5,0.0, 0.0, 1.0);
//            break;
//        case 1:
//            gl_Position = vec4(0.5,0.0, 0.0, 1.0);
//            break;
//        case 2:
//            gl_Position = vec4(0.0,1.0, 0.0, 1.0);
//            break;
//    }

    vs_out.vertexColor = Color;
    vs_out.texCoord0 = UV0;
}