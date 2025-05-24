layout (location = 0) in vec3 aPos;  // Position (x, y, z)
layout (location = 1) in vec2 aUV;   // UV coordinates

out vec2 texCoords;

uniform vec2 screenSize;

void main() {
    vec2 normalizedPos = (aPos.xy / screenSize) * 2.0 - 1.0;
    gl_Position = vec4(normalizedPos.x, -normalizedPos.y, 0.0, 1.0); // Flip y-axis
    texCoords = aUV;
}