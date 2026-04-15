package endfield.world.meta;

import endfield.content.Liquids2;
import endfield.util.CollectionList;
import endfield.world.blocks.HasPressure;
import mindustry.Vars;
import mindustry.type.Liquid;
import org.jetbrains.annotations.Nullable;

public class PressureTank {
	public CollectionList<HasPressure> builds = new CollectionList<>(HasPressure.class);

	/**
	 * Adds a certain amount of fluid distributed over the whole tank.
	 */
	public void addFluid(@Nullable Liquid fluid, float amount) {
		if (!Float.isFinite(amount) || amount == 0f || builds.isEmpty()) return;
		if (amount < 0f) {
			removeFluid(fluid, -amount);
			return;
		}
		float div = amount / builds.size;
		int id = fluid == null ? -1 : fluid.id;

		for (HasPressure build : builds) {
			build.pressure().setAmount(id, build.pressure().getAmount(id) + div);

			float pressure =
					build.pressure().getAmount(id) /
							build.pressureConfig().fluidCapacity /
							Liquids2.getDensity(fluid);
			build.pressure().setPressure(id, pressure);
		}
	}

	/**
	 * Evens out the amount of fluid. Unlike normal flow based on pressure, all builds of the same tank must have an equal amount of each fluid.
	 */
	public void equalize() {
		if (builds.size <= 1) return;
		for (int i = -1; i < Vars.content.liquids().size; i++) {
			float sum = 0;
			for (HasPressure build : builds) sum += build.pressure().getAmount(i);
			sum /= builds.size;

			for (HasPressure build : builds) {
				build.pressure().setAmount(i, sum);
				build.pressure().setPressure(i, sum / build.pressureConfig().fluidCapacity / Liquids2.getDensity(Vars.content.liquid(i)));
			}
		}
	}

	/**
	 * Removes a certain amount of fluid distributed over the whole tank.
	 */
	public void removeFluid(@Nullable Liquid fluid, float amount) {
		if (!Float.isFinite(amount) || amount == 0f || builds.isEmpty()) return;
		if (amount < 0f) {
			addFluid(fluid, -amount);
			return;
		}
		float div = amount / builds.size;
		int id = fluid == null ? -1 : fluid.id;

		for (HasPressure build : builds) {
			build.pressure().setAmount(id, build.pressure().getAmount(id) - div);

			float pressure =
					build.pressure().getAmount(id) /
							build.pressureConfig().fluidCapacity /
							Liquids2.getDensity(fluid);
			build.pressure().setPressure(id, pressure);
		}
	}

	public enum TankGroup {
		drills,
		pumps,
		production,
		transportation
	}
}
