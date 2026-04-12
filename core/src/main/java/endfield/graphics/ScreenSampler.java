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
import endfield.util.Reflects;
import mindustry.game.EventType.ResizeEvent;

import java.util.Objects;

public final class ScreenSampler {
	private static final FieldAccessor currentBoundBuffer;

	private static final FrameBuffer swapBuffer = new FrameBuffer();

	private ScreenSampler() {}

	static {
		try {
			currentBoundBuffer = Reflects.newFieldAccessor(GLFrameBuffer.class.getDeclaredField("currentBoundFramebuffer"));
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

/*import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.GL30;
import arc.graphics.Gl;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.GLFrameBuffer;
import arc.graphics.gl.Shader;
import arc.util.serialization.Jval;
import endfield.util.FieldAccessor;
import endfield.util.Reflects;
import mindustry.Vars;
import mindustry.game.EventType.Trigger;
import mindustry.graphics.Layer;
import mindustry.graphics.Pixelator;

import java.lang.reflect.Field;

public final class ScreenSampler {
	static final FieldAccessor lastBoundFramebufferField;
	static final FieldAccessor bufferField;

	static FrameBuffer pixelatorBuffer;
	static FrameBuffer worldBuffer = null;
	static FrameBuffer uiBuffer = null;

	static FrameBuffer currBuffer = null;
	static boolean activity = false;

	static {
		try {
			lastBoundFramebufferField = Reflects.newFieldAccessor(GLFrameBuffer.class.getDeclaredField("lastBoundFramebuffer"));
			bufferField = Reflects.newFieldAccessor(Pixelator.class.getDeclaredField("buffer"));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	static void ensurePixelatorBufferInitialized() {
		if (pixelatorBuffer == null) {

			Pixelator pixelator = Vars.renderer.pixelator;
			pixelatorBuffer = bufferField.get(pixelator);
		}
	}

	public static void resetMark() {
		Core.settings.remove("sampler.setup");
	}

	public static void setup() {
		if (activity) throw new RuntimeException("forbid setup sampler twice");

		Jval e = Jval.read(Core.settings.getString("sampler.setup", "{enabled: false}"));

		if (!e.getBool("enabled", false)) {
			e = Jval.newObject();
			e.put("enabled", true);
			e.put("className", ScreenSampler.class.getName());
			e.put("worldBuffer", "worldBuffer");
			e.put("uiBuffer", "uiBuffer");

			worldBuffer = new FrameBuffer();
			uiBuffer = new FrameBuffer();

			Core.settings.put("sampler.setup", e.toString());

			Events.run(Trigger.draw, () -> {
				Draw.draw(Layer.min - 0.001f, LegacyScreenSampler::beginWorld);
				Draw.draw(Layer.end + 0.001f, LegacyScreenSampler::endWorld);
			});

			Events.run(Trigger.uiDrawBegin, LegacyScreenSampler::beginUI);
			Events.run(Trigger.uiDrawEnd, LegacyScreenSampler::endUI);
		} else {
			String className = e.getString("className");
			String worldBufferName = e.getString("worldBuffer");
			String uiBufferName = e.getString("uiBuffer");

			try {
				Class<?> clazz = Class.forName(className);
				Field worldBufferField = clazz.getDeclaredField(worldBufferName);
				Field uiBufferField = clazz.getDeclaredField(uiBufferName);

				worldBufferField.setAccessible(true);
				uiBufferField.setAccessible(true);
				worldBuffer = (FrameBuffer) worldBufferField.get(null);
				uiBuffer = (FrameBuffer) uiBufferField.get(null);

				Events.run(Trigger.preDraw, () -> currBuffer = worldBuffer);
				Events.run(Trigger.postDraw, () -> currBuffer = null);
				Events.run(Trigger.uiDrawBegin, () -> currBuffer = uiBuffer);
				Events.run(Trigger.uiDrawEnd, () -> currBuffer = null);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to setup buffers from reflection", ex);
			}
		}

		activity = true;
	}

	private static void beginWorld() {
		if (Vars.renderer.pixelate) {
			ensurePixelatorBufferInitialized();
			currBuffer = pixelatorBuffer;
		} else {
			currBuffer = worldBuffer;

			if (worldBuffer.isBound()) return;

			worldBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
			worldBuffer.begin(Color.clear);
		}
	}

	private static void endWorld() {
		if (!Vars.renderer.pixelate) {
			worldBuffer.end();
			blitBuffer(worldBuffer, null);
		}
	}

	private static void beginUI() {
		currBuffer = uiBuffer;

		if (uiBuffer.isBound()) return;

		uiBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
		uiBuffer.begin(Color.clear);

		ensurePixelatorBufferInitialized();
		if (Vars.renderer.pixelate) blitBuffer(pixelatorBuffer, uiBuffer);
		else blitBuffer(worldBuffer, uiBuffer);
	}

	private static void endUI() {
		currBuffer = null;
		uiBuffer.end();
		blitBuffer(uiBuffer, null);
	}

	public static void blit(Shader shader, int unit) {
		if (currBuffer == null) {
			throw new IllegalStateException("currently no buffer bound");
		}

		currBuffer.getTexture().bind(unit);
		Draw.blit(shader);
	}

	public static void blit(Shader shader) {
		blit(shader, 0);
	}

	static void blitBuffer(FrameBuffer from, FrameBuffer to) {
		if (Core.gl30 == null) {
			from.blit(Shaders2.distBase);
		} else {
			GLFrameBuffer<?> target = to != null ? to : lastBoundFramebufferField.get(from);
			Gl.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, from.getFramebufferHandle());
			Gl.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target != null ? target.getFramebufferHandle() : 0);
			Core.gl30.glBlitFramebuffer(
					0, 0, from.getWidth(), from.getHeight(),
					0, 0,
					target != null ? target.getWidth() : Core.graphics.getWidth(),
					target != null ? target.getHeight() : Core.graphics.getHeight(),
					Gl.colorBufferBit, Gl.nearest
			);
		}
	}

	public static void getToBuffer(FrameBuffer target, boolean clear) {
		if (currBuffer == null) {
			throw new IllegalStateException("currently no buffer bound");
		}

		if (clear) target.begin(Color.clear);
		else target.begin();

		Gl.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, currBuffer.getFramebufferHandle());
		Gl.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target.getFramebufferHandle());
		Core.gl30.glBlitFramebuffer(
				0, 0, currBuffer.getWidth(), currBuffer.getHeight(),
				0, 0, target.getWidth(), target.getHeight(),
				Gl.colorBufferBit, Gl.nearest
		);

		target.end();
	}
}*/
