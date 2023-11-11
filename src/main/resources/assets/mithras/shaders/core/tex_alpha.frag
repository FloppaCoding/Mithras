#version 400

uniform sampler2D Sampler0;

uniform float alpha;

in VERTEX_DATA {
    vec2 texCoord0;
} fs_in;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, fs_in.texCoord0);
    color.a *= alpha;
    fragColor = color;
}