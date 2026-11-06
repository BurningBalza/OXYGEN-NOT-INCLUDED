package demo;

import engine.gfx.*;
import engine.core.*;
import engine.scene.*;
import game.Waves;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Demo extends GameApp {
    private final Scene scene = new Scene();
    private final Renderer renderer = new Renderer();
    private final List<Entity> spinners = new ArrayList<>();

    private Shader shader;
    private Shader oceanShader;

    private Mesh cubeMesh;
    private Mesh oceanMesh;

    private Texture checker;

    private Entity oceanEntity;
    private Entity raft;

    private float frameTime;

    @Override
    protected void init() {
        shader = Shader.load("shaders/engine/basic.vert", "shaders/engine/basic.frag");
        oceanShader = Shader.load("shaders/game/ocean.vert", "shaders/game/ocean.frag");

        cubeMesh = Mesh.cube();
        oceanMesh = Mesh.grid(256, 200.0f, 1.0f);

        Material oceanMaterial = new Material(oceanShader)
                .tint(1.0f, 1.0f, 1.0f);

        oceanEntity = new Entity(oceanMesh, oceanMaterial);
        oceanEntity.transform.position.set(0.0f, -1.0f, 0.0f);

        scene.add(oceanEntity);

        checker = Texture.checker(
                8,
                8,
                new int[]{230, 120, 40},
                new int[]{250, 220, 180}
        );

        for (int i = 0; i < 5; i++) {
            Material mat = new Material(shader, checker)
                    .tint(0.6f + 0.1f * i, 1f, 1.4f - 0.2f * i);

            spinners.add(scene.add(
                    new Entity(cubeMesh, mat)
                            .named("cube" + i)
                            .at(i * 2.5f - 5f, 1f, -4f)
            ));
        }

        raft = scene.add(
                new Entity(cubeMesh, new Material(shader).tint(0.45f, 0.3f, 0.18f))
                        .named("raft")
                        .at(0f, 0f, -12f)
        );
        raft.transform.setScale(2f, 0.3f, 4f);

        scene.camera.position.set(0, 2, 6);
        Input.captureMouse(true);
    }

    @Override
    protected void update(float dt) {
        frameTime = (float) glfwGetTime();

        if (Input.wasPressed(GLFW_KEY_ESCAPE)) {
            window.requestClose();
        }

        Camera cam = scene.camera;
        cam.look(Input.mouseDX(), Input.mouseDY());

        float speed = (Input.isDown(GLFW_KEY_LEFT_SHIFT) ? 12f : 5f) * dt;

        if (Input.isDown(GLFW_KEY_W)) cam.moveForward(speed);
        if (Input.isDown(GLFW_KEY_S)) cam.moveForward(-speed);
        if (Input.isDown(GLFW_KEY_D)) cam.moveRight(speed);
        if (Input.isDown(GLFW_KEY_A)) cam.moveRight(-speed);
        if (Input.isDown(GLFW_KEY_SPACE)) cam.moveUp(speed);
        if (Input.isDown(GLFW_KEY_LEFT_CONTROL)) cam.moveUp(-speed);

        for (int i = 0; i < spinners.size(); i++) {
            spinners.get(i).transform.rotate(
                    dt * (0.5f + i * 0.4f),
                    0.3f,
                    1f,
                    0.2f
            );
        }

        float x = raft.transform.position.x;
        float z = raft.transform.position.z;
        float front = Waves.height(x, z - 2f, frameTime);
        float back = Waves.height(x, z + 2f, frameTime);
        float left = Waves.height(x - 1f, z, frameTime);
        float right = Waves.height(x + 1f, z, frameTime);

        float water = (front + back + left + right) / 4f;
        raft.transform.position.y = oceanEntity.transform.position.y + water + 0.1f;
        raft.transform.setEuler(
                (float) Math.atan2(front - back, 4f),
                0f,
                (float) Math.atan2(right - left, 2f)
        );
    }

    @Override
    protected void render() {
        renderer.render(scene, window.width(), window.height(), frameTime);
    }

    @Override
    protected void cleanup() {
        if (shader != null) shader.close();
        if (oceanShader != null) oceanShader.close();

        if (cubeMesh != null) cubeMesh.close();
        if (oceanMesh != null) oceanMesh.close();

        if (checker != null) checker.close();

        renderer.close();
    }

    public static void main(String[] args) {
        new Demo().run("Abyssaline", 1280, 720);
    }
}