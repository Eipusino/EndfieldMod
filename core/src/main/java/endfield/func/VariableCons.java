package endfield.func;

@FunctionalInterface
public interface VariableCons<P> {
	@SuppressWarnings("unchecked")
	void get(P... params);
}
