package endfield.ui;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.ui.layout.Scl;
import arc.util.Align;
import arc.util.Disposable;
import arc.util.Time;
import arc.util.Tmp;
import endfield.core.IListener;
import endfield.graphics.Regions2;
import mindustry.Vars;
import mindustry.core.Version;
import mindustry.graphics.Pal;
import mindustry.mod.Mods.LoadedMod;

import java.io.DataInputStream;
import java.util.zip.InflaterInputStream;

import static endfield.Vars2.MOD_NAME;

/** Renders a custom splash screen for the mod during game startup. */
public class SplashDrawer implements IListener, Disposable {
	Texture iconTex;
	TextureRegion icon;
	Texture mindyLogoTex;
	TextureRegion mindyLogo;
	String version;
	long startTime;
	Font font;
	boolean fadingOut = false;
	long fadeOutStartTime;

	/**
	 * Initializes the splash screen, loads its assets and starts the drawing loop.
	 *
	 * @param mod The loaded mod instance, used for retrieving metadata like the version.
	 */
	public SplashDrawer(LoadedMod mod) {
		this.version = mod.meta.version;

		byte[] iconBytes = Regions2.spritesDir.child("icons").child("splash-icon.png").readBytes();
		Pixmap iconPixmap = new Pixmap(iconBytes);
		iconTex = new Texture(iconPixmap);
		iconPixmap.dispose();
		icon = new TextureRegion(iconTex);

		Fi logoFile = Core.files.internal("sprites/logo.png");
		Pixmap logoPixmap = new Pixmap(logoFile);
		mindyLogoTex = new Texture(logoPixmap);
		logoPixmap.dispose();
		mindyLogo = new TextureRegion(mindyLogoTex);

		startTime = Time.millis();
	}

	/**
	 * Checks if the splash screen is enabled and adds it to the application listeners if so.
	 *
	 * @param mod The loaded mod instance.
	 */
	public static void add(LoadedMod mod) {
		if (isEnabled()) {
			Core.app.addListener(new SplashDrawer(mod));
		}
	}

	/**
	 * Checks if the splash screen is enabled by manually reading the settings file.
	 * This is an optimized method that only searches for the specific key, making it fast enough for startup.
	 *
	 * @return true if the splash screen should be displayed, false otherwise.
	 */
	static boolean isEnabled() {
		try {
			Fi file = Core.settings.getSettingsFile();
			if (!file.exists()) return true;

			byte[] header = new byte[2];
			file.readBytes(header, 0, 2);
			boolean compressed = header[0] == (byte) 0x78;

			try (DataInputStream stream = new DataInputStream(compressed ? new InflaterInputStream(file.read()) : file.read())) {
				int amount = stream.readInt();
				for (int i = 0; i < amount; i++) {
					String key = stream.readUTF();
					byte type = stream.readByte();

					if (key.equals(MOD_NAME + "-loading-screen")) {
						if (type == 0) return stream.readBoolean(); // typeBool
						return true; // Wrong type -> default to true
					} else {
						// Skip value bytes to quickly get to the next key
						switch (type) {
							case 0:
								stream.skipBytes(1);
								break; // boolean
							case 1:
							case 3:
								stream.skipBytes(4);
								break; // int, float
							case 2:
								stream.skipBytes(8);
								break; // long
							case 4:
								stream.skipBytes(stream.readUnsignedShort());
								break; // String
							case 5:
								stream.skipBytes(stream.readInt());
								break; // byte[]
						}
					}
				}
			}
		} catch (Exception e) {
			return true;
		}
		return true;
	}

