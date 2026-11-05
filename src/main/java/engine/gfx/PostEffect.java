package engine.gfx;

public final class PostEffect implements AutoCloseable {
    @FunctionalInterface
    public interface Setup {
        void apply(Shader shader, FrameContext frame);
    }
 
    public final Shader shader;
    public boolean enabled = true;
    private final Setup setup;
 
    public PostEffect(Shader shader, Setup setup) {
        this.shader = shader;
        this.setup = setup;
    }
 
    void configure(FrameContext frame) { setup.apply(shader, frame); }
 
    @Override
    public void close() { shader.close(); }
}
