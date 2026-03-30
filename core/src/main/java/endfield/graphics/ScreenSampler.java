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
import arc.util.serialization.Jval;
import endfield.util.FieldAccessor;
import endfield.util.handler.FieldHandler;
import mindustry.Vars;
import mindustry.game.EventType.Trigger;
import mindustry.graphics.Layer;
import mindustry.graphics.Pixelator;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

import static endfield.Vars2.platformImpl;

public final class ScreenSampler {
	private static FieldAccessor lastBoundFramebufferAccessor;
	private static FieldAccessor bufferAccessor;

	private static FrameBuffer worldBuffer, uiBuffer, currBuffer;

	private static FrameBuffer pixelatorBuffer;

	private static boolean activity = false;

	private ScreenSampler() {}

	public static void init() {
		try {
			lastBoundFramebufferAccessor = platformImpl.fieldAccessor(GLFrameBuffer.class.getDeclaredField("lastBoundFramebuffer"));
			bufferAccessor = platformImpl.fieldAccessor(Pixelator.class.getDeclaredField("buffer"));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private static void ensurePixelatorBufferInitialized() {
		if (pixelatorBuffer == null) {
			Pixelator pixelator = Vars.renderer.pixelator;
			pixelatorBuffer = bufferAccessor.get(pixelator);
		}
	}

	public static void resetMark() {
		Core.settings.remove("sampler.setup");
	}

	/**
	 * Load Events for ScreenSampler.
	 * If you try to load it a second time, nothing will happen.
	 */
	public static void setup() {
		if (activity) return;

		Jval jval = Jval.read(Core.settings.getString("sampler.setup", "{enabled: false}"));

		if (!jval.getBool("enabled", false)) {
			jval = Jval.newObject();
			jval.put("enabled", true);
			jval.put("className", ScreenSampler.class.getName());
			jval.put("worldBuffer", "worldBuffer");
			jval.put("uiBuffer", "uiBuffer");

			worldBuffer = new FrameBuffer();
			uiBuffer = new FrameBuffer();

			Core.settings.put("sampler.setup", jval.toString());

			Events.run(Trigger.draw, () -> {
				Draw.draw(Layer.min - 0.001f, ScreenSampler::beginWorld);
				Draw.draw(Layer.end + 0.001f, ScreenSampler::endWorld);
			});

			Events.run(Trigger.uiDrawBegin, ScreenSampler::beginUI);
			Events.run(Trigger.uiDrawEnd, ScreenSampler::endUI);
		} else {
			try {
				String className = jval.getString("className");
				String worldBufferName = jval.getString("worldBuffer");
				String uiBufferName = jval.getString("uiBuffer");
				Class<?> type = Class.forName(className);
				Field worldBufferField = type.getDeclaredField(worldBufferName);
				Field uiBufferField = type.getDeclaredField(uiBufferName);

				worldBufferField.setAccessible(true);
				uiBufferField.setAccessible(true);
				worldBuffer = FieldHandler.get(null, worldBufferField);
				uiBuffer = FieldHandler.get(null, uiBufferField);

				Events.run(Trigger.preDraw, () -> currBuffer = worldBuffer);
				Events.run(Trigger.postDraw, () -> currBuffer = null);
				Events.run(Trigger.uiDrawBegin, () -> currBuffer = uiBuffer);
				Events.run(Trigger.uiDrawEnd, () -> currBuffer = null);
			} catch (ClassNotFoundException | NoSuchFieldException e) {
				throw new RuntimeException("Failed to setup buffers from reflection", e);
			}
		}

		activity = true;
	}

	/**
	 * @return Has whether set up.
	 */
	public static boolean isActivity() {
		return activity;
	}

	private static void beginWorld() {
		if (Vars.renderer.pixelate) {
			ensurePixelatorBufferInitialized();

			currBuffer = pixelatorBuffer;
		} else {
			currBuffer = worldBuffer;

			if (currBuffer.isBound()) return;

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

	/**
	 * Draw the current screen texture onto the screen using the passed shader.
	 *
	 * @param unit Texture units bound to screen sampling textures
	 */
	public static void blit(Shader shader, int unit) {
		if (currBuffer == null) {
			throw new IllegalStateException("currently no buffer bound");
		}

		currBuffer.getTexture().bind(unit);
		Draw.blit(shader);
	}

	/** Overload method, use default texture unit {@code 0}. */
	public static void blit(Shader shader) {
		blit(shader, 0);
	}

	private static void blitBuffer(FrameBuffer from, @Nullable FrameBuffer to) {
		if (Core.gl30 == null) {
			from.blit(Shaders2.distBase);
		} else {
			GLFrameBuffer<?> target = to != null ? to : lastBoundFramebufferAccessor.get(from);
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

	/**
	 * Transfer the current screen texture to a{@linkplain FrameBuffer frame buffer}, This will become a copy that can be used to temporarily store screen content.
	 *
	 * @param target Target buffer for transferring screen textures.
	 * @param clear  Is the frame buffer cleared before transferring.
	 */
	public static void getToBuffer(FrameBuffer target, boolean clear) {
		if (currBuffer == null) throw new IllegalStateException("currently no buffer bound");

		if (clear) target.begin(Color.clear);
		else target.begin();

		blitBuffer(currBuffer, target);

		target.end();
	}
}