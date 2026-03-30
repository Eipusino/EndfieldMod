package endfield.world.blocks.sandbox;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;

public class EverythingItemSource extends Block {
	public TextureRegion centerRegion;

	public EverythingItemSource(String name) {
		super(name);

		hasItems = true;
		update = true;
		solid = true;
		group = BlockGroup.transportation;
		noUpdateDisabled = true;
		envEnabled = Env.any;
	}

	@Override
	public void load() {
		super.load();

		centerRegion = Core.atlas.find(name + "-center", "center");
	}

	@Override
	public void setBars() {
		super.setBars();

		removeBar("items");
	}

	public class EverythingItemSourceBuild extends Building {
		@Override
		public void updateTile() {
			for (Item i : Vars.content.items()) {
				items.set(i, 1);
				dump(i);
				items.set(i, 0);
			}
		}

		@Override
		public boolean acceptItem(Building source, Item item) {
			return false;
		}
	}
}
