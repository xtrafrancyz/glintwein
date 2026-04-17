#version 130

in vec2 TexCoord;
in vec4 FragColor;

uniform sampler2D Atlas;
uniform float Range;
uniform float Thickness;
uniform float Smoothness;
uniform float OutlineThickness;
uniform vec4 OutlineColor;

out vec4 OutColor;

float median(vec3 color) {
    return max(min(color.r, color.g), min(max(color.r, color.g), color.b));
}

void main() {
    vec4 mtsdf = texture(Atlas, TexCoord);
    float dist = median(mtsdf.rgb) - 0.5 + Thickness;
    vec2 h = vec2(dFdx(TexCoord.x), dFdy(TexCoord.y)) * textureSize(Atlas, 0);
    float pixels = Range * 1.0 / length(h.xy);
    float alpha = smoothstep(-Smoothness, Smoothness, dist * pixels);
    vec4 color = vec4(FragColor.rgb, FragColor.a * alpha);

    if (OutlineThickness > 0.0) {
        color = mix(OutlineColor, FragColor, alpha);
        color.a *= smoothstep(-Smoothness, Smoothness, (dist + OutlineThickness) * pixels);
    }

    OutColor = color;
}
