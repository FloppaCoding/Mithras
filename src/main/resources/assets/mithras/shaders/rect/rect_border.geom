#version 450

/*
This a wide rectangle border from a line strip containing 4 segments.
The line strip is assumed to enclose a rectangle starting at the top left (lowest x and y) position and going ccw.

Author: Aton
*/

const vec2 offsets[4] = vec2[4](vec2(1.0,1.0), vec2(1.0,-1.0), vec2(-1.0,-1.0), vec2(-1.0,1.0));

layout(lines) in;
layout(triangle_strip, max_vertices = 4) out;

in VERTEX_DATA {
    vec4 vertexColor;
} gs_in[];

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat2 UnitTransform;
// Half of the desired line width
uniform float HalfWidth;

out VERTEX_DATA {
    vec4 vertexColor;
} gs_out;

void main() {
    // Transformed unit vectors scaled with the width of the border.
    mat2x4 unitVecs = {
        ProjMat * ModelViewMat * vec4(UnitTransform[0], 0.0, 0.0) * HalfWidth,
        ProjMat * ModelViewMat * vec4(UnitTransform[1], 0.0, 0.0) * HalfWidth
    };
    // Direction pointing inwards from both vertices of this line segment.
    mat2 corners = {
        offsets[gl_PrimitiveIDIn],
        offsets[(gl_PrimitiveIDIn + 1) % 4]
    };
    // Those same direction transformed to the local coordinate system.
    mat2x4 directions = unitVecs*corners;

    gs_out.vertexColor = vec4(float(gl_PrimitiveIDIn) / 5.0, 0.0, 0.0, 1.0);

    for (int ii = 0; ii < 2; ii++) {
//        gs_out.vertexColor = gs_in[ii].vertexColor;
        gl_Position = gl_in[ii].gl_Position + directions[ii] * gl_in[ii].gl_Position.w;
        EmitVertex();

        gl_Position = gl_in[ii].gl_Position - directions[ii] * gl_in[ii].gl_Position.w;
        EmitVertex();
    }
    EndPrimitive();
}