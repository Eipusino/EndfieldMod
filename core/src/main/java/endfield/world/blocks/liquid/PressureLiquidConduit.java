package endfield.world.blocks.liquid;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.content.Blocks2;
import endfield.type.FluidInteraction;
import endfield.util.Sprites;
import endfield.world.blocks.ConnectedTile;
import endfield.world.blocks.GenericPressureBlock;
import endfield.world.blocks.HasPressure;
import endfield.world.blocks.PressureBlock;
import endfield.world.meta.PressureConfig;
import endfield.world.meta.PressureTank.TankGroup;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import org.jetbrains.annotations.Nullable;

import static endfield.Vars2.MOD_PREFIX;

public class PressureLiquidConduit extends GenericPressureBlock implements ConnectedTile {
	private static final Seq<BuildPlan> plansTmp = new Seq<>(BuildPlan.class);

	public TextureRegion bottomRegion;
	public TextureRegion[] topRegions;
	public TextureRegion[][] liquidRegions;
	public float liquidPadding = 3f;
	public float smoothAlphaSpeed = 0.014f;
	public @Nullable Block junctionReplacement;
	public @Nullable PressureLiquidBridge bridgeReplacement;

	public PressureLiquidConduit(String name) {
		super(name);
		rotate = true;
		destructible = true;
		conveyorPlacement = true;
		update = true;
		canOverdrive = false;
		group = BlockGroup.liquids;
	}

	@Override
	public boolean connectsTo(BuildPlan ref, BuildPlan other) {
		return facingEdge(ref, other, ref.rotation % 2) || facingEdge(ref, other, 2 + ref.rotation % 2);
	}

	@Override
	public Block getReplacement(BuildPlan req, Seq<BuildPlan> plans) {
		if (junctionReplacement == null) return this;

		Boolf<Point2> cont = p -> plans.contains(o -> o.x == req.x + p.x && o.y == req.y + p.y && (req.block instanceof PressureLiquidConduit || req.block instanceof PressureLiquidJunction));
		return cont.get(Geometry.d4(req.rotation)) &&
				cont.get(Geometry.d4(req.rotation - 2)) &&
				req.tile() != null &&
				req.tile().block() instanceof PressureLiquidConduit &&
				Mathf.mod(req.build().rotation - req.rotation, 2) == 1 ? junctionReplacement : this;
	}

	@Override
	protected TextureRegion[] icons() {
		return new TextureRegion[]{bottomRegion, region};
	}

	@Override
	public void init() {
		pressureConfig.hasPressure = pressureConfig.acceptsPressure = pressureConfig.outputsPressure = true;

		super.init();

		if (hasLiquids) hasLiquids = false;
		if (junctionReplacement == null) junctionReplacement = Blocks2.pressureLiquidJunction;
		if (bridgeReplacement == null) bridgeReplacement = Blocks2.pressureLiquidBridge;

		if (pressureConfig.group == null) pressureConfig.group = TankGroup.transportation;
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
		int tiling = mask(plan, list);

		Draw.rect(bottomRegion, plan.drawx(), plan.drawy(), 0);
		if (tiling == 0) {
			Draw.rect(topRegions[tiling], plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f % 180 - 90);
		} else {
			Draw.rect(topRegions[tiling], plan.drawx(), plan.drawy(), 0);
		}
	}

	@Override
	public void load() {
		super.load();

		liquidRegions = new TextureRegion[2][Liquid.animationFrames];
		if (Vars.renderer != null) {
			TextureRegion[][] frames = Vars.renderer.getFluidFrames();

			for (int fluid = 0; fluid < 2; fluid++) {
				for (int frame = 0; frame < Liquid.animationFrames; frame++) {
					TextureRegion base = frames[fluid][frame];
					TextureRegion result = new TextureRegion();
					result.set(base);

					result.setHeight(result.height - liquidPadding);
					result.setWidth(result.width - liquidPadding);
					result.setX(result.getX() + liquidPadding);
					result.setY(result.getY() + liquidPadding);

					liquidRegions[fluid][frame] = result;
				}
			}
		}

		bottomRegion = Core.atlas.find(name + "-bottom", MOD_PREFIX + "liquid-bottom");
		topRegions = Sprites.splitLayer(name + "-sheet", 32, 0);
	}

	@Override
	public int mask(BuildPlan plan, Eachable<BuildPlan> list) {
		final int[] result = {0};
		final Point2[] edges = getEdges();
		if (edges == null) return 0;

		final float myX = plan.x + size / 2f;
		final float myY = plan.y + size / 2f;

		list.each(next -> updateMask(result, plan, next, edges, myX, myY));

		return result[0];
	}

