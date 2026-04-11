package endfield.util;

import java.lang.reflect.Constructor;

/**
 * Create a ConstructorAccessor using the {@link PlatformImpl#constructorAccessor} method.
 * <p>All checked exceptions will be wrapped in a {@link RuntimeException} and do not need to be manually thrown.
 *
 * @since 1.0.9
 */
public interface ConstructorAccessor<T> {
	/**
	 * @param args array of objects to be passed as arguments to the constructor call.
	 * @return a new object created by calling the constructor this object represents.
	 * @throws RuntimeException Any exceptions that may be thrown by calling a constructor.
	 */
	T newInstance(Object... args);

	/**
	 * @return The constructor of definition. Accessible may not be set.
	 */
	Constructor<T> getConstructor();
}
