package endfield.world.blocks.production;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.world.blocks.HasPressure;
import endfield.world.blocks.PressureBlock;
import endfield.world.graph.PressureGraph;
import endfield.world.meta.PressureConfig;
import endfield.world.meta.StatValues2;
import endfield.world.modules.PressureModule;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.Consume;
import mindustry.world.meta.Stat;

public class PressureCrafter extends GenericCrafter implements PressureBlock {
	public PressureConfig pressureConfig = new PressureConfig();

	@Override
	public PressureConfig pressureConfig() {
		return pressureConfig;
	}

	public boolean useConsumerMultiplier = true;

	public float outputAir;

	public PressureCrafter(String name) {
		super(name);
	}

	@Override
	public void init() {
		super.init();

		if (hasLiquids) {
			hasLiquids = false;
			pressureConfig.hasPressure = true;
		}
	}

	@Override
	public void setBars() {
		super.setBars();
		pressureConfig.addBars(this);

		if (outputLiquids != null && outputLiquids.length > 0) {
			removeBar("fluid-bar");

			for (var stack : outputLiquids) {
				addBar("fluid-bar-" + stack.liquid.name, build -> {
					HasPressure e = (HasPressure) build;
					Liquid liq = stack.liquid;
					return new Bar(
							() -> liq == null ?
									Core.bundle.format("bar.air-bar", StatValues2.formatValue(e.getFluid(liq), 2, false)) :
									Core.bundle.format("bar.fluid-bar", liq.localizedName, StatValues2.formatValue(e.getFluid(liq), 2, false), StatValues2.formatValue(e.getFluid(null), 2, false)),
							() -> liq == null ? Color.white : liq.color,
							() -> liq == null ? 0f : e.getFluid(liq) / Math.max(1f, Math.abs(e.getFluid(null)))
					);
				});
			}

			if (outputAir > 0) {
				addBar("fluid-bar-air", build -> {
					HasPressure e = (HasPressure) build;
					Liquid liq = null;
					return new Bar(
							() -> Core.bundle.format("bar.air-bar", StatValues2.formatValue(e.getFluid(liq), 2, false)),
							() -> Color.white,
							() -> 0f
					);
				});
			}
		}
	}

	@Override
	public void setStats() {
		super.setStats();
		pressureConfig.addStats(this, stats);

		if (outputAir > 0) {
			stats.add(Stat.output, StatValues2.fluid(null, outputAir, 1f, true));
		}
	}

	@Override
	public int size() {
		return size;
	}

	public class PressureCrafterBuild extends GenericCrafterBuild implements HasPressure {
		public PressureModule pressure;

		@Override
		public Building create(Block block, Team team) {
			super.create(block, team);
			if (pressureConfig().hasPressure) {
				pressure = new PressureModule();
				pressureGraph().addRaw(this);
			}
			return this;
		}

		@Override
		public void dumpOutputs() {
			if (outputItems != null && timer(timerDump, dumpTime / timeScale)) {
				for (ItemStack output : outputItems) {
					dump(output.item);
				}
			}
		}

		public float efficiencyMultiplier() {
			float value = 1;
			if (!useConsumerMultiplier) return value;
			for (Consume consumer : consumers) {
				value *= consumer.efficiencyMultiplier(this);
			}
			return value;
		}

		@Override
		public float efficiencyScale() {
			return super.efficiencyScale() * efficiencyMultiplier();
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();
			if (pressureConfig.hasPressure) {
				new PressureGraph().floodMergeGraph(this);
			}
		}

		@Override
		public PressureModule pressure() {
			return pressure;
		}

		@Override
		public PressureConfig pressureConfig() {
			return pressureConfig;
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			if (pressureConfig.hasPressure) {
				(pressure == null ? new PressureModule() : pressure).read(read);
			}
		}

		@Override
		public boolean shouldConsume() {
			if (outputItems != null) {
				for (var output : outputItems) {
					if (items.get(output.item) + output.amount > itemCapacity) {
						return false;
					}
				}
			}

			if (outputLiquids != null && !ignoreLiquidFullness) {
				boolean allFull = true;
				boolean someFull = false;

				if (getFluid(null) >= pressureConfig.fluidCapacity) {
					someFull = true;
				} else {
					allFull = false;
				}

				for (LiquidStack output : outputLiquids) {
					if (getFluid(output.liquid) >= pressureConfig.fluidCapacity) {
						someFull = true;
					} else {
						allFull = false;
					}
				}

				if (allFull || (someFull && !ignoreLiquidFullness)) return false;
			}
			return enabled;
		}

		@Override
		public void updateTile() {
			if (efficiency > 0) {
				progress += getProgressIncrease(craftTime);
				warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);

				//continuously output based on efficiency, uncapped
				float inc = getProgressIncrease(1f);
				if (outputLiquids != null) {
					for (var output : outputLiquids) addFluid(output.liquid, output.amount * inc);
				}
				if (outputAir > 0) addFluid(null, outputAir * inc);

				if (wasVisible && Mathf.chanceDelta(updateEffectChance)) {
					updateEffect.at(x + Mathf.range(size * updateEffectSpread), y + Mathf.range(size * updateEffectSpread));
				}
			} else {
				warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
			}

			totalProgress += warmup * edelta();

			if (progress >= 1f) {
				craft();
			}
			dumpOutputs();
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			if (pressureConfig.hasPressure) {
				pressure.write(write);
			}
		}

		@Override
		public Block block() {
			return block;
		}

		@Override
		public Tile tile() {
			return tile;
		}

		@Override
		public float efficiency() {
			return efficiency;
		}

		@Override
		public Seq<Building> proximity() {
			return proximity;
		}
	}
}
