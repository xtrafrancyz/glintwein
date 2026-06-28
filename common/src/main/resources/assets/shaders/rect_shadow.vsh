#version 130

in vec2 Position;
in vec4 Color;          // Shadow Color (usually black with low alpha)
in vec4 Radius;         // Corner radii
in vec3 Size;           // xy: Expanded Size, z: Sigma (Blur radius)

uniform mat4 ProjMat;

out vec3 FragSize;
out vec4 FragRadius;
out vec2 FragCoord;
out vec4 FragColor;

const vec2[4] RECT_VERTICES_COORDS = vec2[](
    vec2(0.0, 0.0),
    vec2(0.0, 1.0),
    vec2(1.0, 1.0),
    vec2(1.0, 0.0)
);

void main() {
    FragSize = Size;
    FragRadius = Radius;
    FragCoord = RECT_VERTICES_COORDS[gl_VertexID % 4];
    FragColor = Color;

    gl_Position = ProjMat * vec4(Position, 0.0, 1.0);
}
