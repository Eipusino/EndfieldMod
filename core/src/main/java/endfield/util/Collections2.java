package endfield.util;

import arc.func.Func;
import arc.func.Prov;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Collections2 {
	private Collections2() {}

	@KotlinIn
	public static <T, R> @Nullable R firstNotNullOfOrNull(Iterable<T> iterable, Func<? super T, ? extends R> transform) {
		for (T element : iterable) {
			R result = transform.get(element);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	@KotlinIn
	public static <T> void forEachIndexed(Iterable<T> iterable, IndexedConsume<? super T> action) {
		int i = 0;
		for (T t : iterable) {
			action.get(i++, t);
		}
	}

	@SuppressWarnings("unchecked")
	@KotlinIn
	public static <T> List<T> filterIsInstance(Object[] array, Class<T> type) {
		ArrayList<T> result = new ArrayList<>();
		for (Object o : array) {
			if (type.isInstance(o)) result.add((T) o);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@KotlinIn
	public static <T> List<T> filterIsInstance(Iterable<?> iterable, Class<T> type) {
		ArrayList<T> result = new ArrayList<>();
		for (Object o : iterable) {
			if (type.isInstance(o)) result.add((T) o);
		}
		return result;
	}

	@KotlinIn
	public static <K, V> V getOrPut(Map<K, V> map, K key, Prov<? extends V> defaultValue) {
		V value = map.get(key);
		if (value == null) {
			value = defaultValue.get();
			map.put(key, value);
		}
		return value;
	}

	@FunctionalInterface
	public interface IndexedConsume<T> {
		void get(int index, T t);
	}
}
