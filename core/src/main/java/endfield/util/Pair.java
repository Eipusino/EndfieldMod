package endfield.util;

import arc.func.Prov;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a generic pair of two values.
 * <p>There is no meaning attached to values in this class, it can be used for any purpose.
 * Pair exhibits value semantics, i.e. two pairs are equal if both components are equal.
 * <p>An example of decomposing it into values:
 *
 * @param <A> type of the first value.
 * @param <B> type of the second value.
 */
public class Pair<A, B> implements Cloneable, Serializable {
	private static final long serialVersionUID = -53431651999908320l;

	public A first;
	public B second;

	/**
	 * Creates a new instance of Pair.
	 *
	 * @param a First value.
	 * @param b Second value.
	 */
	public Pair(A a, B b) {
		first = a;
		second = b;
	}

	public static <A, B> Pair<A, B> of(A first, B second) {
		return new Pair<>(first, second);
	}

	public A getFirst() {
		return first;
	}

	public A getFirst(Prov<? extends A> prov) {
		return first = first == null ? prov.get() : first;
	}

	public B getSecond() {
		return second;
	}

	public B getSecond(Prov<? extends B> prov) {
		return second = second == null ? prov.get() : second;
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof Pair<?, ?> other && Objects.equals(other.second, second) && Objects.equals(other.first, first);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(second) ^ Objects.hashCode(first);
	}

	@SuppressWarnings("unchecked")
	public Pair<A, B> copy() {
		try {
			return (Pair<A, B>) super.clone();
		} catch (CloneNotSupportedException e) {
			return new Pair<>(first, second);
		}
	}
}
