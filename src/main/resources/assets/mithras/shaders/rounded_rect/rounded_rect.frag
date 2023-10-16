#version 400

in vec4 vertexColor;
in vec2 texCoord0;

uniform vec4 radius;
uniform vec2 halfDimensions;

out vec4 fragColor;

float sdRoundedBox( in vec2 p, in vec2 b, in vec4 r )
{
    r.xy = (p.x>0.0)?r.xy : r.zw;
    r.x  = (p.y>0.0)?r.x  : r.y;
    vec2 q = abs(p)-b+r.x;
    return min(max(q.x,q.y),0.0) + length(max(q,0.0)) - r.x;
}

void main() {
    vec4 color = vertexColor;

    float d = sdRoundedBox(texCoord0 - halfDimensions, halfDimensions, radius);

    color.a = smoothstep(1.0, 0.0, d);

    if (color.a == 0.0) {
        discard;
    }
    fragColor = color;
}
