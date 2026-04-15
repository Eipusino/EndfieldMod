package endfield.type.weather;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.Texture.TextureWrap;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Scaling;
import arc.util.Time;
import endfield.entities.bullet.FallingRockBulletType;
import endfield.util.CollectionList;
import endfield.util.ObjectFloatMap2;
import endfield.world.meta.StatValues2;
import endfield.world.meta.Stats2;
import mindustry.Vars;
import mindustry.content.Liquids;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.WeatherState;
import mindustry.type.Liquid;
import mindustry.ui.Styles;
import org.jetbrains.annotations.Nullable;

public class HailStormWeather extends SpawnWeather {
	private static BulletType picked;
	private static float threshold;

	// spawn
	public ObjectFloatMap2<BulletType> bullets = new ObjectFloatMap2<>(BulletType.class);
	public float spawnChance = 0;
	public boolean windDrag = true;
	public float windDragScaleMin = 1, windDragScaleMax = 1;

	// general
	public Color color = new Color(0x596ab8ff);
	public float yspeed = 5f, xspeed = 1.5f, density = 900f, sizeMin = 8f, sizeMax = 40f;
	public boolean useWindVector = false;

	// rain
	public boolean rain = false;
	public Liquid liquid = Liquids.water;
	public float splashTimeScale = 22f, stroke = 0.75f;
	public TextureRegion[] splashes = new TextureRegion[12];

	// particle
	public boolean drawParticles = false, randomParticleRotation = false;
	public String particleRegion = "circle-shadow";
	public float minAlpha = 1f, maxAlpha = 1f;
	public float sinSclMin = 30f, sinSclMax = 80f, sinMagMin = 1f, sinMagMax = 7f;
	public TextureRegion particle;

	// noise
	public Color noiseColor = color;
	public boolean drawNoise = false;
	public int noiseLayers = 1;
	public float noiseAlpha = 1f, noiseScale = 2000f, noiseSpeed = 1f;
	public float noiseLayerSpeedM = 1.1f, noiseLayerAlphaM = 0.8f, noiseLayerSclM = 0.99f, noiseLayerColorM = 1f;
	public String noisePath = "noiseAlpha";
	public @Nullable Texture noise;
	private float minIntensity = Float.POSITIVE_INFINITY;

	public HailStormWeather(String name) {
		super(name);
	}

	public void addBullet(BulletType bullet, float intensity) {
		bullets.put(bullet, intensity);
		minIntensity = Math.min(minIntensity, intensity);
	}

	public void addBullets(Object... items) {
		for (int i = 0; i < items.length - 1; i += 2) {
			addBullet((BulletType) items[i], 1f - ((Number) items[i + 1]).floatValue());
		}
	}

	@Override
	public void drawOver(WeatherState state) {
		super.drawOver(state);
		if (rain) drawRain(sizeMin, sizeMax, xspeed, yspeed, density, state.intensity, stroke, color);

		if (drawNoise) {
			if (noise == null) {
				noise = Core.assets.get("sprites/" + noisePath + ".png", Texture.class);
				noise.setWrap(TextureWrap.repeat);
				noise.setFilter(TextureFilter.linear);
			}

			drawNoiseLayers(noise, noiseColor, noiseScale, state.opacity * noiseAlpha, noiseSpeed, state.intensity, (useWindVector ? state.windVector.x : 1f), (useWindVector ? state.windVector.y : 1f), noiseLayers, noiseLayerSpeedM, noiseLayerAlphaM, noiseLayerSclM, noiseLayerColorM);
		}

		if (drawParticles)
			drawParticles(particle, color, sizeMin, sizeMax, density, state.intensity, state.opacity, xspeed * (useWindVector ? state.windVector.x : 1f), yspeed * (useWindVector ? state.windVector.y : 1f), minAlpha, maxAlpha, sinSclMin, sinSclMax, sinMagMin, sinMagMax, randomParticleRotation);
	}

	@Override
	public void drawUnder(WeatherState state) {
		if (rain)
			drawSplashes(splashes, sizeMax, density, state.intensity, state.opacity, splashTimeScale, stroke, color, liquid);
	}

	@Override
	public void load() {
		super.load();

		for (int i = 0; i < splashes.length; i++) {
			splashes[i] = Core.atlas.find("splash-" + i);
		}

		particle = Core.atlas.find(particleRegion);

		if (drawNoise && Core.assets != null) {
			Core.assets.load("sprites/" + noisePath + ".png", Texture.class);
		}
	}

	@Override
	public void setStats() {
		String descriptionClone = description;
		description = null;
		stats.add(Stats2.space, table -> {
			table.clear();
			table.add("[lightgray]" + descriptionClone).wrap().fillX().width(500).padTop(10).padBottom(10).left();
		});

		if (!bullets.isEmpty()) stats.add(Stats2.debris, stat -> {
			stat.row();
			CollectionList<BulletType> keys = new CollectionList<>(BulletType.class);
			bullets.each(e -> keys.add(e.key));
			keys.sort(b -> bullets.get(b, 0f));

			stat.table(table -> keys.each(bullet -> {
				float value = bullets.get(bullet, 0f);
				float chance = 1f - Mathf.pow(value, spawns * spawnChance * Time.toSeconds);
				table.table(Styles.grayPanel, info -> {
					if (bullet instanceof FallingRockBulletType rock)
						info.image(rock.variantRegions[0]).size(64).padRight(10f).left().scaling(Scaling.fit);
					info.table(damages -> {
						damages.defaults().growX().left();
						damages.add(Core.bundle.format("bullet.damage", bullet.damage)).row();
						damages.add(Core.bundle.format("bullet.splashdamage", bullet.splashDamage, Mathf.round(bullet.splashDamageRadius / 8f))).row();
					}).grow().padRight(20f).left();
					info.add("[lightgray]" + StatValues2.formatValue(chance * 100f, 3, false) + Stats2.percentPerSecond.localized()).right();
				}).growX().pad(5f).margin(10f).row();
			})).growX();
		});
	}

	@Override
	public boolean shouldSpawn(WeatherState state) {
		return Mathf.chance(spawnChance * state.intensity);
	}

	/**
	 * Each time a bullet spawns, it'll have a random variable that ranges from 0-1.
	 * The bullet with the highest intensity that is still under the intensity will be spawned.
	 */
	@Override
	public void spawn(WeatherState state, float x, float y) {
		if (Vars.net.client()) return;
		float intensity = Mathf.lerp(minIntensity, Math.max(minIntensity, Mathf.random(1f)), state.intensity);

		threshold = Float.NEGATIVE_INFINITY;
		picked = null;
		bullets.each(b -> {
			if (b.value <= intensity && b.value >= threshold) {
				picked = b.key;
				threshold = b.value;
			}
		});
		if (picked != null) {
			float speedScl = Mathf.random(windDragScaleMin, windDragScaleMax);
			picked.createNet(
					Team.derelict,
					x - Angles.trnsx(windDrag ? state.windVector.angle() : 0, speedScl * picked.lifetime * picked.speed),
					y - Angles.trnsy(windDrag ? state.windVector.angle() : 0, speedScl * picked.lifetime * picked.speed),
					windDrag ? state.windVector.angle() : 0,
					picked.damage,
					speedScl,
					1
			);
		}
	}
}
