#version 450

const float ONE_OVER_SQRT_2 = 0.7071068;
const float SQRT_2 = 1.414214;
const float PI_QUATER = 0.7853982;
const mat4x2 offsets = mat4x2(vec2(1.0,1.0), vec2(1.0,-1.0), vec2(-1.0,-1.0), vec2(-1.0,1.0)) * ONE_OVER_SQRT_2;
const mat4x2 startDirs = mat4x2(vec2(0.0,1.0), vec2(1.0,0.0), vec2(0.0,-1.0), vec2(-1.0,0.0));

layout(lines) in;
layout(triangle_strip, max_vertices = 64) out;

in VERTEX_DATA {
    vec4 vertexColor;
} gs_in[];

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat2 UnitTransform;
// Half of the desired line width
uniform float HalfWidth;
uniform vec2 ScreenSize;
uniform vec4 radius;

out VERTEX_DATA {
    vec4 vertexColor;
} gs_out;

void main() {
    // Transformed unit vectors scaled with the width of the border.
    mat2x4 unitVecs = {
        ProjMat * ModelViewMat * vec4(UnitTransform[0], 0.0, 0.0),
        ProjMat * ModelViewMat * vec4(UnitTransform[1], 0.0, 0.0)
    };
    ivec2 corners = ivec2(gl_PrimitiveIDIn, (gl_PrimitiveIDIn + 1) % 4);
    // Direction pointing inwards from both vertices of this line segment.
    mat2 inward = { offsets[corners[0]], offsets[corners[1]] };
    // Those same direction transformed to the local coordinate system.
    mat2x4 directions = unitVecs * inward;

    vec2 radii = { radius[corners[0]], radius[corners[1]] };

    mat2 startDir = {inward[0], startDirs[corners[1]]};

    mat2x4 midpoints = {
        gl_in[0].gl_Position + SQRT_2 * directions[0]*radii[0]*gl_in[0].gl_Position.w,
        gl_in[1].gl_Position + SQRT_2 * directions[1]*radii[1]*gl_in[1].gl_Position.w
    };
    float size;
    int segments;
    // Entries of the rotation matrix
    float segmentAngle, c, s;
    vec2 segDir[3];
    mat2 rotationMat;
    vec2 dir;
    vec4 shift;
    float width;
    vec4 altCenter;
    for (int ii = 0; ii < 2; ii++) {
        size = dot(max(abs(unitVecs[0].xy), abs(unitVecs[1].xy)), ScreenSize)*(radii[ii]+HalfWidth);
        segments = min(int(ceil(sqrt(floor(size)) * 0.43)), 16);
        // If 0 segments / sharp corner, the width has to be longer because it is diagonal.
        width = HalfWidth * ( 1.0 + SQRT_2*float(segments < 1));

        gs_out.vertexColor = gs_in[ii].vertexColor;

        segmentAngle = PI_QUATER / segments;
        c = cos(segmentAngle);
        s = sin(segmentAngle);
        rotationMat = mat2(c, -s, s, c);

        dir = -startDir[ii] * float(segments >= 1) - inward[ii]*float(segments < 1);

        for (int jj = 0; jj < segments+1; jj++) {
            shift = unitVecs * dir;

            // Effective conditional for putting the inner position in the correct position when half width > radius.
            // This is required to prevent overlap in that case.
            gl_Position = midpoints[ii] + ( -SQRT_2 * directions[ii]*float(width > radii[ii]) + shift *float(width <= radii[ii]))*(radii[ii] - width) * midpoints[ii].w;
            EmitVertex();
            // Outer vertex of the arc.
            gl_Position = midpoints[ii] + shift*(radii[ii] + width) * midpoints[ii].w;
            EmitVertex();

            dir = rotationMat*dir;
        }
    }
    EndPrimitive();
}