package endfield.world.blocks.liquid;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.world.blocks.GenericPressureBlock;
import endfield.world.blocks.HasPressure;
import endfield.world.graph.PressureGraph;
import endfield.world.meta.PressureTank.TankGroup;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.Placement;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.liquid.LiquidBlock;
import mindustry.world.meta.BlockGroup;
import org.jetbrains.annotations.Nullable;

import static endfield.Vars2.MOD_PREFIX;

public class PressureLiquidBridge extends GenericPressureBlock {
	public int maxConnections = 4;
	public float range = 80;

	public float liquidPadding = 3f;
	public float smoothAlphaSpeed = 0.014f;

	public TextureRegion endRegion;
	public TextureRegion endBottomRegion;
	public TextureRegion endLiquidRegion;
	public TextureRegion bridgeRegion;
	public TextureRegion bridgeBottomRegion;
	public TextureRegion bridgeLiquidRegion;

	public TextureRegion bottomRegion;

	public @Nullable PressureLiquidBridgeBuild lastBuild;

	public PressureLiquidBridge(String name) {
		super(name);
		configurable = true;
		destructible = true;
		update = true;
		canOverdrive = false;
		swapDiagonalPlacement = true;
		group = BlockGroup.liquids;

		config(Integer.class, (PressureLiquidBridgeBuild build, Integer link) -> build.link = link);
		config(Point2.class, (PressureLiquidBridgeBuild build, Point2 link) -> {
			build.link = Point2.unpack(build.pos()).add(link).pack();
		});
		configClear((PressureLiquidBridgeBuild build) -> {
			build.link = -1;
		});
	}

	@Override
	public void changePlacementPath(Seq<Point2> points, int rotation) {
		Placement.calculateNodes(points, this, rotation, (point, other) -> point.dst(other) * Vars.tilesize <= range);
	}

