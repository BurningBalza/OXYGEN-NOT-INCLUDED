package game;

import engine.core.Input;
import engine.scene.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Player {
    public final Vector3f position = new Vector3f();

    public float eyeHeight = 1.6f;
    public float walkSpeed = 4f;
    public float runSpeed = 7f;
    public float swimSpeed = 2f;
    public float jumpSpeed = 7f;
    public float gravity = 20f;
    public float swimDepth = 1.0f;

    public boolean onGround;
    public boolean swimming;

    private float velocityY;

    private final Matrix4f toRaft = new Matrix4f();
    private final Vector3f local = new Vector3f();
    private final Vector3f deckPoint = new Vector3f();

    public void update(float delta, Camera cam, Raft raft, float seaLevel, float time) {
        Vector3f forward = cam.forward();
        Vector3f right = cam.right();
        float flat = (float) Math.sqrt(forward.x * forward.x + forward.z * forward.z);
        float fx = forward.x / flat, fz = forward.z / flat;

        float moveX = 0f, moveZ = 0f;
        if (Input.isDown(GLFW_KEY_W)) { moveX += fx; moveZ += fz; }
        if (Input.isDown(GLFW_KEY_S)) { moveX -= fx; moveZ -= fz; }
        if (Input.isDown(GLFW_KEY_D)) { moveX += right.x; moveZ += right.z; }
        if (Input.isDown(GLFW_KEY_A)) { moveX -= right.x; moveZ -= right.z; }

        float length = (float) Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (length > 0f) {
            float speed = swimming ? swimSpeed : Input.isDown(GLFW_KEY_LEFT_SHIFT) ? runSpeed : walkSpeed;
            position.x += moveX / length * speed * delta;
            position.z += moveZ / length * speed * delta;
        }

        if (Input.wasPressed(GLFW_KEY_SPACE) && (onGround || swimming)) velocityY = jumpSpeed;

        velocityY -= gravity * delta;
        position.y += velocityY * delta;

        boolean wasOnGround = onGround;
        onGround = false;
        swimming = false;

        float deck = deckHeight(raft);
        float snap = wasOnGround ? 0.3f : 0f;
        if (!Float.isNaN(deck) && velocityY <= 0f && position.y <= deck + snap && position.y > deck - 0.6f) {
            position.y = deck;
            velocityY = 0f;
            onGround = true;
        }

        float water = seaLevel + Waves.height(position.x, position.z, time);
        if (!onGround && position.y < water - swimDepth) {
            position.y = water - swimDepth;
            if (velocityY < 0f) velocityY = 0f;
            swimming = true;
        }

        cam.position.set(position.x, position.y + eyeHeight, position.z);
    }

    private float deckHeight(Raft raft) {
        raft.deck.transform.matrix().invert(toRaft);
        toRaft.transformPosition(local.set(position));
        if (Math.abs(local.x) > raft.width / 2f || Math.abs(local.z) > raft.length / 2f) return Float.NaN;

        raft.deck.transform.matrix().transformPosition(deckPoint.set(local.x, raft.thickness / 2f, local.z));
        return deckPoint.y;
    }

}
