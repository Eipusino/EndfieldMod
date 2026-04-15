package endfield.world.blocks.environment;

import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Bits;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.async.AsyncProcess;
import mindustry.world.Block;
import mindustry.world.Tile;

public class CustomShapePropProcess implements AsyncProcess {
	public static CustomShapePropProcess instance;
	private final Bits visitedTilePos = new Bits();
	private final Seq<Tile> tempNonThreadSafeSeq = new Seq<>(Tile.class);
	public Seq<MultiPropGroup> multiProps = new Seq<>(MultiPropGroup.class);

	public static void create() {
		Vars.asyncCore.processes.add(instance = new CustomShapePropProcess());
	}

	@Override
	public void init() {
		multiProps.clear();
		visitedTilePos.clear();
		for (Tile tile : Vars.world.tiles) {
			Block block = tile.block();
			if (!(block instanceof MultiPropI) || visitedTilePos.get(tile.pos())) continue;
			MultiPropGroup multiProp = createMultiProp(tile);
			multiProps.add(multiProp);
			multiProp.findCenter();
			multiProp.findShape();
		}
	}

	public MultiPropGroup createMultiProp(Tile from) {
		Seq<Tile> temp = tempNonThreadSafeSeq.clear().add(from);
		MultiPropGroup out = new MultiPropGroup(from.block());
		Seq<Tile> group = out.group;

		group.add(from);
		visitedTilePos.set(from.pos());

		while (!temp.isEmpty()) {
			Tile tile = temp.pop();
			for (Point2 point : Geometry.d4) {
				Tile nearby = tile.nearby(point);
				if (nearby == null) continue;
				int pos = nearby.pos();
				if (nearby.block() instanceof MultiPropI && !visitedTilePos.get(pos) && nearby.block() == out.type) {
					group.add(nearby);
					visitedTilePos.set(pos);
					temp.add(nearby);
				}
			}
		}
		temp.clear();
		return out;
	}

	@Override
	public void process() {
		for (int i = 0; i < multiProps.size; i++) {
			MultiPropGroup multiProp = multiProps.get(i);
			multiProp.update();
			if (!multiProp.removed) continue;

			multiProps.remove(i);
			i--;
		}

	}

	public void onRemoveBlock(Tile tile, Block block) {
		multiProps.each(multiPropGroup -> {
			if (multiPropGroup.group.contains(tile)) {
				multiPropGroup.remove();
			}
		});
	}
}
