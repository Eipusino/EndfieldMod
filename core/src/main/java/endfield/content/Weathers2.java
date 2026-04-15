package endfield.content;

import arc.graphics.Color;
import arc.util.Time;
import endfield.audio.Sounds2;
import endfield.entities.bullet.FallingRockBulletType;
import endfield.type.weather.EffectWeather;
import endfield.type.weather.HailStormWeather;
import mindustry.content.Fx;
import mindustry.content.Liquids;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.type.Weather;
import mindustry.type.weather.ParticleWeather;
import mindustry.world.meta.Attribute;

import static endfield.Vars2.MOD_NAME;

/**
 * Defines the {@linkplain Weather weather} this mod offers.
 *
 * @author Eipusino
 */
public final class Weathers2 {
	public static EffectWeather wind;
	public static ParticleWeather blizzard;
	public static HailStormWeather hailStone, stoneStorm;

	/** Don't let anyone instantiate this class. */
	private Weathers2() {}

	/** Instantiates all contents. Called in the main thread in {@code EndFieldMod.loadContent()}. */
	public static void load() {
		wind = new EffectWeather("wind") {{
			weatherFx = Fx2.windTail;
			particleRegion = "particle";
			sizeMax = 5f;
			sizeMin = 1f;
			density = 1600;
			baseSpeed = 5.4f;
			minAlpha = 0.05f;
			maxAlpha = 0.18f;
			force = 0.1f;
			sound = Sounds.wind2;
			soundVol = 0.8f;
			spawns = 2;
			duration = 8f * Time.toMinutes;
		}};
		blizzard = new ParticleWeather("blizzard") {{
			particleRegion = "particle";
			sizeMax = 14f;
			sizeMin = 3f;
			density = 600f;
			baseSpeed = 15f;
			yspeed = -2.5f;
			xspeed = 8f;
			minAlpha = 0.75f;
			maxAlpha = 0.9f;
			attrs.set(Attribute.light, -0.35f);
			sound = Sounds.windHowl;
			soundVol = 0.25f;
			soundVolOscMag = 1.5f;
			soundVolOscScl = 1100f;
			soundVolMin = 0.15f;
		}};
		hailStone = new HailStormWeather("hail-storm") {{
			attrs.set(Attribute.light, -2f);
			rain = true;
			duration = 15f * Time.toMinutes;
			soundVol = 0.05f;
			spawns = 6;
			spawnChance = 0.5f;
			sound = Sounds2.hailRain;
			addBullets(new FallingRockBulletType(MOD_NAME + "-hailstone-small") {{
				speed = 0.5f;
				lifetime = 20f;
				fallHeight = 10f;
				fallDistance = 120f;
				variants = 5;
				hitEffect = Fx.none;
				despawnEffect = Fx2.fellStone;
			}}, 1f, new FallingRockBulletType(MOD_NAME + "-hailstone-medium") {{
				speed = 0.5f;
				lifetime = 30f;
				fallHeight = 11f;
				fallDistance = 120f;
				variants = 2;
				hitEffect = new MultiEffect(Fx2.dynamicHailWave.layer(Layer.power).wrap(Liquids.water.color, 5f), Fx2.hailStoneSplashSmall);
				despawnEffect = Fx2.fellStone;
				damage = splashDamage = 10f;
				splashDamageRadius = 25f;
			}}, 1 / 12f, new FallingRockBulletType(MOD_NAME + "-hailstone-big") {{
				speed = 0.5f;
				lifetime = 20f;
				fallHeight = 15f;
				fallDistance = 150f;
				variants = 2;
				hitSize = 12f;
				hitEffect = Fx2.hailStoneImpact;
				despawnEffect = Fx2.staticStone;
				hitSound = Sounds2.bigHailstoneHit;
				damage = splashDamage = 95f;
				splashDamageRadius = 40f;
			}}, 1f / 1600f, new FallingRockBulletType(MOD_NAME + "-hailstone-giant") {{
				speed = 1f;
				lifetime = 200f;
				fallHeight = 20f;
				fallDistance = 400f;
				hitSize = 80f;
				spawnSound = Sounds2.giantHailstoneFall;
				hitEffect = new MultiEffect(Fx.massiveExplosion, Fx2.dynamicHailWave.wrap(Liquids.water.color, 60f));
				despawnEffect = Fx2.staticStone;
				hitSound = Sounds2.giantHailstoneHit;
				damage = splashDamage = 250f;
				splashDamageRadius = 80f;
			}}, 1f / 10000000f);
		}};
		stoneStorm = new HailStormWeather("stone-storm") {{
			attrs.set(Attribute.light, -2f);
			duration = 15f * Time.toMinutes;
			soundVol = 0.05f;
			sound = Sounds2.sandstorm;
			spawns = 6;
			spawnChance = 0.5f;
			windDragScaleMin = 10f;
			windDragScaleMax = 20f;
			color = Color.white;
			useWindVector = true;
			xspeed = yspeed = 20f;
			density = 400f;
			drawNoise = true;
			noiseLayers = 3;
			noiseColor = Color.valueOf("493D37");
			noiseSpeed = 20f;
			drawParticles = true;
			randomParticleRotation = true;
			particleRegion = "-stone-stone-small-0";
			minAlpha = 0.2f;
			maxAlpha = 0.8f;
			sinSclMin = 60f;
			sinSclMax = 120f;
			sinMagMin = sizeMin = 10f;
			sinMagMax = sizeMax = 80f;
			addBullets(new FallingRockBulletType(MOD_NAME + "-stone-small") {{
				speed = 2f;
				lifetime = 20f;
				fallHeight = 1f;
				fallDistance = 10f;
				variants = 3;
				hitEffect = Fx.none;
				despawnEffect = Fx2.fellStone;
			}}, 1f, new FallingRockBulletType(MOD_NAME + "-stone-medium") {{
				speed = 2f;
				lifetime = 30f;
				fallHeight = 2f;
				fallDistance = 11f;
				variants = 4;
				hitEffect = new MultiEffect(/*Fx.dynamicWave.layer(Layer.power),*/Fx2.hailStoneSplashSmall);
				despawnEffect = Fx2.fellStone;
				damage = splashDamage = 10f;
				splashDamageRadius = 25f;
			}}, 1f / 12f, new FallingRockBulletType(MOD_NAME + "-stone-big") {{
				speed = 1f;
				lifetime = 20f;
				fallHeight = 5f;
				fallDistance = 15f;
				variants = 3;
				hitSize = 12f;
				hitEffect = Fx2.hailStoneImpact;
				despawnEffect = Fx2.staticStone;
				hitSound = Sounds2.bigHailstoneHit;
				damage = splashDamage = 95f;
				splashDamageRadius = 40f;
			}}, 1f / 1600f);
		}};
	}
}
