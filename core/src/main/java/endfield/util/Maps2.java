package endfield.util;

import kotlin.jvm.functions.Function0;

import java.util.Map;

public final class Maps2 {
	private Maps2() {}

	@KotlinIn
	public static <K, V> V getOrElse(Map<K, V> map, K key, Function0<V> defaultValue) {
		V v = map.get(key);
		return v == null ? defaultValue.invoke() : v;
	}
}
