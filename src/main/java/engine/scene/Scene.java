package engine.scene;

import engine.util.Color;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public final class Scene {
    public final Camera camera = new Camera();
    public final Vector3f lightDir = new Vector3f(-0.4f, -1f, -0.3f);

    private final List<Entity> entities = new ArrayList<>();

    public final Vector3f fogColor = new Color(0x030204).toNormalisedVector();
    public float fogDensity = 0.12f;
    public float fogHeightFalloff = 0.15f;
    public float fogBaseHeight = 2.0f;
    public float fogNoiseScale = 0.05f;
    public float fogMaxDistance = 200f;

    public Entity add(Entity e) {
        entities.add(e); return e;
    }

    public void remove(Entity e) {
        entities.remove(e);
    }

    public void clear() {
        entities.clear();
    }

    public List<Entity> entities() { return Collections.unmodifiableList(entities); }
}
