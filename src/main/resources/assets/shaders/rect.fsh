#version 130

in vec3 FragSize;
in vec4 FragRadius; // x: top-left, y: top-right, z: bottom-right, w: bottom-left
in vec2 FragCoord;
in vec4 FragColor;
in vec4 FragOutlineColor;

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
    float alpha = 1.0 - smoothstep(-softness, softness, dist);

    if (alpha <= 0.0) discard;

    float outline_width = FragSize.z;

    // Calculate outline
    if (outline_width > 0.0) {
        // Distance to the inner edge of the outline
        float inner_dist = dist + outline_width;

        // Alpha for the inner edge (where fill starts)
        float inner_alpha = 1.0 - smoothstep(-softness, softness, inner_dist);

        // Determine if we're in the outline region
        float outline_alpha = alpha - inner_alpha;

        // Mix between outline color and fill color
        vec3 final_color = mix(FragColor.rgb, FragOutlineColor.rgb, outline_alpha);
        float final_alpha = alpha * mix(FragColor.a, FragOutlineColor.a, outline_alpha);

        OutColor = vec4(final_color, final_alpha);
    } else {
        // No outline, just use the fill color
        OutColor = vec4(FragColor.rgb, FragColor.a * alpha);
    }
}
