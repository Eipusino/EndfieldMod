package endfield.type;

import arc.math.Mathf;
import arc.util.Time;
import endfield.world.blocks.HasPressure;
import mindustry.content.Liquids;
import mindustry.entities.Puddles;
import mindustry.type.CellLiquid;
import mindustry.type.Liquid;

public class NeoplasmFluidInteraction extends FluidInteraction {
	@Override
	public boolean canInteract(Liquid liquid1, Liquid liquid2) {
		return
				(liquid1 == Liquids.water && liquid2 == Liquids.neoplasm) ||
						(liquid2 == Liquids.water && liquid1 == Liquids.neoplasm);
	}

	@Override
	public void interaction(HasPressure build) {
		float remove = Math.min(0.7f * Time.delta, build.getFluid(Liquids.water));

		build.removeFluid(Liquids.water, remove);
		build.addFluid(Liquids.neoplasm, remove);
		build.damageContinuous(((CellLiquid) Liquids.neoplasm).spreadDamage);
		Puddles.deposit(build.tile(), Liquids.neoplasm, remove * ((CellLiquid) Liquids.neoplasm).removeScaling);
	}

	@Override
	public boolean shouldInteract(HasPressure build) {
		return !Mathf.zero(build.getFluid(Liquids.neoplasm), 0.001f) && !Mathf.zero(build.getFluid(Liquids.water), 0.001f);
	}
}
