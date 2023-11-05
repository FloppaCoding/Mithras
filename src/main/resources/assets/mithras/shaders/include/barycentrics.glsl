#version 430

struct Bary_Cache {
    vec2 a;
    vec2 v0;
    vec2 v1;
    float d00;
    float d01;
    float d11;
    float scale;
} bary_cache = Bary_Cache(vec2(0.0, 0.0), vec2(0.0, 0.0), vec2(0.0, 0.0), 0.0, 0.0, 0.0, 0.0);


void calculateBarycentrics(in vec2 p, out vec3 bary) {
    vec2 v2 = p - bary_cache.a;
    float d20 = dot(v2, bary_cache.v0);
    float d21 = dot(v2, bary_cache.v1);
    // Barycentric coordinates.
    bary.t = (bary_cache.d11 * d20 - bary_cache.d01 * d21) * bary_cache.scale;
    bary.p = (bary_cache.d00 * d21 - bary_cache.d01 * d20) * bary_cache.scale;
    bary.s = 1.0f - bary.t - bary.p;
}

void initializeBaryCache() {
    bary_cache.a = gl_in[0].gl_Position.xy / gl_in[0].gl_Position.w;
    bary_cache.v0 = gl_in[1].gl_Position.xy / gl_in[1].gl_Position.w - gl_in[0].gl_Position.xy / gl_in[0].gl_Position.w;
    bary_cache.v1 = gl_in[2].gl_Position.xy / gl_in[2].gl_Position.w - gl_in[0].gl_Position.xy / gl_in[0].gl_Position.w;
    bary_cache.d00 = dot(bary_cache.v0, bary_cache.v0);
    bary_cache.d01 = dot(bary_cache.v0, bary_cache.v1);
    bary_cache.d11 = dot(bary_cache.v1, bary_cache.v1);
    bary_cache.scale = 1.0 / ( bary_cache.d00 * bary_cache.d11 - bary_cache.d01* bary_cache.d01);
}