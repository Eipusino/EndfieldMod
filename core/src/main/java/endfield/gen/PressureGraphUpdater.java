package endfield.gen;

import arc.util.io.Reads;
import arc.util.io.Writes;
import endfield.world.blocks.IBuilding;
import endfield.world.graph.PressureGraph;
import mindustry.entities.EntityGroup;
import mindustry.gen.Entityc;
import mindustry.gen.Groups;

import static endfield.util.Objects2.let;

public class PressureGraphUpdater implements Entityc {
	public transient boolean added;
	public transient int id = EntityGroup.nextId();
	public transient PressureGraph graph;

	public PressureGraphUpdater create(PressureGraph pressureGraph) {
		graph = pressureGraph;
		return this;
	}

	@Override
	public void update() {
		if (graph != null && !let(graph.builds, builds -> builds.retainAll(IBuilding::isValid)).isEmpty()) {
			graph.update();
		} else {
			remove();
		}
	}

	@Override
	public boolean isAdded() {
		return added;
	}

	@Override
	public void remove() {
		if (!added) return;

		Groups.all.remove(this);

		added = false;
	}

	@Override
	public void add() {
		if (added) return;

		Groups.all.add(this);

		added = true;
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public boolean isRemote() {
		return false;
	}

	@Override
	public int classId() {
		return Entitys.getId(PressureGraphUpdater.class);
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public void id(int value) {
		id = value;
	}

	@Override
	public boolean serialize() {
		return false;
	}

	@Override
	public void read(Reads read) {
		afterRead();
	}

	@Override
	public void write(Writes write) {

	}

	@Override
	public void beforeWrite() {

	}

	@Override
	public void afterRead() {

	}

	//called after all entities have been read (useful for ID resolution)
	@Override
	public void afterReadAll() {

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Entityc> T self() {
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T as() {
		return (T) this;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '#' + id;
	}
}
