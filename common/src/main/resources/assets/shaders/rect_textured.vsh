#version 130

in vec2 Position;
in vec4 Color;
in vec2 UV0;
in vec4 Radius; // Top-left, Top-right, Bottom-right, Bottom-left
in vec3 Size;
in vec4 OutlineColor;

uniform mat4 ProjMat;

out vec3 FragSize;
out vec4 FragRadius;
out vec2 TexCoord;
out vec2 FragCoord;
out vec4 FragColor;
out vec4 FragOutlineColor;

const vec2 RECT_VERTICES_COORDS[4] = vec2[](
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
    TexCoord = UV0;
    FragCoord = rvertexcoord(gl_VertexID);
    FragColor = Color;
    FragOutlineColor = OutlineColor;

    gl_Position = ProjMat * vec4(Position, 0.0, 1.0);
}
