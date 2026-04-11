package endfield.util;

import mindustry.Vars;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Handling APIs for different implementations of Desktop and Android.
 *
 * @since 1.0.9
 */
public interface PlatformImpl {
	/**
	 * <pre>{@code
	 * MethodHandle icons = platformImpl.lookup(Block.class).findVirtual(Block.class, "icons", MethodType.methodType(TextureRegion[].class));
	 * }</pre>
	 * <pre>{@code
	 * Method method = Block.class.getDeclaredMethod("icons");
	 * MethodHandle icons = platformImpl.lookup(method.getDeclaringClass()).unreflect(method);
	 * }</pre>
	 *
	 * @return Retrieve a {@code lookup} that can access all members within a given {@code class}.
	 */
	Lookup lookup(Class<?> clazz);

	/**
	 * Reflect to call the clone method of Object.
	 */
	<T> T clone(T object);

	/**
	 * Wrap the field in the Accessor and handle all checked exceptions.
	 * <p>Allow modification of {@code final} fields, provided there are no issues arising from special platforms.
	 *
	 * @param field Field to be packaged
	 * @return New field accessor
	 */
	default FieldAccessor fieldAccessor(Field field) {
		return new ReflectionFieldAccessor(field);
	}

	/**
	 * Wrap the method in the Accessor and handle all checked exceptions.
	 *
	 * @param method Method to be packaged
	 * @return New method accessor
	 */
	default MethodAccessor methodAccessor(Method method) {
		return new ReflectionMethodAccessor(method);
	}

	/**
	 * Wrap the constructor in the Accessor and handle all checked exceptions.
	 *
	 * @param constructor Constructor to be packaged
	 * @return New constructor accessor
	 */
	default <T> ConstructorAccessor<T> constructorAccessor(Constructor<T> constructor) {
		return new ReflectionConstructorAccessor<>(constructor);
	}

	/** Gets the {@code Class} object of the caller who invoked the method that invoked {@code getCallerClass}. */
	default @UnknownNullability Class<?> getCallerClass() {
		Thread thread = Thread.currentThread();
		StackTraceElement[] trace = thread.getStackTrace();

		if (trace.length < 4) return null;

		try {
			return Class.forName(trace[3].getClassName(), false, Vars.mods.mainLoader());
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * Sets all bytes in a given block of memory to a copy of another block.
	 *
	 * @see Memories
	 */
	void put(long srcAddress, long destAddress, long bytes);

	/**
	 * Sets all bytes in a given block of memory to a copy of another block.
	 *
	 * @throws NullPointerException src or dst is null
	 * @see Memories
	 */
	void put(Object src, int srcOffset, Object dst, int dstOffset, long bytes);

	/** Reports the offset of the first element in the storage allocation of a given array class. */
	int arrayBaseOffset(Class<?> arrayClass);

	/** Reports the scale factor for addressing elements in the storage allocation of a given array class. */
	int arrayIndexScale(Class<?> arrayClass);
}
