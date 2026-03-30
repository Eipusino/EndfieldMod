package endfield.util;

import java.lang.reflect.Method;

/**
 * Create a MethodAccessor using the {@link PlatformImpl#methodAccessor} method.
 * <p>All checked exceptions will be wrapped in a {@link RuntimeException} and do not need to be manually thrown.
 *
 * @since 1.0.9
 */
public interface MethodAccessor {
	/**
	 * @param object the object the underlying method is invoked from
	 * @param args   the arguments used for the method call
	 * @param <T>    Return type, no need to manually cast, but be aware of {@code ClassCastException}
	 * @throws RuntimeException Any exceptions that may be thrown by calling a method
	 */
	<T> T invoke(Object object, Object... args);

	/**
	 * @return The method of definition. Accessible may not be set.
	 */
	Method getMethod();
}
