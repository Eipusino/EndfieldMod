package endfield.world.blocks.environment;

import arc.struct.Seq;
import arc.util.Log;
import endfield.util.atomic.AtomicBoolean;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.Tile;

public class MultiPropGroup {
	public Seq<Tile> group = new Seq<>(Tile.class);
	public Tile center;
	public int shape = 0;
	public Block type;
	public MultiPropI propType;
	public boolean removed = false;

	public MultiPropGroup(Block type) {
		this.type = type;
		if (!(type instanceof MultiPropI prop)) throw new IllegalArgumentException("that's not a multiprop ya dummy");
		propType = prop;
	}

	public void findCenter() {
		center = group.max(Tile::pos);
		if (center == null) Log.errTag("what", "HUH?");
	}

	public void findShape() {
		shape = propType.shapes().indexOf(shape -> {
			AtomicBoolean find = new AtomicBoolean(true);

			shape.eachRelativeCenter((x, y) -> {
				switch (shape.getIdRelativeCenter(x, y)) {
					case 2 -> {
						if (!group.contains(center.nearby(x, y))) find.set(false);
					}
					case 3 -> {
					}
					default -> {
						if (group.contains(center.nearby(x, y))) find.set(false);
					}
				}
			});
			return find.get();
		});
		if (shape == -1) shape = 0;
	}

	public void remove() {
		group.each(tile -> tile.setBlock(Blocks.air));
		propType.removed(this).run();
		removed = true;
	}

	public void update() {
		if (group.contains(tile -> tile.block() != type)) remove();
	}
}
