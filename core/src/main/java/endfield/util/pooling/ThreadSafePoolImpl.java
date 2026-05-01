package endfield.util.pooling;

import arc.func.Prov;

public class ThreadSafePoolImpl<T> extends ThreadSafePool<T> {
	public final Prov<? extends T> provider;

	public ThreadSafePoolImpl(Prov<? extends T> provider) {
		this.provider = provider;
	}

	public ThreadSafePoolImpl(int initialCapacity, Prov<? extends T> provider) {
		super(initialCapacity);
		this.provider = provider;
	}

	public ThreadSafePoolImpl(int initialCapacity, int max, Prov<? extends T> provider) {
		super(initialCapacity, max);
		this.provider = provider;
	}

	@Override
	protected T newObject() {
		return provider.get();
	}
}
