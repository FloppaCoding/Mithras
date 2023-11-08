#version 460

in VERTEX_DATA {
    vec4 vertexColor;
    vec2 texCoord0;
} fs_in;

uniform sampler2D Sampler0;
uniform float AAWidth;

out vec4 fragColor;

void main() {
    float sdf = texture(Sampler0, fs_in.texCoord0).r;
    float alpha = smoothstep(0.5-AAWidth, 0.5 + AAWidth, sdf);
    if (alpha == 0.0) {
        discard;
    }
    vec4 color = fs_in.vertexColor;
    color.a *= alpha;
    fragColor = color;
}