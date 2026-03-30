package endfield.world.blocks.power;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;
import mindustry.Vars;
import mindustry.graphics.Pal;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.meta.StatUnit;

public class WindTurbine extends PowerGenerator {
	public TextureRegion fanRegion;
	public TextureRegion topRegion;

	public Point2[] prox;

	public WindTurbine(String name) {
		super(name);
	}

	@Override
	public void load() {
		super.load();

		fanRegion = Core.atlas.find(name + "-fan");
		topRegion = Core.atlas.find(name + "-top");
	}

	@Override
	public void init() {
		super.init();

		int bot = (int) (-((size - 1) / 2f)) - 1;
		int top = (int) ((size - 1) / 2f + 0.5f) + 1;

		prox = new Point2[]{new Point2(bot, bot), new Point2(bot, top), new Point2(top, top), new Point2(top, bot),
				new Point2(bot - 1, bot - 1), new Point2(bot - 1, top + 1), new Point2(top + 1, top + 1), new Point2(top + 1, bot - 1)};
	}

	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid) {
		Draw.color(Pal.placing);
		Lines.stroke(size);
		Lines.square(x * Vars.tilesize + offset, y * Vars.tilesize + offset, (Vars.tilesize / 2f) * (size + 4f));
	}

	@Override
	public void setStats() {
		super.setStats();
		stats.remove(generationType);
		stats.add(generationType, powerProduction * 60f, StatUnit.powerSecond);
	}

	@Override
	public TextureRegion[] icons() {
		return new TextureRegion[]{region, fanRegion, topRegion};
	}

	public class WindTurbineBuild extends GeneratorBuild {
		public float time = 0f;
		public float count = 0;

		@Override
		public void updateTile() {
			count = (count + 1) % 60; // update only once every second
			if (count == 1) {
				Point2[] edges = Edges.getEdges(size);
				Point2[] edges2 = Edges.getEdges(size + 2);
				float base = edges.length + edges2.length + prox.length;
				int occupied = occupied(edges) +  occupied(edges2) + occupied(prox);
				productionEfficiency = enabled ? 1f - occupied / base : 0f;
			}
			time += delta() * productionEfficiency;
		}

		public int occupied(Point2[] edges) {
			int occupied = 0;
			for (Point2 edge : edges) {
				Tile t = Vars.world.tile(tile.x + edge.x, tile.y + edge.y);
				if (t != null && t.solid()) occupied++;
			}
			return occupied;
		}

		@Override
		public void drawCracks() {}

		@Override
		public void draw() {
			Draw.rect(region, x, y);
			super.drawCracks();
			Draw.rect(fanRegion, x, y, time);
			Draw.rect(topRegion, x, y);
		}

		@Override
		public void drawSelect() {
			drawPlace(tileX(), tileY(), 0, true);
		}
	}
}
