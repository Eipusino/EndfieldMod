package endfield.world.blocks.defense.turrets;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.Units;
import mindustry.entities.Units.Sortf;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.blocks.defense.turrets.BaseTurret;

public class HackTurret extends BaseTurret {
	public Seq<Unit> targets = new Seq<>(Unit.class);

	public TextureRegion baseRegion;
	public TextureRegion laser;
	public TextureRegion laserEnd;

	public float shootCone = 6f;
	public float shootLength = 5f;
	public float laserWidth = 0.6f;
	public float damage = 0.5f;
	public boolean targetAir = true;
	public boolean targetGround = true;
	public Color laserColor = Color.white;
	public Sound shootSound = Sounds.beamParallax;
	public float shootSoundVolume = 0.9f;

	public Sortf unitSort = Position::dst2;

	public HackTurret(String name) {
		super(name);
	}

	@Override
	public void load() {
		super.load();

		baseRegion = Core.atlas.find("block-" + size);
		laser = Core.atlas.find(name + "-laser");
		laserEnd = Core.atlas.find(name + "-laser-end");
	}

	@Override
	protected TextureRegion[] icons() {
		return new TextureRegion[]{baseRegion, region};
	}

	public class HackTurretBuild extends BaseTurretBuild {
		public Unit target;
		public float lastX = 0f, lastY = 0f;
		public float progress = 0f;
		public float normalProgress = 0f;

		@Override
		public void updateTile() {
			if (validateTarget()) {
				if (!Vars.headless) {
					Vars.control.sound.loop(shootSound, this, shootSoundVolume);
				}

				var dest = angleTo(target);
				rotation = Angles.moveToward(rotation, dest, rotateSpeed * edelta());

				lastX = target.x;
						lastY = target.y;

				if (Angles.within(rotation, dest, shootCone)) {
					progress += edelta() * damage;
					normalProgress = progress / target.maxHealth();
					if (progress > target.maxHealth()) {
						target.team(team());
						reset();
					}
				} else {
					reset();
				}
			} else {
				reset();
				findTarget();
			}
		}

		public void findTarget() {
			target = Units.bestEnemy(
					team, x, y, range, e -> !e.dead() && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround) && !e.spawnedByCore && !targets.contains(e),
					unitSort);

			if (target != null) {
				targets.add(target);
				lastX = target.x;
				lastY = target.y;
			}
		}

		public void reset() {
			progress = 0f;
			targets.remove(target);
			target = null;
		}

		public boolean validateTarget() {
			return !Units.invalidateTarget(target, team, x, y, range) && efficiency > 0.02f;
		}

		@Override
		public void onRemoved() {
			targets.remove(target);
			super.onRemoved();
		}

		@Override
		public void draw() {
			drawTurret();
			if (target != null) {
				drawLaser();
				drawProgress();
			}
		}

		public void drawTurret() {
			Draw.rect(baseRegion, x, y);
			Drawf.shadow(region, x - size / 2f, y - size / 2f, rotation - 90);
			Draw.rect(region, x, y, rotation - 90);
		}

		public void drawLaser() {
			Draw.z(Layer.bullet);
			var ang = angleTo(lastX, lastY);
			Draw.mixcol(laserColor, Mathf.absin(4f, 0.6f));
			Drawf.laser(
					laser, laserEnd,
					x + Angles.trnsx(ang, shootLength), y + Angles.trnsy(ang, shootLength),
					lastX, lastY, efficiency * laserWidth
			);
			Draw.mixcol();
		}

		public void drawProgress() {
			Drawf.target(lastX, lastY, target.hitSize, normalProgress, team.color);
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			write.f(rotation);
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			rotation = read.f();
		}

		@Override
		public boolean shouldConsume() {
			return target != null;
		}
	}
}
