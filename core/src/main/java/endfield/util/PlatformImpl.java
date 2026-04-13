package endfield.util;

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
	Lookup lookup(Class<?> clazz);

	<T> T clone(T object);

	default FieldAccessor fieldAccessor(Field field) {
		return new ReflectionFieldAccessor(field);
	}

	default MethodAccessor methodAccessor(Method method) {
		return new ReflectionMethodAccessor(method);
	}

	default <T> ConstructorAccessor<T> constructorAccessor(Constructor<T> constructor) {
		return new ReflectionConstructorAccessor<>(constructor);
	}

	void put(long srcAddress, long destAddress, long bytes);

	void put(Object src, int srcOffset, Object dst, int dstOffset, long bytes);

	int arrayBaseOffset(Class<?> arrayClass);

	int arrayIndexScale(Class<?> arrayClass);
}