	private void updateMask(int[] result, BuildPlan plan, BuildPlan next, Point2[] edges, float myX, float myY) {
		if (next.breaking || next == plan || !(next.block instanceof PressureBlock pb)) return;

		final PressureConfig config = pb.pressureConfig();
		if (config == null || !config.hasPressure) return;

		final int otherSize = next.block.size;
		if (Math.abs(next.x - plan.x) > otherSize + 1 || Math.abs(next.y - plan.y) > otherSize + 1) return;

		final float nx1 = next.x - (otherSize - 1f) / 2f;
		final float ny1 = next.y - (otherSize - 1f) / 2f;

		for (int i = 0; i < edges.length; i++) {
			if ((result[0] & (1 << i)) != 0) continue;

			final Point2 edge = edges[i];
			if (canConnect(plan, next, myX + edge.x, myY + edge.y, nx1, ny1, otherSize)) {
				result[0] |= (1 << i);
			}
		}
	}

	private boolean canConnect(BuildPlan plan, BuildPlan next, float ex, float ey, float nx1, float ny1, int otherSize) {
		return ex >= nx1 && ex <= nx1 + otherSize && ey >= ny1 && ey <= ny1 + otherSize &&
				(!(next.block instanceof ConnectedTile && !((ConnectedTile) next.block).connectsTo(next, plan)) || connectsTo(plan, next));
	}

	@Override
	public void handlePlacementLine(Seq<BuildPlan> plans) {
		if (bridgeReplacement == null) return;

		Boolf<BuildPlan> placeable = plan ->
				(plan.placeable(Vars.player.team()) || (plan.tile() != null && plan.tile().block() == plan.block)) &&  //don't count the same block as inaccessible
						!(plan != plans.first() && plan.build() != null && plan.build().rotation % 2 != plan.rotation % 2);

		plansTmp.clear();

		for (int i = 0; i < plans.size; i++) {
			BuildPlan plan = plans.get(i);
			BuildPlan next = null;

			plansTmp.add(plan);

			if (!placeable.get(plan)) continue;

			int oldI = i;

			int j = i + 1;
			boolean same = true;
			while (j < plans.size) {
				if (placeable.get(plans.get(j))) {
					next = plans.get(j);
					i = j - 1;
					break;
				} else if (plans.get(j).tile() != null && plans.get(j).tile().block() != this) {
					same = false;
				}
				j++;
			}

			if (next == null || plan.block != this || next.block != this) continue;

			if (bridgeReplacement.linkValid(plan.tile(), next.tile()) && plan.dst(next) > 8 && !same) {
				plan.block = bridgeReplacement;
				next.block = bridgeReplacement;

				plan.config = new Point2(next.x - plan.x, next.y - plan.y);
			} else {
				i = oldI;
			}
		}

		plans.set(plansTmp);
	}

	public class PressureLiquidConduitBuild extends GenericPressureBlockBuild {
		public int tiling = 0;
		public float smoothAlpha;

		@Override
		public boolean acceptsFluid(HasPressure from, @Nullable Liquid liquid, float amount) {
			Liquid main = pressure.getMain();
			return
					super.acceptsFluid(from, liquid, amount) &&
							(liquid == main || liquid == null || main == null || from.pressure().getMain() == null || FluidInteraction.interactions.contains(i -> i.canInteract(main, liquid)));
		}

		@Override
		public boolean connects(HasPressure to) {
			return super.connects(to) && (
					!(to instanceof PressureLiquidConduitBuild) ||
							to == front() || to == back() ||
							this == ((Building) to).front() || this == ((Building) to).back() ||
							!proximity.contains((Building) to));
		}

		@Override
		public void draw() {
			Draw.rect(bottomRegion, x, y);
			Liquid main = pressure.getMain();

			smoothAlpha = Mathf.approachDelta(smoothAlpha, main == null ? 0f : getFluid(main) / (getFluid(main) + Math.abs(getFluid(null))), smoothAlphaSpeed);

			if (smoothAlpha > 0.001f && main != null) {
				int frame = main.getAnimationFrame();
				int gas = main.gas ? 1 : 0;

				float xscl = Draw.xscl, yscl = Draw.yscl;
				Draw.scl(1f, 1f);
				Drawf.liquid(liquidRegions[gas][frame], x, y, Mathf.clamp(smoothAlpha), main.color.write(Tmp.c1).a(1f));
				Draw.scl(xscl, yscl);
			}
			Draw.rect(topRegions[tiling], x, y, tiling != 0 ? 0 : (rotdeg() + 90) % 180 - 90);
		}

		@Override
		public void onPressureGraphUpdate() {
			tiling = 0;
			for (int i = 0; i < 4; i++) {
				HasPressure build = nearby(i) instanceof HasPressure ? ((HasPressure) nearby(i)).getFluidDestination(this, null) : null;
				if (
						build != null && HasPressure.connects(this, build)
				) tiling |= (1 << i);
			}
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			smoothAlpha = read.f();
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			write.f(smoothAlpha);
		}
	}
}
