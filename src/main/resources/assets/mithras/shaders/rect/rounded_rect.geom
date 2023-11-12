#version 450

/*
This shader rounds the corners of input tirnangles.
It is designed to be used for drawing rounded quads.

Author: Aton
*/

#define MAX_VERTICES 67
#define INTERPOLATE_COLOR 0
#define TEXTURE_MODE 0

const float ONE_OVER_SQRT_2 = 0.7071068;
const float PI_HALF = 1.5707963;
const float PI_QUATER = 0.7853982;

const mat4x2 offsets = mat4x2(vec2(1.0,1.0), vec2(1.0,-1.0), vec2(-1.0,-1.0), vec2(-1.0,1.0));

layout(triangles) in;
layout(triangle_strip, max_vertices = MAX_VERTICES) out;

//in int gl_PrimitiveIDIn[];

in VERTEX_DATA {
#if TEXTURE_MODE
    vec2 texCoord0;
#else
    vec4 vertexColor;
#endif
} gs_in[];

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 ScreenSize;
uniform mat2 UnitTransform;
uniform vec4 radius;

out VERTEX_DATA {
#if TEXTURE_MODE
    vec2 texCoord0;
#else
    vec4 vertexColor;
#endif
} gs_out;

#if TEXTURE_MODE || INTERPOLATE_COLOR
    #include "barycentrics.glsl"

    #if TEXTURE_MODE
        vec2 lerpTex(in vec2 p) {
            vec3 bary;
            calculateBarycentrics(p, bary);
            return bary.s * gs_in[0].texCoord0 + bary.t * gs_in[1].texCoord0 + bary.p * gs_in[2].texCoord0;
        }
    #else
        vec4 lerpColor(in vec2 p) {
            vec3 bary;
            calculateBarycentrics(p, bary);
            return bary.s * gs_in[0].vertexColor + bary.t * gs_in[1].vertexColor + bary.p * gs_in[2].vertexColor;
        }
    #endif
#endif

void main() {
#if TEXTURE_MODE || INTERPOLATE_COLOR
    initializeBaryCache();
#else
    gs_out.vertexColor = gs_in[0].vertexColor;
#endif

    // transformed unit vectors.
    vec4 e1 = ProjMat * ModelViewMat * vec4(UnitTransform[0], 0.0, 0.0);
    vec4 e2 = ProjMat * ModelViewMat * vec4(UnitTransform[1], 0.0, 0.0);

    // Perpendicular direction vectors for the circles.
    vec2 r1[3], r2[3];
    vec4 midPoints[3];
    float size;
    // Number of segments for the quater circle used for the corner.
    int segments[3], corners[3];
    int corner;
    for (int ii = 0; ii < 3; ii++) {
        corner = ii + (2*(gl_PrimitiveIDIn % 2))%4;
        r1[ii] = radius[corner] * e1.xy;
        r2[ii] = radius[corner] * e2.xy;
        midPoints[ii] = gl_in[ii].gl_Position + vec4((offsets[corner].x * r1[ii] + offsets[corner].y * r2[ii]) * gl_in[ii].gl_Position.w, 0., 0.);
        size = dot(max(abs(r1[ii]), abs(r2[ii])), ScreenSize);
        if      (size <=   0.4) {
            segments[ii] =   1;
            corners[ii] =   1;
            continue;
        }
        else if (size <=  10.0) segments[ii] =   2;
        else if (size <=  30.0) segments[ii] =   4;
        else if (size <=  50.0) segments[ii] =   6;
        else if (size <= 100.0) segments[ii] =   8;
        else if (size <= 200.0) segments[ii] =  12;
        else if (size <= 400.0) segments[ii] =  24;
        else                    segments[ii] =  32;
        if (ii % 2 == 0) {
            segments[ii] = segments[ii] / 2;
        }
        corners[ii] = segments[ii]+1;
    }

    // Entries of the rotation matrix
    float segmentAngle, c, s;
    vec2 segDir[3];
    mat2 rotationMat[3];

    // 0
    segmentAngle = PI_QUATER / segments[0];
    c = cos(segmentAngle);
    s = sin(segmentAngle);
    rotationMat[0] = mat2(c, -s, s, c);

    // 1
    segmentAngle = PI_HALF / segments[1];
    c = cos(segmentAngle);
    s = sin(segmentAngle);
    rotationMat[1] = mat2(c, -s, s, c);

    // 2
    segmentAngle = PI_QUATER / segments[2];
    c = cos(segmentAngle);
    s = sin(segmentAngle);
    rotationMat[2] = mat2(c, -s, s, c);

    // Start positions on the circle.
    if( gl_PrimitiveIDIn % 2 == 0) {
        segDir[0] = vec2(-ONE_OVER_SQRT_2, -ONE_OVER_SQRT_2);
        segDir[1] = vec2(-1.0, 0.0);
        segDir[2] = vec2(0.0, 1.0);
    }else {
        segDir[0] = vec2(ONE_OVER_SQRT_2, ONE_OVER_SQRT_2);
        segDir[1] = vec2(1.0, 0.0);
        segDir[2] = vec2(0.0, -1.0);
    }

    // Generate all vertices in ccw order.
    const int totalVertices = corners[0] + corners[1] + corners[2];
    vec4 vertices[MAX_VERTICES];
    int position = 0;
    for (int ii = 0; ii < 3; ii++) {
        for (int jj = 0; jj < corners[ii]; jj++) {
            vertices[position] = vec4( (segDir[ii].x * r1[ii] + segDir[ii].y * r2[ii]) * midPoints[ii].w + midPoints[ii].xy, midPoints[ii].zw);
            segDir[ii] = rotationMat[ii] * segDir[ii];
            position++;
        }
    }

    // Emit vertices alternating from both ends to the center of the list.
    int opposite;
    for (int jj = 0; jj < totalVertices / 2; jj++) {
        gl_Position = vertices[jj];
        #if TEXTURE_MODE
        gs_out.texCoord0 = lerpTex(vertices[jj].xy / vertices[jj].w);
        #elif  INTERPOLATE_COLOR
        gs_out.vertexColor = lerpColor(vertices[jj].xy / vertices[jj].w);
        #endif
        EmitVertex();
        opposite = totalVertices - 1 - jj;
        gl_Position = vertices[opposite];
        #if TEXTURE_MODE
        gs_out.texCoord0 = lerpTex(vertices[opposite].xy / vertices[opposite].w);
        #elif  INTERPOLATE_COLOR
        gs_out.vertexColor = lerpColor(vertices[opposite].xy / vertices[opposite].w);
        #endif
        EmitVertex();
    }

    // If uneven number of totalVertices the one in the middle was missed by the for loop.
    if (totalVertices % 2 != 0) {
        opposite = totalVertices /2;
        gl_Position = vertices[opposite];
        #if TEXTURE_MODE
        gs_out.texCoord0 = lerpTex(vertices[opposite].xy / vertices[opposite].w);
        #elif  INTERPOLATE_COLOR
        gs_out.vertexColor = lerpColor(vertices[opposite].xy / vertices[opposite].w);
        #endif
        EmitVertex();
    }

    EndPrimitive();
}
