#version 460

#define PI_HALF 1.5707963
// Change this to a 1 to interpolate the color along with width of the line.
// Usually there should not be a color gradient so interpolation can be omitted.
#define INTERPOLATE_COLOR 0

layout(triangles) in;
layout(triangle_strip, max_vertices = 35) out;

in int gl_PrimitiveIDIn[];

in VERTEX_DATA {
    vec4 vertexColor;
} gs_in[];

uniform int style;
uniform int lastSegment;
uniform float LineWidth;
uniform vec2 ScreenSize;

out VERTEX_DATA {
    vec4 vertexColor;
} gs_out;

/*
* This shader constructs line caps when required.
* When the input triangle is the end of a line a line cap will be constructed.
* Round line caps are achieved by approximating a half circle through triangle segments.
*
* Author: Aton
*/
void main() {
    // First passing through the input triangle.
    gs_out.vertexColor = gs_in[0].vertexColor;
    gl_Position = gl_in[0].gl_Position;
    EmitVertex();
    gs_out.vertexColor = gs_in[1].vertexColor;
    gl_Position = gl_in[1].gl_Position;
    EmitVertex();
    gs_out.vertexColor = gs_in[2].vertexColor;
    gl_Position = gl_in[2].gl_Position;
    EmitVertex();
    EndPrimitive();

    if(style == 0) { return; }
    bool hasCap = lastSegment == 0 || gl_PrimitiveIDIn[0] == 0 || gl_PrimitiveIDIn[0] == lastSegment;
    if(!hasCap) { return; }
    if(style == 1) {
        vec4 midPoint = (gl_in[1].gl_Position + gl_in[0].gl_Position) / 2.0;

        // vector from mid point to point 1
        vec2 dir1 = (gl_in[1].gl_Position.xy / gl_in[1].gl_Position.w  - gl_in[0].gl_Position.xy / gl_in[0].gl_Position.w) / 2.0;
        // vector to the apex of the line cap
        vec2 dir2 = normalize((gl_in[0].gl_Position.xy / gl_in[0].gl_Position.w  - gl_in[2].gl_Position.xy / gl_in[2].gl_Position.w) * ScreenSize) * LineWidth / ScreenSize;

        int half_segments;
        if (LineWidth <= 10.0) half_segments = 2;
        else if (LineWidth <= 30.0) half_segments = 4;
        else if (LineWidth <= 50.0) half_segments = 6;
        else if (LineWidth <= 100.0) half_segments = 8;
        else if (LineWidth <= 200.0) half_segments = 12;
        else half_segments = 16;

        // Entries of the rotation matrix
        float segmentAngle = PI_HALF / half_segments;
        float c = cos(segmentAngle);
        float s = sin(segmentAngle);
        vec2 segDir = vec2(1.0, 0.0);
        mat2 rotationMat = mat2(c, s, -s, c);

        gl_Position = vec4( dir2 * midPoint.w  + midPoint.xy, midPoint.zw);
        gs_out.vertexColor = gs_in[1].vertexColor;
        EmitVertex();

        for (int segment = 0; segment < half_segments; segment++) {
            segDir = rotationMat * segDir;

            gl_Position = vec4( (segDir.x * dir2 + segDir.y * dir1) * midPoint.w + midPoint.xy, midPoint.zw);
            #if INTERPOLATE_COLOR
                gs_out.vertexColor = mix(gs_in[0].vertexColor, gs_in[1].vertexColor, (segDir.y + 1.0)/2.0);
            #endif
            EmitVertex();
            gl_Position = vec4( (segDir.x * dir2 - segDir.y * dir1) * midPoint.w + midPoint.xy, midPoint.zw);
            #if INTERPOLATE_COLOR
                gs_out.vertexColor = mix(gs_in[0].vertexColor, gs_in[1].vertexColor, (-segDir.y + 1.0)/2.0);
            #endif
            EmitVertex();
        }
        EndPrimitive();
    }

}
