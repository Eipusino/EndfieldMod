package endfield.world.blocks.liquid;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.world.blocks.GenericPressureBlock;
import endfield.world.blocks.HasPressure;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.type.Liquid;
import mindustry.world.blocks.ItemSelection;

public class PressureLiquidOutlet extends GenericPressureBlock {
	public TextureRegion liquidRegion;

	public PressureLiquidOutlet(String name) {
		super(name);
		configurable = true;
		clearOnDoubleTap = true;
		destructible = true;
		saveConfig = true;

		config(Liquid.class, (PressureLiquidOutletBuild build, Liquid liquid) -> build.currentLiquid = liquid);
		configClear((PressureLiquidOutletBuild build) -> build.currentLiquid = null);
	}

	@Override
	public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list) {
		if (plan.config instanceof Liquid liq) {
			Draw.color(liq.color);
			Draw.rect(liquidRegion, plan.drawx(), plan.drawy());
			Draw.color();
		}
	}

	@Override
	public void load() {
		super.load();

		liquidRegion = Core.atlas.find(name + "-liquid");
	}

	@Override
	public void init() {
		pressureConfig.hasPressure = pressureConfig.acceptsPressure = pressureConfig.outputsPressure = true;

		super.init();

		pressureConfig.group = null;
	}

	public class PressureLiquidOutletBuild extends GenericPressureBlockBuild {
		public Liquid currentLiquid = null;

		@Override
		public boolean acceptsFluid(HasPressure from, Liquid fluid, float amount) {
			return super.acceptsFluid(from, fluid, amount) && fluid == currentLiquid;
		}

		@Override
		public void buildConfiguration(Table table) {
			ItemSelection.buildTable(
					table,
					Vars.content.liquids(),
					() -> currentLiquid,
					this::configure
			);
		}

		@Override
		public Object config() {
			return currentLiquid;
		}

		@Override
		public void draw() {
			super.draw();

			if (currentLiquid != null) {
				Draw.color(currentLiquid.color);
				Draw.rect(liquidRegion, x, y);
				Draw.color();
			}
		}

		@Override
		public void drawSelect() {
			super.drawSelect();
			drawItemSelection(currentLiquid);
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);

			currentLiquid = Vars.content.liquid(read.i());
		}

		@Override
		public void write(Writes write) {
			super.write(write);

			write.i(currentLiquid == null ? -1 : currentLiquid.id);
		}
	}
}
