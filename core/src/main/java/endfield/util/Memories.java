package endfield.util;

import java.nio.Buffer;

import static endfield.Vars2.platformImpl;

/**
 * Provide partial memory operations.
 *
 * @since 1.0.9
 */
public final class Memories {
	/** The value of {@code arrayBaseOffset(boolean[].class)}. */
	public static final int ARRAY_BOOLEAN_BASE_OFFSET;
	/** The value of {@code arrayBaseOffset(byte[].class)}. */
	public static final int ARRAY_BYTE_BASE_OFFSET;
	/** The value of {@code arrayBaseOffset(short[].class)}. */
	public static final int ARRAY_SHORT_BASE_OFFSET;
	/** The value of {@code arrayBaseOffset(char[].class)}. */
	public static final int ARRAY_CHAR_BASE_OFFSET;
	/** The value of {@code arrayBaseOffset(int[].class)}. */
	public static final int ARRAY_INT_BASE_OFFSET;
	/** The value of {@code arrayBaseOffset(long[].class)}. */
	public static final int ARRAY_LONG_BASE_OFFSET;
	/** The value of {@code arrayBaseOffset(float[].class)}. */
	public static final int ARRAY_FLOAT_BASE_OFFSET;
	/** The value of {@code arrayBaseOffset(double[].class)}. */
	public static final int ARRAY_DOUBLE_BASE_OFFSET;
	/** The value of {@code arrayBaseOffset(Object[].class)}. */
	public static final int ARRAY_OBJECT_BASE_OFFSET;

	/** The value of {@code arrayIndexScale(boolean[].class)} */
	public static final int ARRAY_BOOLEAN_INDEX_SCALE;
	/** The value of {@code arrayIndexScale(byte[].class)} */
	public static final int ARRAY_BYTE_INDEX_SCALE;
	/** The value of {@code arrayIndexScale(short[].class)} */
	public static final int ARRAY_SHORT_INDEX_SCALE;
	/** The value of {@code arrayIndexScale(char[].class)} */
	public static final int ARRAY_CHAR_INDEX_SCALE;
	/** The value of {@code arrayIndexScale(int[].class)} */
	public static final int ARRAY_INT_INDEX_SCALE;
	/** The value of {@code arrayIndexScale(long[].class)} */
	public static final int ARRAY_LONG_INDEX_SCALE;
	/** The value of {@code arrayIndexScale(float[].class)} */
	public static final int ARRAY_FLOAT_INDEX_SCALE;
	/** The value of {@code arrayIndexScale(double[].class)} */
	public static final int ARRAY_DOUBLE_INDEX_SCALE;
	/** The value of {@code arrayIndexScale(Object[].class)} */
	public static final int ARRAY_OBJECT_INDEX_SCALE;

	static {
		ARRAY_BOOLEAN_BASE_OFFSET = platformImpl.arrayBaseOffset(boolean[].class);
		ARRAY_BYTE_BASE_OFFSET = platformImpl.arrayBaseOffset(byte[].class);
		ARRAY_SHORT_BASE_OFFSET = platformImpl.arrayBaseOffset(short[].class);
		ARRAY_CHAR_BASE_OFFSET = platformImpl.arrayBaseOffset(char[].class);
		ARRAY_INT_BASE_OFFSET = platformImpl.arrayBaseOffset(int[].class);
		ARRAY_LONG_BASE_OFFSET = platformImpl.arrayBaseOffset(long[].class);
		ARRAY_FLOAT_BASE_OFFSET = platformImpl.arrayBaseOffset(float[].class);
		ARRAY_DOUBLE_BASE_OFFSET = platformImpl.arrayBaseOffset(double[].class);
		ARRAY_OBJECT_BASE_OFFSET = platformImpl.arrayBaseOffset(Object[].class);

		ARRAY_BOOLEAN_INDEX_SCALE = platformImpl.arrayIndexScale(boolean[].class);
		ARRAY_BYTE_INDEX_SCALE = platformImpl.arrayIndexScale(byte[].class);
		ARRAY_SHORT_INDEX_SCALE = platformImpl.arrayIndexScale(short[].class);
		ARRAY_INT_INDEX_SCALE = platformImpl.arrayIndexScale(int[].class);
		ARRAY_LONG_INDEX_SCALE = platformImpl.arrayIndexScale(long[].class);
		ARRAY_CHAR_INDEX_SCALE = platformImpl.arrayIndexScale(char[].class);
		ARRAY_FLOAT_INDEX_SCALE = platformImpl.arrayIndexScale(float[].class);
		ARRAY_DOUBLE_INDEX_SCALE = platformImpl.arrayIndexScale(double[].class);
		ARRAY_OBJECT_INDEX_SCALE = platformImpl.arrayIndexScale(Object[].class);
	}

