package minecraft_clone.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Frustum {
    private final Vector4f[] planes = new Vector4f[6];

    public static final int PLANE_LEFT = 0;
    public static final int PLANE_RIGHT = 1;
    public static final int PLANE_BOTTOM = 2;
    public static final int PLANE_TOP = 3;
    public static final int PLANE_NEAR = 4;
    public static final int PLANE_FAR = 5;

    public Frustum() {
        for (int i = 0; i < 6; i++) {
            planes[i] = new Vector4f();
        }
    }

    public void extractPlanes(Matrix4f projectionViewMatrix) {
        float[] m = new float[16];
        projectionViewMatrix.get(m);
        
        // Left plane
        planes[PLANE_LEFT].set(
            m[3] + m[0],
            m[7] + m[4],
            m[11] + m[8],
            m[15] + m[12]
        );
        
        // Right plane
        planes[PLANE_RIGHT].set(
            m[3] - m[0],
            m[7] - m[4],
            m[11] - m[8],
            m[15] - m[12]
        );
        
        // Bottom plane
        planes[PLANE_BOTTOM].set(
            m[3] + m[1],
            m[7] + m[5],
            m[11] + m[9],
            m[15] + m[13]
        );
        
        // Top plane
        planes[PLANE_TOP].set(
            m[3] - m[1],
            m[7] - m[5],
            m[11] - m[9],
            m[15] - m[13]
        );
        
        // Near plane
        planes[PLANE_NEAR].set(
            m[3] + m[2],
            m[7] + m[6],
            m[11] + m[10],
            m[15] + m[14]
        );
        
        // Far plane
        planes[PLANE_FAR].set(
            m[3] - m[2],
            m[7] - m[6],
            m[11] - m[10],
            m[15] - m[14]
        );
        
        // Normalize all planes
        for (int i = 0; i < 6; i++) {
            normalizePlane(planes[i]);
        }
    }

    private void normalizePlane(Vector4f plane) {
        float length = (float) Math.sqrt(plane.x * plane.x + plane.y * plane.y + plane.z * plane.z);
        if (length > 0) {
            plane.x /= length;
            plane.y /= length;
            plane.z /= length;
            plane.w /= length;
        }
    }

    public boolean isPointInside(Vector3f point) {
        for (int i = 0; i < 6; i++) {
            if (getDistanceToPlane(planes[i], point) < 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isSphereInside(Vector3f center, float radius) {
        for (int i = 0; i < 6; i++) {
            if (getDistanceToPlane(planes[i], center) < -radius) {
                return false;
            }
        }
        return true;
    }

    public boolean isAABBInside(Vector3f min, Vector3f max) {
        for (int i = 0; i < 6; i++) {
            Vector4f plane = planes[i];
            
            // Find the "positive vertex" - the corner of the AABB that's furthest along the plane normal
            Vector3f positiveVertex = new Vector3f();
            positiveVertex.x = (plane.x >= 0) ? max.x : min.x;
            positiveVertex.y = (plane.y >= 0) ? max.y : min.y;
            positiveVertex.z = (plane.z >= 0) ? max.z : min.z;
            
            // If the positive vertex is behind the plane, the entire AABB is outside
            if (getDistanceToPlane(plane, positiveVertex) < 0) {
                return false;
            }
        }
        return true;
    }

    private float getDistanceToPlane(Vector4f plane, Vector3f point) {
        return plane.x * point.x + plane.y * point.y + plane.z * point.z + plane.w;
    }

    public Vector4f getPlane(int index) {
        return planes[index];
    }
}
