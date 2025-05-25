in vec2 passTextureCoords;
in vec4 passColor;

out vec4 fragColor;

uniform sampler2D textureSampler;

void main() {
    vec4 textureColor = texture(textureSampler, passTextureCoords);
    
    // Combine texture color with vertex color (including alpha)
    fragColor = textureColor * passColor;
    
    // Discard fully transparent fragments
    if (fragColor.a < 0.01) {
        discard;
    }
}