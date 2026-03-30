package endfield.util;

import java.io.Serializable;

/**
 * Represents a generic pair of two values.
 * <p>There is no meaning attached to values in this class, it can be used for any purpose.
 * Pair exhibits value semantics, i.e. two pairs are equal if both components are equal.
 * <p>An example of decomposing it into values:
 *
 * @param <A> type of the first value.
 * @param <B> type of the second value.
 */
public class Pair<A, B> implements Serializable {
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

	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}
}
