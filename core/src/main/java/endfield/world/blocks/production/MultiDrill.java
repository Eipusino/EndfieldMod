package endfield.world.blocks.production;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.util.ObjectFloatMap2;
import endfield.util.ObjectIntMap2;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.mod.NoPatch;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.meta.BlockGroup;

public class MultiDrill extends Block {
	@NoPatch
	public ObjectIntMap2<Item> oreCount = new ObjectIntMap2<>(Item.class);

	@NoPatch
	public Point2[] prox;

	public float hardnessDrillMultiplier = 50f;
	public float drillTime = 280f;
	public float liquidBoostIntensity = 1.8f;

	public float warmupSpeed = 0.01f;
	public float rotateSpeed = 6f;

	public Effect drillEffect = Fx.mineHuge;
	public Effect updateEffect = Fx.pulverizeRed;
	public float updateEffectChance = 0.03f;

	public Color heatColor = new Color(0xff5512);

	public TextureRegion rimRegion;
	public TextureRegion rotatorRegion;
	public TextureRegion topRegion;

	public MultiDrill(String name) {
		super(name);

		update = true;
		solid = true;
		group = BlockGroup.drills;
		hasLiquids = true;
		hasItems = true;
		ambientSound = Sounds.loopDrill;
		ambientSoundVolume = 0.018f;
	}

	@Override
	public void load() {
		super.load();

		rimRegion = Core.atlas.find(name + "-rim");
		rotatorRegion = Core.atlas.find(name + "-rotator");
		topRegion = Core.atlas.find(name + "-top");
	}

	@Override
	public void init() {
		super.init();

		int bot = (int) (-((size - 1) / 2f)) - 1;
		int top = (int) ((size - 1) / 2f + 0.5f) + 1;

		prox = new Point2[]{new Point2(bot, bot), new Point2(bot, top), new Point2(top, top), new Point2(top, bot)};
	}

	@Override
	protected TextureRegion[] icons() {
		return new TextureRegion[]{region, rotatorRegion, topRegion};
	}

