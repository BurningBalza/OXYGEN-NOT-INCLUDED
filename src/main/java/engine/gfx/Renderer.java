package engine.gfx;

import engine.scene.Entity;
import engine.scene.Scene;
import org.joml.Matrix4f;

public class Renderer {
    public void render(Scene scene, float aspect) {
        render(scene, aspect, 0);
    }

    public void render(Scene scene, float aspect, float time) {
        Matrix4f view = scene.camera.view();
        Matrix4f proj = scene.camera.projection(aspect);

        Shader lastShader = null;

        for (Entity e : scene.entities()) {
            if (!e.visible) continue;

            Material m = e.material;
            Shader s = m.shader;

            if (s != lastShader) {
                s.bind()
                        .set("uView", view)
                        .set("uProj", proj)
                        .set("uLightDir", scene.lightDir)
                        .set("uViewPos", scene.camera.position)
                        .set("uTime", time);

                lastShader = s;
            }

            m.apply();
            s.set("uModel", e.transform.matrix());

            e.mesh.draw();
        }

        if (lastShader != null) {
            lastShader.unbind();
        }
    }
}
