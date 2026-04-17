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
	//float pix_size = fwidth(dist);
	//float aa_corner_spread = pix_size * 5.0;
	//vec2 q0 = q + aa_corner_spread;
	//float corner_factor = smoothstep(0.0, aa_corner_spread, min(q0.x,q0.y)) * min(step(0.0, q0.x), step(0.0, q0.y));
    //float softness = mix(0.0, pix_size * 0.5, corner_factor);
    float softness = fwidth(dist) * 0.5;
    float alpha = 1.0 - smoothstep(-softness, softness, dist);

    if (alpha <= 0.0) discard;

    float outline_width = FragSize.z;

    // Calculate outline
    if (outline_width > 0.0) {
        // Distance to the inner edge of the outline
        float inner_dist = dist + outline_width;

        // Alpha for the inner edge (where fill starts)
        float inner_alpha = 1.0 - smoothstep(-softness * 2.0, 0.0, inner_dist);

        // Determine if we're in the outline region
        float outline_alpha = alpha - inner_alpha;

		// Clip inner body to it's bounds
		//vec2 inner_pixel_pos = pixel_pos - outline_width;
		//vec2 inner_size = FragSize.xy - outline_width * 2.0;
		//float outside_inner = 1.0 - step(0.0, inner_pixel_pos.x) * step(0.0, inner_pixel_pos.y) * step(inner_pixel_pos.x, inner_size.x) * step(inner_pixel_pos.y, inner_size.y);
		//outline_alpha = max(outline_alpha, outside_inner);

        // Mix between outline color and fill color
		// min(1, max(outline_alpha, inner_dist)) -- inner_dist here is to eliminate blending with inner color on the outside
        vec3 final_color = mix(FragColor.rgb, FragOutlineColor.rgb, min(1.0, max(outline_alpha, inner_dist)));
        float final_alpha = alpha * mix(FragColor.a, FragOutlineColor.a, outline_alpha);

        OutColor = vec4(final_color, final_alpha);
    } else {
        // No outline, just use the fill color
        OutColor = vec4(FragColor.rgb, FragColor.a * alpha);
    }
	//OutColor = vec4(fract(pixel_pos),0,1);
}
