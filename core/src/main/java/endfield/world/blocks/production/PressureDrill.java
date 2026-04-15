package endfield.world.blocks.production;

import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.world.blocks.HasPressure;
import endfield.world.blocks.PressureBlock;
import endfield.world.graph.PressureGraph;
import endfield.world.meta.PressureConfig;
import endfield.world.meta.PressureTank.TankGroup;
import endfield.world.modules.PressureModule;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.production.Drill;
import mindustry.world.consumers.Consume;

public class PressureDrill extends Drill implements PressureBlock {
	public PressureConfig pressureConfig = new PressureConfig();

	@Override
	public PressureConfig pressureConfig() {
		return pressureConfig;
	}

	public boolean useConsumerMultiplier = true;

	public PressureDrill(String name) {
		super(name);
	}

	@Override
	public void init() {
		if (hasLiquids) {
			hasLiquids = false;
			pressureConfig.hasPressure = true;
		}

		super.init();

		if (hasLiquids) hasLiquids = false;

		if (pressureConfig.group == null) pressureConfig.group = TankGroup.drills;
	}

	@Override
	public void setBars() {
		super.setBars();
		pressureConfig.addBars(this);
	}

	@Override
	public void setStats() {
		super.setStats();
		pressureConfig.addStats(this, stats);
	}

	@Override
	public int size() {
		return size;
	}

	public class PressureDrillBuild extends DrillBuild implements HasPressure {
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

		public float efficiencyMultiplier() {
			float val = 1;
			if (!useConsumerMultiplier) return val;
			for (Consume consumer : consumers) {
				val *= consumer.efficiencyMultiplier(this);
			}
			return val;
		}

		@Override
		public float efficiencyScale() {
			return super.efficiencyScale() * efficiencyMultiplier();
		}

		@Override
		public float getProgressIncrease(float baseTime) {
			return super.getProgressIncrease(baseTime) * efficiencyMultiplier();
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
