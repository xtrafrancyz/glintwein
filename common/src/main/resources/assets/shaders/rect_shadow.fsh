#version 130

in vec3 FragSize;     // xy: expanded rect size (including spread), z: sigma (blur radius)
in vec4 FragRadius;   // corner radii: TL, TR, BR, BL
in vec2 FragCoord;    // 0..1 UV across the expanded quad
in vec4 FragColor;    // shadow color (RGBA)

out vec4 outColor;

// Box-shadow sigma - blur radius conversion.
// Browsers use roughly sigma = spread / 2.0, but you pass sigma directly.
// Increase SIGMA_SCALE if the falloff looks too sharp.
const float SIGMA_SCALE = 1.0;

// erf approximation (Abramowitz & Stegun 7.1.26, max error < 1.5e-7)
float erf(float x) {
    float ax  = abs(x);
    float t   = 1.0 / (1.0 + 0.3275911 * ax);
    float poly = t * (0.254829592
               + t * (-0.284496736
               + t * (1.421413741
               + t * (-1.453152027
               + t *  1.061405429))));
    float val = 1.0 - poly * exp(-ax * ax);
    return sign(x) * val;
}

// 1-D Gaussian CDF integral over [a, b] with std-dev sigma
// = 0.5 * (erf(b / (sqrt2 * sigma)) - erf(a / (sqrt2 * sigma)))
float boxBlur1D(float lo, float hi, float sigma) {
    const float INV_SQRT2 = 0.7071067811865476;
    float s = sigma * INV_SQRT2 * SIGMA_SCALE;
    return 0.5 * (erf(hi / s) - erf(lo / s));
}

// Rounded-rect SDF (per-corner radii)
// p : position relative to rect centre
// b : half-extents (w/2, h/2)
// r : corner radius for this quadrant
float roundedBoxSDF(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

// Gaussian shadow alpha for a rounded rect
// Analytically integrates the Gaussian over the rect interior.
// For sigma == 0 it collapses to a crisp alpha == 1 inside, 0 outside.
float shadowAlpha(vec2 pos, vec2 halfSize, float radius, float sigma) {
    if (sigma < 0.0001) {
        float dist = roundedBoxSDF(pos, halfSize, radius);
        float softness = fwidth(dist) * 0.5;
        return 1.0 - smoothstep(-softness, softness, dist);
    }

    // Approximate: separate the Gaussian blur along X and Y axes,
    // compute 1-D integrals, multiply. This matches how browsers do it
    // (it's exact for rectangles and a very good approximation for corners).
	float coverX = boxBlur1D(-halfSize.x - pos.x, halfSize.x - pos.x, sigma);
	float coverY = boxBlur1D(-halfSize.y - pos.y, halfSize.y - pos.y, sigma);
    float alpha  = coverX * coverY;

    // Soften the corners: reduce alpha by the amount the Gaussian "leaks"
    // outside the rounded corner. We sample the SDF and blend toward 0
    // at the corners using a falloff scaled to the blur sigma.
    float sdf        = roundedBoxSDF(pos, halfSize, radius);
    float cornerMask = 1.0 - smoothstep(-sigma, sigma, sdf);
    alpha = min(alpha, cornerMask);

    return clamp(alpha, 0.0, 1.0);
}

// Select per-vertex corner radius based on quadrant
// FragRadius: TL=x, TR=y, BR=z, BL=w
// FragCoord:  (0,0)=TL, (1,0)=TR, (1,1)=BR, (0,1)=BL
float selectRadius(vec2 uv) {
    if (uv.x < 0.5 && uv.y < 0.5) return FragRadius.x; // TL
    if (uv.x > 0.5 && uv.y < 0.5) return FragRadius.y; // TR
    if (uv.x > 0.5 && uv.y > 0.5) return FragRadius.z; // BR
    return FragRadius.w;                               // BL
}

void main() {
    float w     = FragSize.x;
    float h     = FragSize.y;
    float sigma = FragSize.z;

    // Convert UV to position relative to rect centre
    vec2 pos      = (FragCoord - 0.5) * vec2(w, h);
    vec2 halfSize = vec2(w, h) * 0.5 - sigma;

    float radius = selectRadius(FragCoord);
    radius = min(radius, min(halfSize.x, halfSize.y));

    float alpha = shadowAlpha(pos, halfSize, radius, sigma);

    outColor = vec4(FragColor.rgb, FragColor.a * alpha);
}