	/** Don't let anyone instantiate this class. */
	private Memories() {}

	/**
	 * @return The memory address of DirectBuffer
	 * @throws IllegalArgumentException If {@code buffer} is not a DirectBuffer
	 */
	public static long addressOf(Buffer buffer) {
		if (!buffer.isDirect()) throw new IllegalArgumentException("buffer is non-direct");

		return ((sun.nio.ch.DirectBuffer) buffer).address();
	}

	/**
	 * @param <T>       Ensure type safety
	 * @param src       the source object
	 * @param srcOffset starting position in the source object
	 * @param dst       the destination object
	 * @param dstOffset starting position in the destination data
	 * @param numBytes  the number of data to be copied
	 */
	public static <T> void copy(T src, int srcOffset, T dst, int dstOffset, int numBytes) {
		platformImpl.put(src, srcOffset, dst, dstOffset, numBytes);
	}

	/**
	 * Copy the contents of an array to another array, with functionality consistent with
	 * {@link System#arraycopy}.
	 *
	 * @param <T>       Ensure type safety
	 * @param src       the source array.
	 * @param srcOffset starting position in the source array.
	 * @param dst       the destination array.
	 * @param dstOffset starting position in the destination data.
	 * @param numBytes  the number of array elements to be copied
	 */
	public static <T> void arraycopy(T[] src, int srcOffset, T[] dst, int dstOffset, int numBytes) {
		platformImpl.put(src, ARRAY_OBJECT_BASE_OFFSET + srcOffset, dst, ARRAY_OBJECT_BASE_OFFSET + dstOffset, numBytes);
	}

	public static void arraycopy(boolean[] src, int srcOffset, boolean[] dst, int dstOffset, int numBytes) {
		platformImpl.put(src, ARRAY_BOOLEAN_BASE_OFFSET + srcOffset, dst, ARRAY_BOOLEAN_BASE_OFFSET + dstOffset, numBytes);
	}

	public static void arraycopy(byte[] src, int srcOffset, byte[] dst, int dstOffset, int numBytes) {
		platformImpl.put(src, ARRAY_BYTE_BASE_OFFSET + srcOffset, dst, ARRAY_BYTE_BASE_OFFSET + dstOffset, numBytes);
	}

	public static void arraycopy(short[] src, int srcOffset, short[] dst, int dstOffset, int numBytes) {
		platformImpl.put(src, ARRAY_SHORT_BASE_OFFSET + srcOffset, dst, ARRAY_SHORT_BASE_OFFSET + dstOffset, numBytes);
	}

	public static void arraycopy(char[] src, int srcOffset, char[] dst, int dstOffset, int numBytes) {
		platformImpl.put(src, ARRAY_CHAR_BASE_OFFSET + srcOffset, dst, ARRAY_CHAR_BASE_OFFSET + dstOffset, numBytes);
	}

	public static void arraycopy(int[] src, int srcOffset, int[] dst, int dstOffset, int numBytes) {
		platformImpl.put(src, ARRAY_INT_BASE_OFFSET + srcOffset, dst, ARRAY_INT_BASE_OFFSET + dstOffset, numBytes);
	}

	public static void arraycopy(long[] src, int srcOffset, long[] dst, int dstOffset, int numBytes) {
		platformImpl.put(src, ARRAY_LONG_BASE_OFFSET + srcOffset, dst, ARRAY_LONG_BASE_OFFSET + dstOffset, numBytes);
	}

	public static void arraycopy(float[] src, int srcOffset, float[] dst, int dstOffset, int numBytes) {
		platformImpl.put(src, ARRAY_FLOAT_BASE_OFFSET + srcOffset, dst, ARRAY_FLOAT_BASE_OFFSET + dstOffset, numBytes);
	}

	public static void arraycopy(double[] src, int srcOffset, double[] dst, int dstOffset, int numBytes) {
		platformImpl.put(src, ARRAY_DOUBLE_BASE_OFFSET + srcOffset, dst, ARRAY_DOUBLE_BASE_OFFSET + dstOffset, numBytes);
	}
}
