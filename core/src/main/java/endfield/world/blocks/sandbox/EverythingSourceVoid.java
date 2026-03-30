package endfield.world.blocks.sandbox;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;

public class EverythingSourceVoid extends Block {
	public TextureRegion[] center = new TextureRegion[2];

	public EverythingSourceVoid(String name) {
		super(name);

		update = solid = true;
		hasItems = hasLiquids = acceptsItems = true;
		displayFlow = false;
		liquidCapacity = 10000f;
		group = BlockGroup.transportation;
		noUpdateDisabled = true;
		envEnabled = Env.any;
	}

	@Override
	public void load() {
		super.load();

		center[0] = Core.atlas.find(name + "-center-0");
		center[1] = Core.atlas.find(name + "-center-1");
	}

	@Override
	public void setBars() {
		super.setBars();

		removeBar("items");
		removeBar("liquid");
	}

	public class MaterialSourceVoidBuild extends Building {
		@Override
		public void updateTile() {
			for (Item i : Vars.content.items()) {
				items.set(i, 1);
				dump(i);
				items.set(i, 0);
			}

			for (Liquid l : Vars.content.liquids()) {
				liquids.add(l, liquidCapacity);
				dumpLiquid(l);
				liquids.clear();
			}
		}

		@Override
		public boolean acceptItem(Building source, Item item) {
			return enabled;
		}

		@Override
		public void handleItem(Building source, Item item) {
		}

		@Override
		public boolean acceptLiquid(Building source, Liquid liquid) {
			return enabled;
		}

		@Override
		public void handleLiquid(Building source, Liquid liquid, float amount) {
		}
	}
}
