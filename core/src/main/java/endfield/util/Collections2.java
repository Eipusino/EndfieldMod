package endfield.util;

import arc.func.Func;
import arc.func.Prov;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Collections2 {
	private Collections2() {}

	@KotlinIn
	@SafeVarargs
	public static <T> Set<T> asSet(T... array) {
		HashSet<T> result = new HashSet<>(array.length);
		Collections.addAll(result, array);
		return result;
	}

	@KotlinIn
	@SafeVarargs
	public static <T> List<T> asList(T... array) {
		ArrayList<T> result = new ArrayList<>(array.length);
		Collections.addAll(result, array);
		return result;
	}

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
	public static <T> void forEachIndexed(Iterable<T> iterable, IterIndexed<? super T> action) {
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

	public interface IterIndexed<T> {
		void get(int index, T t);
	}
}
