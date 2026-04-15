package endfield.type;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import endfield.world.blocks.HasPressure;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.type.Liquid;

public class BoilFluidInteraction extends FluidInteraction {
	@Override
	public boolean canInteract(Liquid liquid1, Liquid liquid2) {
		return liquid1.blockReactive && liquid2.blockReactive &&
				(
						(liquid1.temperature < 0.55f && liquid2.temperature > 0.7f) ||
								(liquid2.temperature < 0.55f && liquid1.temperature > 0.7f)
				);
	}

	@Override
	public void interaction(HasPressure build) {
		Seq<Liquid> lowTemp = Vars.content.liquids().select(l -> !Mathf.zero(build.getFluid(l), 0.001f) && l.blockReactive && l.temperature < 0.55f);
		Seq<Liquid> highTemp = Vars.content.liquids().select(l -> !Mathf.zero(build.getFluid(l), 0.001f) && l.blockReactive && l.temperature > 0.7f);

		float remove = Math.min(0.7f * Time.delta, Math.min(build.getFluid(lowTemp.first()), build.getFluid(highTemp.first())));

		build.removeFluid(lowTemp.first(), remove);
		build.removeFluid(highTemp.first(), remove);
		// TODO make something special since pipes are completely sealed?
		if (Mathf.chance(0.2))
			Fx.steam.at(build.x() + Mathf.range(4f), build.y() + Mathf.range(4f));
	}

	@Override
	public boolean shouldInteract(HasPressure build) {
		Seq<Liquid> lowTemp = Vars.content.liquids().select(l -> !Mathf.zero(build.getFluid(l), 0.001f) && l.blockReactive && l.temperature < 0.55f);
		Seq<Liquid> highTemp = Vars.content.liquids().select(l -> !Mathf.zero(build.getFluid(l), 0.001f) && l.blockReactive && l.temperature > 0.7f);
		return !lowTemp.isEmpty() && !highTemp.isEmpty();
	}
}
