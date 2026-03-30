package endfield.util.holder;

import java.util.Objects;

public class ObjectLongHolder<K> implements Cloneable {
	public K key;
	public long value;

	public ObjectLongHolder() {}

	public ObjectLongHolder(K k, long v) {
		key = k;
		value = v;
	}

	public ObjectLongHolder<K> set(K k, long v) {
		key = k;
		value = v;

		return this;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof ObjectLongHolder<?> that && Objects.equals(key, that.key) && value == that.value;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(key) ^ Long.hashCode(value);
	}

	@Override
	public String toString() {
		return key + "=" + value;
	}

	@SuppressWarnings("unchecked")
	public ObjectLongHolder<K> copy() {
		try {
			return (ObjectLongHolder<K>) super.clone();
		} catch (CloneNotSupportedException e) {
			return new ObjectLongHolder<>(key, value);
		}
	}
}
