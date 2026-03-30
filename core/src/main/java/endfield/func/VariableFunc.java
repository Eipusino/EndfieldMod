package endfield.func;

@FunctionalInterface
public interface VariableFunc<P, R> {
	@SuppressWarnings("unchecked")
	R get(P... params);
}
