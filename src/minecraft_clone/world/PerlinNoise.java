package minecraft_clone.world;

import java.util.Random;

public class PerlinNoise {
    private int[] permutation;
    private Random random;

    public PerlinNoise(long seed) {
        permutation = new int[512];
        random = new Random(seed);
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        // Shuffle permutation
        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = p[j];
            p[i] = p[i];
            p[j] = temp;
        }
        // Duplicate for overflow
        for (int i = 0; i < 256; i++) {
            permutation[i] = permutation[i + 256] = p[i];
        }
    }

    public float noise(float x, float y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        float u = fade(x);
        float v = fade(y);

        int aa = permutation[X + permutation[Y]];
        int ab = permutation[X + permutation[Y + 1]];
        int ba = permutation[X + 1 + permutation[Y]];
        int bb = permutation[X + 1 + permutation[Y + 1]];

        float gradAA = grad(permutation[aa], x, y);
        float gradBA = grad(permutation[ba], x - 1, y);
        float gradAB = grad(permutation[ab], x, y - 1);
        float gradBB = grad(permutation[bb], x - 1, y - 1);

        float lerpX1 = lerp(gradAA, gradBA, u);
        float lerpX2 = lerp(gradAB, gradBB, u);
        return lerp(lerpX1, lerpX2, v);
    }

    private float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    private float grad(int hash, float x, float y) {
        int h = hash & 15;
        float u = h < 8 ? x : y;
        float v = h < 4 ? y : h == 12 || h == 14 ? x : 0;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
