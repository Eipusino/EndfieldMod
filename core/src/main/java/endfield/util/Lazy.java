package endfield.util;

import arc.func.Prov;

public class Lazy<T> {
	T value;
	Prov<? extends T> prov;

	public Lazy(Prov<? extends T> initial) {
		if (initial == null) throw new IllegalArgumentException("The prov cannot be null.");

		prov = initial;
	}

	public static <T> Lazy<T> of(Prov<? extends T> prov) {
		return new Lazy<>(prov);
	}

	public T get() {
		if (prov == null) return value;

		value = prov.get();
		prov = null;
		return value;
	}
}
