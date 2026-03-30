package endfield.world.blocks.sandbox;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;

public class EverythingLiquidSource extends Block {
	public TextureRegion centerRegion;

	public EverythingLiquidSource(String name) {
		super(name);

		update = true;
		solid = true;
		hasLiquids = true;
		liquidCapacity = 100f;
		outputsLiquid = true;
		noUpdateDisabled = true;
		displayFlow = false;
		group = BlockGroup.liquids;
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

		removeBar("liquid");
	}

	public class EverythingLiquidSourceBuild extends Building {
		@Override
		public void updateTile() {
			for (Liquid l : Vars.content.liquids()) {
				liquids.add(l, liquidCapacity);
				dumpLiquid(l);
				liquids.clear();
			}
		}
	}
}
