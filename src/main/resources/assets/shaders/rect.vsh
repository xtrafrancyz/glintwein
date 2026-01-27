#version 130

in vec2 Position;
in vec4 Color;
in vec4 Radius; // Top-left, Top-right, Bottom-right, Bottom-left
in vec2 Size;

uniform mat4 ProjMat;

out vec2 FragSize;
out vec4 FragRadius;
out vec2 FragCoord;
out vec4 FragColor;

const vec2[4] RECT_VERTICES_COORDS = vec2[](
vec2(0.0, 0.0),
vec2(0.0, 1.0),
vec2(1.0, 1.0),
vec2(1.0, 0.0)
);

vec2 rvertexcoord(int id) {
    return RECT_VERTICES_COORDS[id % 4];
}

void main() {
    FragSize = Size;
    FragRadius = Radius;
    FragCoord = rvertexcoord(gl_VertexID);
    FragColor = Color;

    gl_Position = ProjMat * vec4(Position, 0.0, 1.0);
}