	@Override
	public boolean canPlaceOn(Tile tile, Team team, int rotation) {
		if (tile != null) {
			for (var other : tile.getLinkedTilesAs(this, tempTiles)) {
				if (canMine(other)) {
					return true;
				}
			}
			for (var edge : Edges.getInsideEdges(size + 2)) {
				var other = Vars.world.tile(tile.x + edge.x, tile.y + edge.y);
				if (canMine(other)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid) {
		super.drawPlace(x, y, rotation, valid);

		var tile = Vars.world.tile(x, y);
		if (tile == null) return;

		countOre(tile);

		var off = 0;
		for (var ore : oreCount.keys()) {
			var dx = x * Vars.tilesize + offset - 16;
			var dy = y * Vars.tilesize + offset + size * Vars.tilesize / 2f;
			Draw.mixcol(Color.darkGray, 1f);
			var itemRegion = ore.fullIcon;
			Draw.rect(itemRegion, dx + off, dy - 1);
			Draw.reset();
			Draw.rect(itemRegion, dx + off, dy);
			off += 8;
		}
		Draw.reset();

		Draw.color(Pal.placing);
		Lines.stroke(size / 2f);
		Lines.square(x * Vars.tilesize + offset, y * Vars.tilesize + offset, (Vars.tilesize / 2f) * (size + 2f));
	}

	public void countOre(Tile tile) {
		oreCount.clear();

		for (Tile other : tile.getLinkedTilesAs(this, tempTiles)) {
			if (canMine(other)) {
				oreCount.increment(other.drop(), 0, 1);
			}
		}

		Point2[] edges = Edges.getEdges(size);
		for (Point2 edge : edges) {
			countOre(tile, edge);
		}
		for (Point2 edge : prox) {
			countOre(tile, edge);
		}
	}

	public void countOre(Tile tile, Point2 edge) {
		var other = Vars.world.tile(tile.x + edge.x, tile.y + edge.y);
		if (canMine(other)) {
			oreCount.increment(other.drop(), 0, 1);
		}
	}

	public boolean canMine(Tile tile) {
		return tile != null && tile.drop() != null;
	}

	public class MultiDrillBuild extends Building {
		public ObjectIntMap2<Item> ores = new ObjectIntMap2<>(Item.class);

		public ObjectFloatMap2<Item> oreProgress = new ObjectFloatMap2<>(Item.class);

		public float timeDrilled = 0f;
		public float warmup = 0f;

		@Override
		public boolean shouldAmbientSound() {
			return efficiency > 0.01f && items.total() < itemCapacity;
		}

		@Override
		public float ambientVolume() {
			return efficiency * (size * size) / 4f;
		}

		@Override
		public void drawSelect() {
			var off = 0;
			for (var ore : ores.keys()) {
				var dx = x - size * Vars.tilesize / 2f;
				var dy = y + size * Vars.tilesize / 2f;
				Draw.mixcol(Color.darkGray, 1f);
				var itemRegion = ore.fullIcon;
				Draw.rect(itemRegion, dx + off, dy - 1);
				Draw.reset();
				Draw.rect(itemRegion, dx + off, dy);
				off += 8;
			}
			Draw.reset();
			Draw.color(Pal.placing);
			Lines.stroke(size / 2f);
			Lines.square(tileX() * Vars.tilesize + offset, tileY() * Vars.tilesize + offset, (Vars.tilesize / 2f) * (size + 2f));
		}

		@Override
		public void drawCracks() {}

		@Override
		public void onProximityUpdate() {
			countOre(tile);
			ores.clear();
			oreProgress.clear();
			for (var ore : oreCount) {
				ores.put(ore.key, ore.value);
			}
		}

		@Override
		public void updateTile() {
			if (ores.isEmpty()) return;

			if (timer(timerDump, dumpTime)) {
				items.each((item, n) -> dump(item));
			}

			timeDrilled += warmup * delta();

			if (items.total() < ores.size * itemCapacity && canConsume()) {
				var speed = Mathf.lerp(1f, liquidBoostIntensity, optionalEfficiency) * efficiency;
				warmup = Mathf.approachDelta(warmup, speed, warmupSpeed);

				for (var ore : ores) {
					oreProgress.increment(ore.key, 0f, delta() * ore.value * speed * warmup);
				}

				if (Mathf.chanceDelta(updateEffectChance * warmup)) {
					updateEffect.at(x + Mathf.range(size * 2f), y + Mathf.range(size * 2f));
				}
			} else {
				warmup = Mathf.lerpDelta(warmup, 0f, warmupSpeed);
				return;
			}

			for (var ore : ores) {
				var delay = drillTime + hardnessDrillMultiplier * ore.key.hardness;
				if (oreProgress.get(ore.key, 0f) >= delay && items.get(ore.key) < itemCapacity) {
					offload(ore.key);
					oreProgress.increment(ore.key, 0f, -delay);
					drillEffect.at(x + Mathf.range(size), y + Mathf.range(size), ore.key.color);
				}
			}
		}

		@Override
		public void draw() {
			float s = 0.3f;
			float ts = 0.6f;

			Draw.rect(region, x, y);
			super.drawCracks();

			Draw.color(heatColor);
			Draw.alpha(warmup * ts * (1f - s + Mathf.absin(Time.time, 3f, s)));
			Draw.blend(Blending.additive);
			Draw.rect(rimRegion, x, y);
			Draw.blend();
			Draw.color();

			Draw.rect(rotatorRegion, x, y, timeDrilled * rotateSpeed);

			Draw.rect(topRegion, x, y);
		}

		@Override
		public void write(Writes write) {
			super.write(write);

			write.f(warmup);
			write.f(timeDrilled);
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);

			warmup = read.f();
			timeDrilled = read.f();
		}
	}
}
