#version 460

#define PI 3.1415925

layout(points) in;
layout(triangle_strip, max_vertices = 255) out;

in VERTEX_DATA {
    vec4 vertexColor;
    vec2 texCoord0;
    vec2 texCoord1;
} gs_in[];

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 ScreenSize;

out VERTEX_DATA {
    vec4 vertexColor;
} gs_out;

/*
* This shader creates a triangle stip approximating a circle for every point primitive it receives.
* The number of segments in the approximation is automatically scaled with the size of the resulting circle in pixels.
*
* Author: Aton
*/
void main() {
    gs_out.vertexColor = gs_in[0].vertexColor;
    vec4 midPoint = gl_in[0].gl_Position;

    // a and b semi-axes in clip coordinates.
    vec4 a = ProjMat * ModelViewMat * vec4(gs_in[0].texCoord0, 0.0, 0.0);
    vec4 b = ProjMat * ModelViewMat * vec4(gs_in[0].texCoord1, 0.0, 0.0);

    vec2 dir1 = a.xy;
    vec2 dir2 = b.xy;

    // The length of the major-axis of the ellipse in pixels.
    float size = dot(max(abs(dir1), abs(dir2)), ScreenSize);
    int segments;
    if(size <= 10.0) segments = 8;
    else if (size <= 30.0) segments = 16;
    else if (size <= 50.0) segments = 24;
    else if (size <= 100.0) segments = 32;
    else if (size <= 200.0) segments = 48;
    else if (size <= 400.0) segments = 96;
    else segments = 127;

    // Entries of the rotation matrix
    float segmentAngle = 2.0 * PI / segments;
    float c = cos(segmentAngle);
    float s = sin(segmentAngle);
    vec2 segDir = vec2(1.0, 0.0);
    mat2 rotationMat = mat2(c, s, -s, c);

    gl_Position = vec4( dir1 * midPoint.w  + midPoint.xy, midPoint.zw);
    EmitVertex();

    for (int segment = 0; segment < segments; segment++) {
        gl_Position = midPoint;
        EmitVertex();

        segDir = rotationMat * segDir;

        gl_Position = vec4( (segDir.x * dir1 + segDir.y * dir2) * midPoint.w + midPoint.xy, midPoint.zw);
        EmitVertex();
    }
    EndPrimitive();
}
