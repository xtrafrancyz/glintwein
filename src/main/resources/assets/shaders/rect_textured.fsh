#version 130

in vec3 FragSize;
in vec4 FragRadius; // x: top-left, y: top-right, z: bottom-right, w: bottom-left
in vec2 TexCoord;
in vec2 FragCoord;
in vec4 FragColor;
in vec4 FragOutlineColor;

uniform sampler2D Texture;

out vec4 OutColor;

// Helper function to get the radius for the specific quadrant
float get_radius(vec2 p, vec4 r) {
    if (p.x < 0.5 && p.y < 0.5) return r.x; // Top-Left
    if (p.x > 0.5 && p.y < 0.5) return r.y; // Top-Right
    if (p.x > 0.5 && p.y > 0.5) return r.z; // Bottom-Right
    return r.w;                            // Bottom-Left
}

void main() {
    // Convert normalized 0->1 coordinates to pixel space
    vec2 pixel_pos = FragCoord * FragSize.xy;

    // Pick the correct radius based on which corner we are closer to
    float radius = get_radius(FragCoord, FragRadius);

    // Calculate the center-relative position
    // We use the absolute distance from the center to mirror the logic across axes
    vec2 half_size = FragSize.xy * 0.5;
    vec2 q = abs(pixel_pos - half_size) - half_size + radius;

    // Standard SDF for a rounded corner
    float dist = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;

    // Smoothstep for anti-aliasing. AA size is 0.5 pixels in the corners, and 0 in the straight edges
    float corner_factor = step(0.0, q.x) * step(0.0, q.y);
    float softness = mix(0.0, fwidth(dist) * 0.5, corner_factor);
    float alpha_mask = 1.0 - smoothstep(-softness, softness, dist);

    if (alpha_mask <= 0.0) discard;

    vec4 tex_sample = texture(Texture, TexCoord);

    float outline_width = FragSize.z;
    if (outline_width > 0.0) {
        // Distance to the inner edge of the outline
        float inner_dist = dist + outline_width;

        // Alpha for the inner edge (where textured fill starts)
        float inner_alpha = 1.0 - smoothstep(-softness, softness, inner_dist);

        // Determine if we're in the outline region
        float outline_alpha = alpha_mask - inner_alpha;

        // In outline region: use outline color
        // In fill region: use texture * FragColor
        vec4 fill_color = tex_sample * FragColor;
        vec4 final_color = mix(fill_color, FragOutlineColor, max(outline_alpha, inner_dist));

        OutColor = final_color;
        OutColor.a *= alpha_mask;
    } else {
        // No outline, just use the textured fill
        OutColor = tex_sample * FragColor;
        OutColor.a *= alpha_mask;
    }
}
