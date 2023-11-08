#version 460

in VERTEX_DATA {
    vec4 vertexColor;
    vec2 texCoord0;
} fs_in;

uniform sampler2D Sampler0;

out vec4 fragColor;

void main() {
    float alpha = texture(Sampler0, fs_in.texCoord0).r;
    if (alpha == 0.0) {
        discard;
    }
    vec4 color = fs_in.vertexColor;
    color.a *= alpha;
    fragColor = color;
}