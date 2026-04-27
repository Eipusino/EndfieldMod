package endfield.func

@FunctionalInterface
fun interface VariableCons<P> {
	fun get(vararg params: P)
}