	public void drawBridge(TextureRegion bridge, TextureRegion end, float x1, float y1, float x2, float y2) {
		float angle = Angles.angle(x1, y1, x2, y2);
		float dst = Mathf.dst(x1, y1, x2, y2);

		boolean flip = angle > 45f && angle < 225f;

		if (flip) Draw.yscl = -1f;

		Draw.rect(end, x1, y1, angle);
		Draw.xscl = -1f;
		Draw.rect(end, x2, y2, angle);
		Draw.xscl = Draw.yscl = 1f;

		Tmp.v1.trns(angle, end.width / 8f).add(x1, y1);
		Tmp.v2.trns(angle, dst - end.width / 8f).add(x1, y1);

		Lines.stroke(end.height / 4f * (flip ? -1f : 1f));
		Lines.line(bridge, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, false);
	}

	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid) {
		Drawf.dashCircle(x * Vars.tilesize + offset, y * Vars.tilesize + offset, range, Pal.accent);
	}

	@Override
	public void drawPlanConfigTop(BuildPlan plan, Eachable<BuildPlan> list) {
		list.each(other -> {
			if (other.block == this && plan != other && plan.config instanceof Point2 p && p.equals(other.x - plan.x, other.y - plan.y)) {
				drawBridge(bridgeBottomRegion, endBottomRegion, plan.drawx(), plan.drawy(), other.drawx(), other.drawy());
				drawBridge(bridgeRegion, endRegion, plan.drawx(), plan.drawy(), other.drawx(), other.drawy());
			}
		});
	}

	@Override
	public void handlePlacementLine(Seq<BuildPlan> plans) {
		for (int i = 0; i < plans.size - 1; i++) {
			BuildPlan cur = plans.get(i);
			BuildPlan next = plans.get(i + 1);
			if (linkValid(cur.tile(), next.tile())) {
				cur.config = new Point2(next.x - cur.x, next.y - cur.y);
			}
		}
	}

	@Override
	public void load() {
		super.load();

		endRegion = Core.atlas.find(name + "-end");
		endBottomRegion = Core.atlas.find(name + "-end-bottom");
		endLiquidRegion = Core.atlas.find(name + "-end-liquid");
		bridgeRegion = Core.atlas.find(name + "-bridge");
		bridgeBottomRegion = Core.atlas.find(name + "-bridge-bottom");
		bridgeLiquidRegion = Core.atlas.find(name + "-bridge-liquid");
		bottomRegion = Core.atlas.find(name + "-bottom", MOD_PREFIX + "liquid-bottom");
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
		if (pressureConfig.group == null) pressureConfig.group = TankGroup.transportation;
	}

	public boolean linkValid(Tile from, Tile to) {
		return from.dst(to) <= range;
	}

	@Override
	public void setBars() {
		super.setBars();

		addBar("bridge-connections", (PressureLiquidBridgeBuild entity) -> new Bar(
				() -> Core.bundle.format("bar.powerlines", entity.linked.size + (entity.getLink() == null ? 0 : 1), maxConnections),
				() -> Pal.items,
				() -> (entity.linked.size + (entity.getLink() == null ? 0f : 1f)) / maxConnections
		));
	}

	public class PressureLiquidBridgeBuild extends GenericPressureBlockBuild {
		public int link = -1;
		public IntSeq linked = new IntSeq();

		public float smoothAlpha;

		@Override
		public boolean acceptsFluid(HasPressure from, @Nullable Liquid liquid, float amount) {
			return
					super.acceptsFluid(from, liquid, amount) &&
							(liquid == pressure.getMain() || liquid == null || pressure.getMain() == null || from.pressure().getMain() == null);
		}

		public boolean acceptsLinks() {
			return (linked.size + (getLink() == null ? 0 : 1)) <= maxConnections;
		}

		@Override
		public Seq<HasPressure> connections() {
			Seq<HasPressure> o = super.connections();
			if (Vars.world.build(link) instanceof PressureLiquidBridgeBuild b) o.add(b);
			for (int pos : linked.items) if (Vars.world.build(pos) instanceof PressureLiquidBridgeBuild b) o.add(b);
			return o;
		}

		@Override
		public Point2 config() {
			return Point2.unpack(link).sub(tileX(), tileY());
		}

		@Override
		public void draw() {
			Draw.rect(bottomRegion, x, y);
			Liquid main = pressure.getMain();

			smoothAlpha = Mathf.approachDelta(smoothAlpha, main == null ? 0f : getFluid(main) / (getFluid(main) + getFluid(null)), smoothAlphaSpeed);

			if (smoothAlpha > 0.001f && main != null) {
				LiquidBlock.drawTiledFrames(size, x, y, liquidPadding, main, Mathf.clamp(smoothAlpha));
			}

			Draw.rect(region, x, y);

			Draw.z(Layer.power);

			if (getLink() != null) {
				Draw.alpha(Renderer.bridgeOpacity);
				drawBridge(bridgeBottomRegion, endBottomRegion, x, y, getLink().x, getLink().y);

				if (smoothAlpha > 0.001f && main != null) {
					Draw.color(main.color, Mathf.clamp(smoothAlpha) * Renderer.bridgeOpacity);
					drawBridge(bridgeLiquidRegion, endLiquidRegion, x, y, getLink().x, getLink().y);
					Draw.color();
				}

				Draw.alpha(Renderer.bridgeOpacity);
				drawBridge(bridgeRegion, endRegion, x, y, getLink().x, getLink().y);
			}

			Draw.reset();
		}

		@Override
		public void drawConfigure() {
			Drawf.select(x, y, size * Vars.tilesize / 2f + 2f, Pal.accent);

			if (acceptsLinks()) {
				Vars.indexer.eachBlock(this, range, other -> other != this && other.dst(this) <= range && other instanceof PressureLiquidBridgeBuild bridge && HasPressure.connects(this, bridge) && bridge.acceptsLinks(), other -> {
					if (!linked.contains(other.pos())) {
						Drawf.select(
								other.x, other.y,
								other.block.size * Vars.tilesize / 2f + 2f + (other != getLink() ? Mathf.absin(4f, 1) : 0),
								other != getLink() ? Pal.breakInvalid : Pal.place
						);
					}
				});
			} else {
				if (getLink() != null) {
					Drawf.select(getLink().x, getLink().y, getLink().block.size * Vars.tilesize / 2f + 2f, Pal.place);
				}
			}
		}

		@Override
		public void drawSelect() {
			for (int i : linked.items) {
				Building other = Vars.world.build(i);

				if (other == null) continue;

				Tmp.v1.trns(angleTo(other), 2);

				Lines.stroke(3f, Pal.gray);

				Lines.poly(other.x, other.y, 12, 2f, 0f);
				Lines.line(
						other.x - Tmp.v1.x,
						other.y - Tmp.v1.y,
						x + Tmp.v1.x,
						y + Tmp.v1.y
				);

				Lines.stroke(1f, Pal.accent);

				Lines.poly(other.x, other.y, 12, 2f, 0f);
				Lines.line(
						other.x - Tmp.v1.x,
						other.y - Tmp.v1.y,
						x + Tmp.v1.x,
						y + Tmp.v1.y
				);
			}

			if (getLink() != null) {
				Building other = getLink();

				Tmp.v1.trns(angleTo(other), 2);

				Lines.stroke(3f, Pal.gray);

				Lines.poly(other.x, other.y, 12, 2f, 0f);
				Lines.line(
						other.x - Tmp.v1.x,
						other.y - Tmp.v1.y,
						x + Tmp.v1.x,
						y + Tmp.v1.y
				);

				Lines.stroke(1f, Pal.place);

				Lines.poly(other.x, other.y, 12, 2f, 0f);
				Lines.line(
						other.x - Tmp.v1.x,
						other.y - Tmp.v1.y,
						x + Tmp.v1.x,
						y + Tmp.v1.y
				);
			}
		}

		public @Nullable PressureLiquidBridgeBuild getLink() {
			return Vars.world.build(link) instanceof PressureLiquidBridgeBuild bridge ? bridge : null;
		}

		@Override
		public boolean onConfigureBuildTapped(Building other) {
			if (other instanceof PressureLiquidBridgeBuild bridge && HasPressure.connects(this, bridge)) {
				if (bridge.link == pos()) {
					linked.removeValue(other.pos());
					bridge.linked.add(pos());
					configure(other.pos());
					other.configure(-1);
				} else if (linkValid(this.tile, other.tile)) {
					if (other == this) {
						if (getLink() != null) {
							getLink().linked.removeValue(pos());
							configure(-1);
						}
						return false;
					}
					if (link == other.pos()) {
						bridge.linked.removeValue(pos());
						configure(-1);
					} else if (acceptsLinks() && bridge.acceptsLinks()) {
						if (getLink() != null) getLink().linked.removeValue(pos());
						bridge.linked.add(pos());
						configure(other.pos());
					}
					return false;
				}
			}
			return true;
		}

		@Override
		public void pickedUp() {
			link = -1;
		}

		@Override
		public void playerPlaced(Object config) {
			super.playerPlaced(config);

			if (tile == null || lastBuild == null || !linkValid(tile, lastBuild.tile) || lastBuild.tile == tile || lastBuild.link != -1)
				return;

			Tile link = lastBuild.tile;
			if (linkValid(tile, link) && this.link != link.pos() && !proximity.contains(link.build)) {
				link.build.configure(tile.pos());
			}

			lastBuild = this;
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			link = read.i();

			byte size = read.b();

			for (int i = 0; i < size; i++) {
				int otherLink = read.i();
				linked.add(otherLink);
			}

			smoothAlpha = read.f();
		}

		@Override
		public void updateTile() {
			if (getLink() != null && !getLink().linked.contains(pos())) {
				getLink().linked.add(pos());
				new PressureGraph().floodMergeGraph(this);
			}
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			write.i(link);

			write.b(linked.size);

			linked.each(write::i);

			write.f(smoothAlpha);
		}
	}
}
