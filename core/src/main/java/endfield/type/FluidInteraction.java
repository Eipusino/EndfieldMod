package endfield.type;

import endfield.util.CollectionList;
import endfield.world.blocks.HasPressure;
import mindustry.type.Liquid;

public abstract class FluidInteraction {
	public static final CollectionList<FluidInteraction> interactions = new CollectionList<>(FluidInteraction.class);

	public FluidInteraction() {
		interactions.add(this);
	}

	public abstract boolean canInteract(Liquid liquid1, Liquid liquid2);

	/**
	 * Method containing the interaction that will happen if {@link #shouldInteract} returns true.
	 */
	public abstract void interaction(HasPressure build);

	/**
	 * @return true when the specified building has this interaction possible.
	 */
	public abstract boolean shouldInteract(HasPressure build);
}
