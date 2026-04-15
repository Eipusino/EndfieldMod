package endfield.world.graph;

import arc.struct.FloatSeq;
import arc.struct.Seq;
import arc.util.Time;
import endfield.content.Liquids2;
import endfield.gen.PressureGraphUpdater;
import endfield.math.Physics;
import endfield.util.CollectionList;
import endfield.util.ObjectIntMap2;
import endfield.util.holder.ObjectHolder;
import endfield.world.blocks.HasPressure;
import endfield.world.meta.PressureTank;
import mindustry.Vars;
import mindustry.type.Liquid;

/**
 * @author Liz
 */
public class PressureGraph {
	static CollectionList<HasPressure> tmp = new CollectionList<>(HasPressure.class), tmp2 = new CollectionList<>(HasPressure.class), tmp3 = new CollectionList<>(HasPressure.class);

	static CollectionList<ObjectHolder<HasPressure, HasPressure>> edges = new CollectionList<>(ObjectHolder.class);
	static ObjectIntMap2<HasPressure> connections = new ObjectIntMap2<>(HasPressure.class);
	static FloatSeq flows = new FloatSeq(Vars.content.liquids().size + 1);

	public CollectionList<HasPressure> builds = new CollectionList<>(false, HasPressure.class);

	public boolean changed;

	public PressureGraphUpdater updater = new PressureGraphUpdater().create(this);

	public void addRaw(HasPressure build) {
		builds.addUnique(build);
		build.pressure().graph = this;
		checkEntity();
		changed = true;
	}

	public void checkEntity() {
		if (builds.isEmpty()) {
			updater.remove();
		} else {
			updater.add();
		}
	}

	public void floodMergeGraph(HasPressure start) {
		tmp.clear();
		tmp.add(start);
		tmp2.clear();
		while (!tmp.isEmpty()) {
			HasPressure current = tmp.pop();
			tmp2.add(current);

			if (current.pressureGraph() != this) {
				current.pressureGraph().removeRaw(current);
				addRaw(current);
			}

			for (HasPressure next : current.connections()) {
				if (!tmp2.contains(next)) {
					tmp.add(next);
					tmp2.add(next);
				}
			}
		}
	}

	public void rebuildTanks() {
		tmp.clear();
		tmp.add(builds.first());
		tmp2.clear();
		tmp3.clear();

		PressureTank section;
		while (!tmp.isEmpty()) {
			section = new PressureTank();
			tmp2.add(tmp.pop());
			while (!tmp2.isEmpty()) {
				HasPressure current = tmp2.pop();

				section.builds.addUnique(current);
				current.pressure().section = section;

				for (HasPressure other : current.connections()) {
					if (!tmp3.contains(other)) {
						if (other.pressureConfig().group != current.pressureConfig().group || other.pressureConfig().group == null) {
							tmp.add(other);
						} else {
							tmp2.add(other);
						}
						tmp3.add(other);
					}
				}
			}
			section.equalize();
		}
	}

	public void removeRaw(HasPressure build) {
		builds.remove(build);
		checkEntity();
		changed = true;
	}

	public void transferFluids() {
		edges.clear();
		connections.clear();

		builds.each(build -> {
			Seq<HasPressure> others = build.connections().retainAll(other -> other.pressureSection() != build.pressureSection());
			connections.put(build, Math.max(1, others.size));
			others.each(other -> edges.add(new ObjectHolder<>() {{
				key = build;
				value = other;
			}}));
		});

		for (int i = 0; i < Vars.content.liquids().size + 1; i++) {
			flows.clear();
			int liquidID = i - 1;
			Liquid liquid = Vars.content.liquid(liquidID);

			edges.each(e -> {
				HasPressure from = e.key;
				HasPressure to = e.value;

				flows.add(Physics.fluidFlow(
						from.pressure().getPressure(liquidID),
						from.pressureConfig().fluidCapacity,
						to.pressure().getPressure(liquidID),
						to.pressureConfig().fluidCapacity,
						Liquids2.getDensity(liquid),
						Liquids2.getViscosity(liquid),
						Time.delta
				) / connections.get(to) / 2f);
			});

			int edgeIndex = 0;
			for (ObjectHolder<HasPressure, HasPressure> currentEdge : edges) {
				float flow = flows.get(edgeIndex);
				if (HasPressure.canTransfer(currentEdge.key, currentEdge.value, liquid, flow)) {
					currentEdge.key.pressureSection().removeFluid(liquid, flow);
					currentEdge.value.pressureSection().addFluid(liquid, flow);
				}
				edgeIndex++;
			}
		}
	}

	public void update() {
		if (changed) {
			rebuildTanks();

			builds.each(HasPressure::onPressureGraphUpdate);
			changed = false;
		}

		transferFluids();

//        checkDamage();

		builds.each(HasPressure::updateFluids);
	}
}