	/**
	 * The main drawing method, executed each frame via `Core.app.post()`.
	 * Handles animations, state changes, and renders all visual elements.
	 */
	void draw() {
		if (fadingOut && Time.timeSinceMillis(fadeOutStartTime) >= 0.9f * 1000f) {
			dispose();
			return;
		}

		if (Vars.clientLoaded && !fadingOut) {
			fadingOut = true;
			fadeOutStartTime = Time.millis();
		}

		float backgroundAlpha, elementsAlpha;
		if (fadingOut) {
			float elapsed = Time.timeSinceMillis(fadeOutStartTime) / 1000f;
			float uiFadeDuration = 0.3f;
			float holdDuration = 0.2f;
			float backgroundFadeDelay = uiFadeDuration + holdDuration;
			float backgroundFadeDuration = 0.4f;

			elementsAlpha = 1f - Interp.fade.apply(Math.min(elapsed / uiFadeDuration, 1f));
			backgroundAlpha = 1f;
			if (elapsed > backgroundFadeDelay) {
				float fadeProgress = (elapsed - backgroundFadeDelay) / backgroundFadeDuration;
				backgroundAlpha = 1f - Interp.fade.apply(Math.min(fadeProgress, 1f));
			}
		} else {
			float time = Time.timeSinceMillis(startTime) / 1000f;
			backgroundAlpha = Interp.fade.apply(Math.min(time / 0.15f, 1f));
			elementsAlpha = Interp.fade.apply(Math.min(time / 0.4f, 1f));
		}

		if (font == null && Core.assets.isLoaded("tech")) font = Core.assets.get("tech");

		Draw.color(Pal.darkestGray, backgroundAlpha);
		Fill.rect(Core.graphics.getWidth() / 2f, Core.graphics.getHeight() / 2f, Core.graphics.getWidth(), Core.graphics.getHeight());

		Draw.proj().setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

		float progress = Core.assets.getProgress();
		float w = Core.graphics.getWidth(), h = Core.graphics.getHeight();

		Draw.color(Color.black, backgroundAlpha);
		Fill.poly(w / 2f, h / 2f, 6, (Mathf.dst(w, h) / 2f) * progress * 1.3f);
		Draw.reset();

		if (elementsAlpha > 0.001f) {
			float scaledIconSize = 256f * Scl.scl();
			Draw.color(Color.white, elementsAlpha);
			Draw.rect(icon, w / 2f, h / 2f, scaledIconSize, scaledIconSize);

			if (font != null) {
				font.getData().setScale(1f);
				font.setColor(Tmp.c1.set(Pal.darkerGray).a(elementsAlpha));
				font.draw(version, w / 2f, h / 2f - scaledIconSize / 2f - Scl.scl(20f), 0, Align.center, false);

				if (mindyLogo != null && Version.build != -1) {
					String mindyVersion = "v" + Version.buildString();
					GlyphLayout layout = GlyphLayout.obtain();
					layout.setText(font, mindyVersion);

					float logoWidth = 236f * Scl.scl();
					float logoHeight = logoWidth * mindyLogo.height / mindyLogo.width;
					float padding = Scl.scl(10f);
					float totalWidth = logoWidth + padding + layout.width;
					float startX = w / 2f - totalWidth / 2f;

					float barHeight = 32f * Scl.scl();
					float barTopY = (40f * Scl.scl()) + barHeight;
					float blockCenterY = barTopY + Scl.scl(30f);

					Draw.color(Pal.lightishGray.cpy().lerp(Color.white, 0.5f), elementsAlpha);
					Draw.rect(mindyLogo, startX + logoWidth / 2f, blockCenterY, logoWidth, logoHeight);

					font.setColor(Pal.lightishGray.cpy().a(elementsAlpha));
					font.draw(mindyVersion, startX + logoWidth + padding, blockCenterY + layout.height / 2f, 0, Align.left, false);

					layout.free();
				}
			}

			float barHeight = 32f * Scl.scl();
			float barCenterX = w / 2f;
			float barCenterY = (40f * Scl.scl()) + barHeight / 2f;
			float barWidth = w * 0.6f;

			Draw.color(Color.white, elementsAlpha);
			float stroke = 3f * Scl.scl();
			Lines.stroke(stroke);
			Lines.rect(barCenterX - barWidth / 2f, barCenterY - barHeight / 2f, barWidth, barHeight);

			float fillPadding = 2f * Scl.scl();
			float maxFillWidth = barWidth - (stroke * 2f) - (fillPadding * 2f);
			float fillHeight = barHeight - (stroke * 2f) - (fillPadding * 2f);
			float fillWidth = maxFillWidth * progress;

			if (fillWidth > 0 && fillHeight > 0) {
				float fillStartX = barCenterX - barWidth / 2f + stroke + fillPadding;
				Fill.rect(fillStartX + fillWidth / 2f, barCenterY, fillWidth, fillHeight);

				if (font != null) {
					font.setColor(Tmp.c1.set(Pal.darkestGray).a(elementsAlpha));
					font.draw((int) (progress * 100) + "%", barCenterX, barCenterY + font.getCapHeight() / 2f, 0, Align.center, false);
				}
			}
		}

		Draw.flush();
	}

	@Override
	public void dispose() {
		iconTex.dispose();
		mindyLogoTex.dispose();
	}

	@Override
	public boolean isDisposed() {
		return iconTex.isDisposed() && mindyLogoTex.isDisposed();
	}

	@Override
	public void update() {
		draw();
	}
}
