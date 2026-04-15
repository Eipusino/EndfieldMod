package endfield.world.meta;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.math.Mathf;
import endfield.ui.CenterBar;
import endfield.util.Constant;
import endfield.world.blocks.HasPressure;
import endfield.world.consumers.ConsumeFluid;
import endfield.world.meta.PressureTank.TankGroup;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;

public class PressureConfig {
	/**
	 * Whether the block supports pressurized fluids.
	 *
	 * @apiNote when false, the resulting building will not have a PressureModule
	 */
	public boolean hasPressure = false;

	/**
	 * Whether the block accepts or outputs pressure
	 *
	 * @apiNote Should not define static connections, as most blocks do not distinguish accepting fluids or outputting fluids.
	 */
	public boolean acceptsPressure, outputsPressure;

	/**
	 * Whether the fluids inside this block react to one another.
	 */
	public boolean fluidReacts;

	/**
	 * Internal fluid capacity of the block. Does not define how much fluid it can contain. But is used instead to determine pressure.
	 */
	public float fluidCapacity = 8f;

	/**
	 * Minimum or maximum pressure of this block. Going beyond this will cause the building to be damaged.
	 */
	public float minPressure = -50f, maxPressure = 50;

	/**
	 * Damage dealt to certain buildings if pressure is over maxPressure or under minPressure.
	 */
	public float underPressureDamage = 0.1f, overPressureDamage = 0.1f;

	/**
	 * Group of fluid section. Connected buildings with the same group will act as one singular tank.
	 *
	 * @apiNote A null group will not create tanks with nearby buildings.
	 */
	public TankGroup group;

	/**
	 * An extra filter that allows/denies connections based on block types.
	 * should return true if the block type can connect.
	 */
	public Boolf<Block> blockFilter = Constant.boolf(true);

	public void addBars(Block block) {
		if (!hasPressure) return;
		block.removeBar("liquid");

		boolean added = false;

		// add bars for each consumed fluid
		for (Consume cons : block.consumers) {
			if (cons instanceof ConsumeFluid consFluid && block.consumers.length > 1) {
				String barName = "fluid-bar-" + (consFluid.fluid == null ? "air" : consFluid.fluid);

				block.addBar(barName, build -> {
					HasPressure pressure = (HasPressure) build;
					return new Bar(() -> {
						Liquid main = pressure.pressure().getMain();

						return main == null ?
								Core.bundle.format("bar.air-bar", StatValues2.formatValue(pressure.getFluid(null), 2, false)) :
								Core.bundle.format("bar.fluid-bar", main.localizedName, StatValues2.formatValue(pressure.getFluid(main), 2, false), StatValues2.formatValue(pressure.getFluid(null), 2, false));
					}, () -> {
						Liquid main = pressure.pressure().getMain();

						return main == null ? Color.white : main.color;
					}, () -> {
						Liquid main = pressure.pressure().getMain();

						return main == null ? 0f : Mathf.clamp(pressure.getFluid(main));
					});
				});

				added = true;
			}
		}

		// default to generic bar if there's only one liquid consumed
		if (!added) {
			block.addBar("fluid-bar", build -> {
				HasPressure pressure = (HasPressure) build;
				return new Bar(
						() -> {
							Liquid main = pressure.pressure().getMain();

							return main == null ?
									Core.bundle.format("bar.air-bar", StatValues2.formatValue(pressure.getFluid(null), 2, false)) :
									Core.bundle.format("bar.fluid-bar", main.localizedName, StatValues2.formatValue(pressure.getFluid(main), 2, false), StatValues2.formatValue(pressure.getFluid(null), 2, false));
						},
						() -> {
							Liquid main = pressure.pressure().getMain();

							return main == null ? Color.white : main.color;
						},
						() -> {
							Liquid main = pressure.pressure().getMain();

							return Mathf.clamp(main == null ? 0f : pressure.getFluid(main) / Math.max(1f, pressure.getFluid(main) + Math.abs(pressure.getFluid(null))));
						}
				);
			});
		} else {
			block.addBar("fluid-bar-air", build -> {
				HasPressure pressure = (HasPressure) build;
				return new Bar(
						() -> Core.bundle.format("bar.air-bar", StatValues2.formatValue(pressure.getFluid(pressure.pressure().getMain()), 2, false)),
						() -> Color.white,
						() -> 0f
				);
			});
		}

		block.addBar("pressure-bar", build -> {
			HasPressure pressure = (HasPressure) build;
			return new CenterBar(
					() -> Core.bundle.format("bar.pressure-bar", StatValues2.formatValue(pressure.pressure().sumPressure(), 2, false)),
					() -> pressure.pressure().sumPressure() > 0 ? Color.white : Color.gray,
					() -> Mathf.map(pressure.pressure().sumPressure(), minPressure, maxPressure, -1f, 1f)
			);
		});
	}

	public void addStats(Block block, Stats stats) {
		if (!hasPressure) return;
		stats.remove(Stat.liquidCapacity);
		stats.add(Stat.liquidCapacity, fluidCapacity / 8f, Stats2.blocksCubed);

		stats.add(Stats2.minPressure, StatValues2.number(minPressure, Stats2.pressureUnit, false));
		stats.add(Stats2.maxPressure, StatValues2.number(maxPressure, Stats2.pressureUnit, false));
	}
}
