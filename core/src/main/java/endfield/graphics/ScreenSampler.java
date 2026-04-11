package endfield.graphics;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.GL30;
import arc.graphics.Gl;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.GLFrameBuffer;
import arc.graphics.gl.Shader;
import endfield.util.FieldAccessor;
import mindustry.game.EventType.ResizeEvent;

import java.util.Objects;

import static endfield.Vars2.platformImpl;

public final class ScreenSampler {
	private static final FieldAccessor currentBoundBuffer;

	private static final FrameBuffer swapBuffer = new FrameBuffer();

	private ScreenSampler() {}

	static {
		try {
			currentBoundBuffer = platformImpl.fieldAccessor(GLFrameBuffer.class.getDeclaredField("currentBoundFramebuffer"));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setup() {
		swapBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());

		Events.on(ResizeEvent.class, event -> swapBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight()));
	}

	public static void toBuffer(FrameBuffer target) {
		GLFrameBuffer<?> buffer = currentBoundBuffer.getObject(null);

		if (buffer != null) {
			if (buffer.getWidth() == target.getWidth() && buffer.getHeight() == target.getHeight()) {
				buffer.begin();
				target.getTexture().bind();
				Gl.copyTexSubImage2D(
						Gl.texture2d,
						0,
						0, 0,
						0, 0,
						target.getWidth(), target.getHeight());
				Gl.bindTexture(Gl.texture2d, 0);
				buffer.end();
			} else {
				blitBuffer(buffer, target);
			}
		} else {
			if (swapBuffer.getWidth() == target.getWidth() && swapBuffer.getHeight() == target.getHeight()) {
				Draw.flush();
				target.getTexture().bind();
				Gl.copyTexSubImage2D(
						Gl.texture2d,
						0,
						0, 0,
						0, 0,
						target.getWidth(), target.getHeight()
				);
				Gl.bindTexture(Gl.texture2d, 0);
			} else {
				Draw.flush();
				swapBuffer.getTexture().bind();
				Gl.copyTexSubImage2D(
						Gl.texture2d,
						0,
						0, 0,
						0, 0,
						swapBuffer.getWidth(), swapBuffer.getHeight()
				);
				Gl.bindTexture(Gl.texture2d, 0);

				blitBuffer(swapBuffer, target);
			}
		}
	}

	public static void blitShader(Shader shader, int unit) {
		GLFrameBuffer<?> buffer = currentBoundBuffer.getObject(null);

		Objects.requireNonNullElse(buffer, swapBuffer).getTexture().bind(unit);
		Draw.blit(shader);
	}

	private static void blitBuffer(GLFrameBuffer<?> source, GLFrameBuffer<?> target) {
		if (Core.gl30 != null) {
			Core.gl30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, source.getFramebufferHandle());
			Core.gl30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target.getFramebufferHandle());
			Core.gl30.glBlitFramebuffer(
					0, 0, source.getWidth(), source.getHeight(),
					0, 0, target.getWidth(), target.getHeight(),
					Gl.colorBufferBit, Gl.nearest
			);
			Core.gl30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
			Core.gl30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		} else {
			target.begin(Color.clear);
			source.getTexture().bind(0);
			Draw.blit(Shaders2.distBase);
			Gl.bindTexture(Gl.texture2d, 0);
			target.end();
		}
	}

	public static void dispose() {
		swapBuffer.dispose();
	}
}