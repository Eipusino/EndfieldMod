package endfield.world.blocks.defense;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Structs;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.math.Mathm;
import endfield.world.blocks.GenericPressureBlock;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.ui.Bar;
import mindustry.world.consumers.Consume;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class Shelter extends GenericPressureBlock {
	private static final Seq<Building> sharedBuildings = new Seq<>(Building.class);
	private final int retargetTimer = timers++;

	public float range = 80f;
	public float minRange = 12f;
	public float retargetTime = 10f;
	public float rotateSpeed = 1f;
	public float growSpeed = 1f;
	public float warmupSpeed = 0.014f;
	public Color arcColor = Pal.heal;
	public Color glowColor = Pal.heal;
	public float glowScl = 2.84f, glowMag = 0.02f;

	public float shieldHealth = 100;
	public float shieldHeal = 0.1f;

	public Sound startSound = Sounds.none;
	public float startSoundVolume = 0.05f;

	public boolean scaleEfficiency = true;

	public Effect shieldHealEffect = Fx.none, shieldBreakEffect = Fx.none;

	public TextureRegion glowRegion;

	public DrawBlock drawer = new DrawDefault();

	public Shelter(String name) {
		super(name);
		update = rotate = true;
		quickRotate = drawArrow = false;
	}

	@Override
	public void load() {
		super.load();
		drawer.load(this);
		glowRegion = Core.atlas.find(name + "-glow");
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
		drawer.drawPlan(this, plan, list);
		Draw.rect(region, plan.drawx(), plan.drawy(), plan.rotation * 90 - 90);
	}

	@Override
	public void drawOverlay(float x, float y, int rotation) {
		Drawf.dashCircle(x, y, range, Pal.placing);
	}

	@Override
	protected TextureRegion[] icons() {
		return Structs.add(drawer.icons(this), region);
	}

	@Override
	public void init() {
		super.init();
		updateClipRadius(range + 20f);
	}

	@Override
	public void setBars() {
		super.setBars();
		addBar("shield", (ShelterBuild b) -> new Bar("stat.shieldhealth", Pal.accent, () -> Mathf.clamp(b.shield / shieldHealth)).blink(Color.white));
	}

	@Override
	public void setStats() {
		super.setStats();
		stats.add(Stat.shieldHealth, shieldHealth);
		stats.add(Stat.regenerationRate, shieldHeal * 60f, StatUnit.seconds);
	}

	public class ShelterBuild extends GenericPressureBlockBuild {
		public float shield = shieldHealth;
		public boolean broken = false;

		public float warmup, targetRotation, currentRotation, targetArcLength, currentArcLength;
		public float targetLRadius, currentLRadius, targetRRadius, currentRRadius;
		public float lastCoverageRange = -1f;
		public Seq<Building> myBuildings = new Seq<>(Building.class);

		@Override
		public void created() {
			super.created();
			currentRotation = targetRotation = rotation * 90;
		}

		@Override
		public float warmup() {
			return warmup;
		}

		@Override
		public void draw() {
			drawer.draw(this);
			float headRotation = currentRotation + currentArcLength / 2f - 90f;
			Draw.rect(region, x, y, headRotation);
			if (glowRegion.found() && warmup > 0.001f) {
				float glowAlpha = Mathf.absin(Time.time, glowScl, glowMag) * warmup;
				Drawf.additive(glowRegion, glowColor, glowAlpha, x, y, headRotation, Layer.blockAdditive);
			}
			drawArc();
		}

		public void drawArc() {
			//Draw.z(OlShaders.shelterShieldLayer);
			Draw.z(Layer.shields);
			Draw.color(arcColor);
			Fill.circle(x, y, minRange * warmup * efficiency);
			float ov = 4f;
			if (currentArcLength > 0.01f)
				Fill.arc(x, y, range * warmup * efficiency, (currentArcLength + ov) / 360f, currentRotation - ov / 2f);
			if (currentLRadius > 0.01f)
				Fill.arc(x, y, currentLRadius * warmup, (90f + ov) / 360f, currentRotation + currentArcLength - ov / 2f);
			if (currentRRadius > 0.01f)
				Fill.arc(x, y, currentRRadius * warmup, (90f + ov) / 360f, currentRotation - 90f - ov / 2f);
		}

		@Override
		public void drawSelect() {
			Drawf.dashCircle(x, y, efficiency > 0 ? range * efficiency : range, Pal.placing);
		}

		@Override
		public float efficiencyScale() {
			float mul = 1f;
			if (scaleEfficiency) for (Consume cons : consumers) {
				mul *= cons.efficiencyMultiplier(this);
			}
			return mul;
		}

		public float coverageRange() {
			return range * Math.max(efficiency, 1f);
		}

		public void retarget(float coverage) {
			sharedBuildings.clear();
			team.data().buildingTree.intersect(x - coverage, y - coverage, coverage * 2, coverage * 2, sharedBuildings);

			Vec2 tPos = Tmp.v1.setZero();
			int weight = 0;
			myBuildings.clear();

			for (Building b : sharedBuildings) {
				int s = b.block.size * b.block.size;
				float size = Mathf.sqrt2 * b.block.size * 8f;
				if (b == this || b.dst(this) > coverage + size) continue;
				tPos.add(b.x * s, b.y * s);
				weight += s;
				myBuildings.add(b);
			}

			if (weight == 0) {
				targetArcLength = targetLRadius = targetRRadius = 0;
				return;
			}

			float base = tPos.scl(1f / weight).sub(this).angle(), min = 0, max = 0;
			boolean first = true;

			for (Building b : myBuildings) {
				float size = Mathf.sqrt2 * b.block.size * 8f;
				for (int s = 0; s < 4; s++) {
					float d = Mathm.angleDistSigned(base, angleTo(Tmp.v3.trns(s * 90 - 45, size).add(b.x, b.y)));
					if (first) {
						min = max = d;
						first = false;
					} else {
						min = Math.min(min, d);
						max = Math.max(max, d);
					}
				}
			}

			targetArcLength = Math.min(180f, max - min);
			targetRotation = base + (min + max) / 2f - targetArcLength / 2f;
		}

		@Override
		public double sense(LAccess sensor) {
			if (sensor == LAccess.shield) return broken ? 0f : shield;
			return super.sense(sensor);
		}

		@Override
		public boolean shouldConsume() {
			return super.shouldConsume() && (targetArcLength > 0 || targetLRadius > 0 || targetRRadius > 0);
		}

		@Override
		public void updateTile() {
			float coverage = coverageRange();
			if (timer(retargetTimer, retargetTime) || !Mathf.equal(lastCoverageRange, coverage, 0.5f)) {
				retarget(coverage);
				lastCoverageRange = coverage;
			}

			if (efficiency > 0) {
				boolean wasWorking = warmup > 0f;
				warmup = Mathf.approach(warmup, broken ? 0f : 1f, warmupSpeed * edelta());
				if (!wasWorking && warmup > 0f && !broken) startSound.at(x, y, 1f, startSoundVolume);
				currentRotation = Angles.moveToward(currentRotation, targetRotation, rotateSpeed * edelta());
				currentArcLength = Mathf.approach(currentArcLength, targetArcLength, growSpeed * edelta());

				shield = Mathf.approachDelta(shield, shieldHealth, shieldHeal);

				if (shield >= shieldHealth && broken) {
					broken = false;
					shieldHealEffect.at(x, y);
				}

				float nextL = 0, nextR = 0, center = currentRotation + currentArcLength / 2f;
				for (Building b : myBuildings) {
					float size = Mathf.sqrt2 * b.block.size * 8f;
					if (!b.isValid() || b.dst(this) > coverage + size) continue;
					for (int s = 0; s < 4; s++) {
						Vec2 corner = Tmp.v3.trns(s * 90 - 45, size).add(b.x, b.y);
						float d = dst(corner), rel = Mathm.angleDistSigned(center, angleTo(corner));
						if (rel > currentArcLength / 2f + 1f && rel < currentArcLength / 2f + 91f)
							nextR = Math.max(nextR, Math.min(d, coverage));
						else if (rel < -currentArcLength / 2f - 1f && rel > -currentArcLength / 2f - 91f)
							nextL = Math.max(nextL, Math.min(d, coverage));
					}
				}

				targetLRadius = nextL;
				targetRRadius = nextR;
				float step = growSpeed * 6f * edelta();
				currentLRadius = Mathf.approach(currentLRadius, targetLRadius, step);
				currentRRadius = Mathf.approach(currentRRadius, targetRRadius, step);

				if (!broken) {
					Groups.bullet.intersect(x - coverage, y - coverage, coverage * 2, coverage * 2, b -> {
						if (b.team != Team.derelict || !b.type.absorbable) return;
						float bRel = Mathm.angleDistSigned(center, angleTo(b)), d = dst(b);
						if (d < minRange * efficiency || (currentArcLength > 0.01f && d < coverage && Math.abs(bRel) < currentArcLength / 2f) ||
								(currentRRadius > 0.01f && d < currentRRadius && bRel > currentArcLength / 2f && bRel < currentArcLength / 2f + 90f) ||
								(currentLRadius > 0.01f && d < currentLRadius && bRel < -currentArcLength / 2f && bRel > -currentArcLength / 2f - 90f)) {
							shield -= b.type.shieldDamage(b);
							if (shield > 0) {
								b.absorb();
							} else {
								broken = true;
								b.damage = -shield;
								shield = 0f;
								shieldBreakEffect.at(x, y);
							}
						}
					});
				}
			} else {
				warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
			}
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			write.f(warmup);
			write.f(targetRotation);
			write.f(currentRotation);
			write.f(targetArcLength);
			write.f(currentArcLength);
			write.f(targetLRadius);
			write.f(currentLRadius);
			write.f(targetRRadius);
			write.f(currentRRadius);

			write.f(shield);
			write.bool(broken);
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			warmup = read.f();
			targetRotation = read.f();
			currentRotation = read.f();
			targetArcLength = read.f();
			currentArcLength = read.f();
			targetLRadius = read.f();
			currentLRadius = read.f();
			targetRRadius = read.f();
			currentRRadius = read.f();

			shield = read.f();
			broken = read.bool();
		}
	}
}
