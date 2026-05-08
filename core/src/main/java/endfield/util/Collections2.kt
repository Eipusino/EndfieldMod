package endfield.util

inline fun <reified E> list(ordered: Boolean = true, capacity: Int = 16): CollectionList<E> =
	CollectionList(ordered, capacity, E::class.java)
inline fun <reified E> set(initialCapacity: Int = 51, loadFactor: Float = 0.8f): CollectionObjectSet<E> =
	CollectionObjectSet(E::class.java, initialCapacity, loadFactor)
inline fun <reified K, reified V> map(initialCapacity: Int = 51, loadFactor: Float = 0.8f): CollectionObjectMap<K, V> =
	CollectionObjectMap(K::class.java, V::class.java, initialCapacity, loadFactor)
