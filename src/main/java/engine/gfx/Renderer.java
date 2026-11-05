package engine.gfx;

import engine.scene.Entity;
import engine.scene.Scene;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

public class Renderer {
    private Framebuffer fb;
    private Shader fogShader;
    private int fullscreenVao;

    private void ensureInit() {
        if (fogShader == null) {
            fogShader = Shader.load(
                    "shaders/fullscreen.vert",
                    "shaders/volumetric_fog.frag");
        }
        if (fullscreenVao == 0) {
            fullscreenVao = glGenVertexArrays();
        }
    }

    private void ensureFramebuffer(int w, int h) {
        if (fb == null || fb.width != w || fb.height != h) {
            fb = new Framebuffer(w, h);
        }
    }

    public void render(Scene scene, float aspect) {
        render(scene, aspect, 0);
    }

    public void render(Scene scene, float aspect, float time) {
        ensureInit();

        int vpW, vpH;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer vp = stack.mallocInt(4);
            glGetIntegerv(GL_VIEWPORT, vp);
            vpW = vp.get(2);
            vpH = vp.get(3);
        }
        ensureFramebuffer(vpW, vpH);

        Matrix4f view  = scene.camera.view();
        Matrix4f proj  = scene.camera.projection(aspect);
        Matrix4f invVP = new Matrix4f(proj).mul(view).invert();

        fb.bind();

        glClearColor(scene.fogColor.x, scene.fogColor.y, scene.fogColor.z, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);

        Shader lastShader = null;
        for (Entity e : scene.entities()) {
            if (!e.visible) continue;
            Material m = e.material;
            Shader s   = m.shader;

            if (s != lastShader) {
                s.bind()
                        .set("uView",     view)
                        .set("uProj",     proj)
                        .set("uLightDir", scene.lightDir)
                        .set("uViewPos",  scene.camera.position)
                        .set("uTime",     time);
                lastShader = s;
            }

            m.apply();
            s.set("uModel", e.transform.matrix());
            e.mesh.draw();
        }
        if (lastShader != null) lastShader.unbind();

        fb.unbind();

        glDisable(GL_DEPTH_TEST);
        glViewport(0, 0, vpW, vpH);

        fogShader.bind()
                .set("uColorTex",         0)
                .set("uDepthTex",         1)
                .set("uInvViewProj",      invVP)
                .set("uCameraPos",        scene.camera.position)
                .set("uLightDir",         scene.lightDir)
                .set("uFogColor",         scene.fogColor)
                .set("uFogDensity",       scene.fogDensity)
                .set("uFogHeightFalloff", scene.fogHeightFalloff)
                .set("uFogBaseHeight",    scene.fogBaseHeight)
                .set("uNoiseScale",       scene.fogNoiseScale)
                .set("uMaxDistance",      scene.fogMaxDistance)
                .set("uTime",             time);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, fb.colorTex);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, fb.depthTex);

        glBindVertexArray(fullscreenVao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);

        fogShader.unbind();
        glEnable(GL_DEPTH_TEST);
    }
}