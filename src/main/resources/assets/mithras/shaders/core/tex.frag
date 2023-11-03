#version 400

uniform sampler2D Sampler0;

in VERTEX_DATA {
    vec2 texCoord0;
} fs_in;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, fs_in.texCoord0);
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color;
}