package endfield.world.meta;

import arc.struct.Seq;
import endfield.util.handler.FieldHandler;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;
import mindustry.world.meta.StatUnit;

import static endfield.Vars2.MOD_PREFIX;

public final class Stats2 {
	public static final Stat minSpeed = new Stat("min-speed");
	public static final Stat maxSpeed = new Stat("max-speed");
	public static final Stat sentryLifetime = new Stat("sentry-lifetime");
	public static final Stat fuel = new Stat("fuel", StatCat.crafting);
	public static final Stat recipes = new Stat("recipes", StatCat.crafting);
	public static final Stat producer = new Stat("producer", StatCat.crafting);
	public static final Stat produce = new Stat("produce", StatCat.crafting);
	public static final Stat baseHealChance = new Stat("base-heal-chance");
	public static final Stat itemsMovedBoost = new Stat("items-moved-boost", StatCat.optional);
	public static final Stat powerConsModifier = new Stat("power-cons-modifier", StatCat.function);
	public static final Stat minerBoosModifier = new Stat("miner-boost-modifier", StatCat.function);
	public static final Stat itemConvertList = new Stat("item-convert-list", StatCat.function);
	public static final Stat maxBoostPercent = new Stat("max-boost-percent", StatCat.function);
	public static final Stat maxModules = new Stat("max-modules", StatCat.function);
	public static final Stat damageReduction = new Stat("damage-reduction", StatCat.general);
	public static final Stat fieldStrength = new Stat("field-strength", StatCat.function);
	public static final Stat albedo = new Stat("albedo", StatCat.function);
	public static final Stat contents = new Stat("contents");
	public static final Stat healPercent = new Stat("heal-percent", StatCat.general);
	public static final Stat produceChance = new Stat("produce-chance", StatCat.crafting);
	//public static final Stat maxStructureSize = new Stat("max-structure-size");

	public static StatUnit blocksCubed = new StatUnit("blocks-cubed");
	public static StatUnit densityUnit = new StatUnit("density-unit");
	public static StatUnit viscosityUnit = new StatUnit("viscosity-unit");
	public static StatUnit pressureUnit = new StatUnit("pressure-unit", MOD_PREFIX + "pressure-icon");

	public static StatUnit percentPerSecond = new StatUnit("percent-per-second");

	public static StatCat pressure = new StatCat("pressure");

	public static Stat space = new Stat("space");

	public static Stat debris = new Stat("debris");

	public static Stat density = new Stat("density");

	public static Stat minPressure = new Stat("min-pressure", pressure);
	public static Stat maxPressure = new Stat("max-pressure", pressure);

	public static Stat pumpStrength = new Stat("pump-strength");
	public static Stat pressureGradient = new Stat("pressure-gradient");

	/** Don't let anyone instantiate this class. */
	private Stats2() {}

	public static Stat insert(String name, int index, StatCat cat) {
		Seq<Stat> all = Stat.all;
		Stat res = new Stat(name, cat);

		all.remove(res);
		all.insert(index, res);

		for (int i = 0; i < all.size; i++) {
			FieldHandler.setIntDefault(all.get(i), "id", i);
		}

		return res;
	}
}
