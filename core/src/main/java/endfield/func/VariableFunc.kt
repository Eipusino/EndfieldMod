package endfield.func

@FunctionalInterface
fun interface VariableFunc<P, R> {
	fun get(vararg params: P): R
}
