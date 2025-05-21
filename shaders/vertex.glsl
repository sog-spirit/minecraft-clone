layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aUV;
layout (location = 2) in vec3 aColor;

out vec2 texCoords;
out vec3 vColor;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
    texCoords = aUV;
    vColor = aColor;
    gl_Position = projection * view * model * vec4(aPos, 1.0);
}