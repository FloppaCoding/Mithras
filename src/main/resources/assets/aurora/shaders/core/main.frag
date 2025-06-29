#version 450

#define COLOR_SOURCE_BIT_SHIFT 8

const int COLOR_ALPHA_BIT = 0x10;
const int TEXT_BIT = 0x20;
const int COLOR_BIT = 0x0;
const int COLOR_SOURCE_MASK = 0xff;

in VERTEX_DATA {
    vec4 vertexColor;
    vec2 texCoord0;
} fs_in;

uniform sampler2D Sampler0;
uniform float AAWidth;
uniform int Mode;
uniform vec4 TopLeftColor;
uniform vec4 TopRightColor;
uniform vec4 BottomLeftColor;
uniform vec4 BottomRightColor;

out vec4 fragColor;

#include "chroma.glsl"

void main() {
    vec4 color;
    if(Mode == 0) { // This is technically not required, maybe remove it?!
        fragColor = fs_in.vertexColor;
        return;
    }
    // Color Source
    switch( (Mode >> COLOR_SOURCE_BIT_SHIFT) & COLOR_SOURCE_MASK) {
        case 0: // Vertex Color determines color.
            color = fs_in.vertexColor;
            break;
        case 1: // Texture determines color.
            color = texture(Sampler0, fs_in.texCoord0);
            break;
        case 2: // Chroma determines color.
            color = vec4(chroma_color(), 1.0);
            break;
        case 3: // Horizontal fade determines color.
            color = vec4(mix(TopLeftColor, TopRightColor, fs_in.texCoord0.s));
            break;
        case 4: // Vertical fade determines color.
            color = vec4(mix(TopLeftColor, BottomLeftColor, fs_in.texCoord0.t));
            break;
        case 5: // Four color fade determines color.
            color = vec4(mix(
                mix(TopLeftColor, TopRightColor, fs_in.texCoord0.s),
                mix(BottomLeftColor, BottomRightColor, fs_in.texCoord0.s),
                fs_in.texCoord0.t)
            );
            break;
    }

    if(bool(Mode & TEXT_BIT)) {
        float sdf = texture(Sampler0, fs_in.texCoord0).r;
        color.a *= smoothstep(0.5-AAWidth, 0.5 + AAWidth, sdf);
    }
    if(bool(Mode & COLOR_ALPHA_BIT)) {
        color.a *= fs_in.vertexColor.a;
    }
    fragColor = color;
}