package endfield.util;

import java.util.Iterator;
import java.util.function.Consumer;

public class ValueList extends ValueMap implements Iterable<ValueMap> {
	protected CollectionList<ValueMap> list = new CollectionList<>(ValueMap.class);

	public ValueMap getMap(int index) {
		return list.get(index);
	}

	public void add(ValueMap f) {
		list.add(f);
	}

	public void addFloat(float f) {
		list.add(new ValueMap(f));
	}

	public float getFloat(int t) {
		return list.get(t).floatValue;
	}

	public void addInt(int f) {
		list.add(new ValueMap(f));
	}

	public float getInt(int t) {
		return list.get(t).intValue;
	}

	public void add(Object f) {
		list.add(new ValueMap(f));
	}

	@SuppressWarnings("unchecked")
	public <T> T get(int t) {
		return (T) list.get(t).value;
	}

	public void set(int index, ValueMap f) {
		list.set(index, f);
	}

	@Override
	public Iterator<ValueMap> iterator() {
		return list.iterator();
	}

	@Override
	public void forEach(Consumer<? super ValueMap> action) {
		list.forEach(action);
	}

	public int length() {
		return list.size();
	}

	@Override
	public ValueList copy() {
		try {
			ValueList out = (ValueList) super.clone();
			out.map = map.copy();
			out.list = list.copy();
			return out;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
