package aspector;

public class TypePair<T> {
	public final Class<T> type;
	public final T value;

	public TypePair(Class<T> c, T v) {
		type = c;
		value = v;
	}
}
