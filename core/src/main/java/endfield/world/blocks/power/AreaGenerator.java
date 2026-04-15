package endfield.world.blocks.power;

import arc.func.Boolf;
import arc.func.Intc2;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Time;
import endfield.world.meta.Stats2;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.game.Teams.BlockPlan;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.input.Placement;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.consumers.Consume;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;
import org.jetbrains.annotations.Nullable;

public class AreaGenerator extends ConsumeGenerator {
	public int range = 5;

	/**
	 * Damage multiplier applied if the placement check is worked around (e.g. trough editor).
	 */
	public float crowdingDamageScale = 0.01f;

	public Effect crowdingEffect = Fx.fire;

	public AreaGenerator(String name) {
		super(name);
	}

	@Override
	public boolean canPlaceOn(Tile tile, Team team, int rotation) {
		if (range == 0) return true;

		boolean nearbyBlock = checkNearby(tile.x, tile.y, ntile -> {
			if (ntile == null) return false;
			if (ntile.block() == this) return true;
			return ntile.build instanceof ConstructBuild cb && cb.current == this;
		});

		if (nearbyBlock) return false;

		int r = range + Mathf.ceil(size / 2f) + ((size + 1) % 2);

		for (Unit unit : Groups.unit) {
			if (unit.team != team) continue;

			for (BuildPlan plan : unit.plans()) {
				if (plan.block == this && !plan.breaking) {
					if (plan.x == tile.x && plan.y == tile.y) continue;
					if (Math.abs(plan.x - tile.x) < r && Math.abs(plan.y - tile.y) < r) {
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public void changePlacementPath(Seq<Point2> points, int rotation) {
		Placement.calculateNodes(points, this, rotation, (point, other) -> Math.max(Math.abs(point.x - other.x), Math.abs(point.y - other.y)) <= range + Mathf.ceil(size / 2f) + ((size + 1) % 2));
	}

	public void checkNearby(int x, int y, Intc2 pos) {
		int r = range + Mathf.ceil(size / 2f) + ((size + 1) % 2);
		for (int i = -r + 1; i < r; i++) {
			for (int j = -r + 1; j < r; j++) {
				pos.get(x + i, y + j);
			}
		}
	}

	public boolean checkNearby(int x, int y, Boolf<@Nullable Tile> pos) {
		int r = range + Mathf.ceil(size / 2f) + ((size + 1) % 2);
		for (int i = -r + 1; i < r; i++) {
			for (int j = -r + 1; j < r; j++) {
				if (pos.get(Vars.world.tile(x + i, y + j))) return true;
			}
		}
		return false;
	}

	@Override
	public void drawOverlay(float x, float y, int rotation) {
		if (range > 0) Drawf.dashSquare(Pal.accent, x, y, (range + size / 2f) * Vars.tilesize * 2f);
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
		super.drawPlanRegion(plan, list);
		Draw.mixcol();
		Tile tile = plan.tile();
		if (range < 1 || tile == null) return;

		int r = range + Mathf.ceil(size / 2f) + ((size + 1) % 2);

		for (int i = -r + 1; i < r; i++) {
			for (int j = -r + 1; j < r; j++) {
				Tile t = Vars.world.tile(tile.x + i, tile.y + j);
				if (t == null) continue;

				boolean isMyBlock = t.block() == this;
				boolean isConstructing = t.build instanceof ConstructBuild cb && cb.current == this;

				if ((isMyBlock || isConstructing) && t.build != null) {
					Drawf.selected(t.x, t.y, this, Pal.remove);
				}
			}
		}

		list.each(other -> {
			if (other.block == this && other != plan) {
				if (Math.abs(other.x - plan.x) < r && Math.abs(other.y - plan.y) < r) {
					Drawf.selected(other.x, other.y, other.block, Pal.remove);
				}
			}
		});

		for (BlockPlan other : Vars.player.team().data().plans) {
			if (other.block == this) {
				if (other.x == plan.x && other.y == plan.y) continue;
				if (Math.abs(other.x - plan.x) < r && Math.abs(other.y - plan.y) < r) {
					Drawf.selected(other.x, other.y, other.block, Pal.lightishGray);
				}
			}
		}
	}

	@Override
	public void setStats() {
		super.setStats();
		stats.add(Stats2.space, StatValues.squared(range, StatUnit.blocks));
	}

	public class AreaGeneratorBuild extends ConsumeGeneratorBuild {
		public float checkTimer = 0f;
		public float crowdingFactor = 0f;
		public float smoothCrowding = 0f;

		@Override
		public void placed() {
			super.placed();

			int r = range + Mathf.ceil(size / 2f) + ((size + 1) % 2);
			var it = team.data().plans.iterator();
			while (it.hasNext()) {
				BlockPlan p = it.next();
				if (p.block == block && Math.abs(p.x - tile.x) < r && Math.abs(p.y - tile.y) < r) {
					it.remove();
				}
			}
		}

		@Override
		public void updateTile() {
			super.updateTile();

			if ((checkTimer += Time.delta) >= 20f) {
				checkTimer = 0f;
				calculateCrowding();
			}

			smoothCrowding = Mathf.lerpDelta(smoothCrowding, crowdingFactor, 0.05f);

			if (smoothCrowding > 0.001f) {
				if (Mathf.chanceDelta(smoothCrowding * 0.15f)) {
					crowdingEffect.at(x + Mathf.range(size * Vars.tilesize / 2f), y + Mathf.range(size * Vars.tilesize / 2f), rotation, Pal.remove);
				}

				damage(maxHealth * smoothCrowding * crowdingDamageScale * Time.delta);

				if (warmup > 0) {
					warmup -= smoothCrowding * 0.05f * Time.delta;
					if (warmup < 0) warmup = 0;
				}

				updateEfficiencyMultiplier();
			}
		}

		@Override
		public void heal(float amount) {
			if (crowdingFactor > 0.01f) return;
			super.heal(amount);
		}

		public void calculateCrowding() {
			int r = range + Mathf.ceil(size / 2f) + ((size + 1) % 2);
			float rangePixel = (range + size / 2f + 1) * Vars.tilesize;
			float maxFactor = 0f;

			for (int i = -r + 1; i < r; i++) {
				for (int j = -r + 1; j < r; j++) {
					Tile other = Vars.world.tile(tile.x + i, tile.y + j);

					if (other != null && other.block() == block && other.build != this && other.build != null) {
						float dist = Math.max(Math.abs(other.build.x - x), Math.abs(other.build.y - y));

						if (dist < rangePixel) {
							float factor = 1f - (dist / rangePixel);
							if (factor > maxFactor) maxFactor = factor;
						}
					}
				}
			}

			crowdingFactor = maxFactor;
		}

		@Override
		public void updateEfficiencyMultiplier() {
			super.updateEfficiencyMultiplier();

			if (filterItem == null || filterLiquid == null) efficiencyMultiplier = 1f;
			for (Consume cons : block.optionalConsumers) {
				if (cons.booster) {
					efficiencyMultiplier *= cons.efficiencyMultiplier(this);
				} else {
					efficiencyMultiplier += cons.efficiencyMultiplier(this);
				}
			}

			if (smoothCrowding > 0f) {
				efficiencyMultiplier *= Math.max(0f, 1f - smoothCrowding);
			}
		}
	}
}
