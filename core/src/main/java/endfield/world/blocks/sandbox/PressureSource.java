package endfield.world.blocks.sandbox;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.ImageButton.ImageButtonStyle;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.content.Liquids2;
import endfield.world.blocks.GenericPressureBlock;
import endfield.world.meta.Stats2;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Tex;
import mindustry.type.Category;
import mindustry.type.Liquid;
import mindustry.ui.Styles;
import mindustry.world.blocks.liquid.LiquidBlock;
import mindustry.world.meta.BuildVisibility;
import org.jetbrains.annotations.Nullable;

import static endfield.Vars2.MOD_PREFIX;

public class PressureSource extends GenericPressureBlock {
	public TextureRegion bottomRegion;

	public PressureSource(String name) {
		super(name);
		solid = true;
		destructible = true;
		update = true;
		configurable = true;
		saveConfig = copyConfig = true;
		category = Category.liquid;
		buildVisibility = BuildVisibility.sandboxOnly;

		config(SourceEntry.class, (PressureLiquidSourceBuild build, SourceEntry entry) -> {
			build.liquid = entry.fluid == null ? -1 : entry.fluid.id;
			build.targetAmount = entry.amount;

			for (int i = -1; i < Vars.content.liquids().size; i++) {
				build.pressure.setAmount(i, 0);
				build.pressure.setPressure(i, 0);
			}
		});
	}

	@Override
	public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list) {
		Draw.rect(bottomRegion, plan.drawx(), plan.drawy());
		if (plan.config instanceof SourceEntry e && e.fluid != null)
			LiquidBlock.drawTiledFrames(size, plan.drawx(), plan.drawy(), 0f, e.fluid, 1f);
		Draw.rect(region, plan.drawx(), plan.drawy());
	}

	@Override
	public void load() {
		super.load();

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

		pressureConfig.group = null;
	}

	@Override
	public void setStats() {
		super.setStats();

		stats.remove(Stats2.minPressure);
		stats.remove(Stats2.maxPressure);
	}

	public static class SourceEntry {
		public @Nullable Liquid fluid;
		public float amount;
	}

	public class PressureLiquidSourceBuild extends GenericPressureBlockBuild {
		public int liquid = -1;
		public float targetAmount;

		@Override
		public void buildConfiguration(Table cont) {
			cont.table(Styles.black6, table -> {
				table.pane(Styles.smallPane, liquids -> Vars.content.liquids().each(liquid -> {
					Button button = liquids.button(
							new TextureRegionDrawable(liquid.uiIcon),
							new ImageButtonStyle() {{
								over = Styles.flatOver;
								down = checked = Tex.flatDownBase;
							}}, () -> {
								if (this.liquid != liquid.id) {
									configure(new SourceEntry() {{
										fluid = liquid;
										amount = targetAmount;
									}});
								} else {
									configure(new SourceEntry() {{
										fluid = null;
										amount = targetAmount;
									}});
								}
							}
					).tooltip(liquid.localizedName).size(40f).get();
					button.update(() -> button.setChecked(liquid.id == this.liquid));
					if ((Vars.content.liquids().indexOf(liquid) + 1) % 4 == 0) liquids.row();
				})).maxHeight(160f).row();
				table.add("@filter.option.amount").padTop(5f).padBottom(5f).row();
				table.field(
						"" + targetAmount,
						(field, c) -> Character.isDigit(c) || ((!field.getText().contains(".")) && c == '.') || (field.getText().isEmpty() && c == '-'),
						s -> configure(new SourceEntry() {{
							fluid = Vars.content.liquid(liquid);
							amount = Strings.parseFloat(s, 0f);
						}})
				);
			}).margin(5f);
		}

		@Override
		public SourceEntry config() {
			return new SourceEntry() {{
				fluid = Vars.content.liquid(liquid);
				amount = targetAmount;
			}};
		}

		@Override
		public boolean doPressureDamage() {
			return false;
		}

		@Override
		public void draw() {
			Draw.rect(bottomRegion, x, y);

			if (liquid != -1) {
				LiquidBlock.drawTiledFrames(size, x, y, 0f, Vars.content.liquid(liquid), 1f);
			}

			Draw.rect(region, x, y);
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);

			liquid = read.i();
			if (Vars.content.liquid(liquid) == null) liquid = -1;
			targetAmount = read.f();
		}

		@Override
		public void updateTile() {
			for (int i = -1; i < Vars.content.liquids().size; i++) {
				if (i == liquid) {
					pressure.setAmount(liquid, targetAmount);

					float p = targetAmount /
							pressureConfig.fluidCapacity /
							Liquids2.getDensity(Vars.content.liquid(liquid));

					pressure.setPressure(liquid, p);
				} else {
					pressure.setAmount(i, 0);
					pressure.setPressure(i, 0);
				}
			}
		}

		@Override
		public void write(Writes write) {
			super.write(write);

			write.i(liquid);
			write.f(targetAmount);
		}
	}
}
