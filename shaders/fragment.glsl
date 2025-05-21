in vec2 texCoords;
in vec3 vColor;
out vec4 fragColor;

uniform sampler2D textureSampler;

void main() {
    fragColor = texture(textureSampler, texCoords) * vec4(vColor, 1.0);
}