package endfield.desktop;

import arc.func.Boolf;
import endfield.util.ClassHelper;
import endfield.util.NoSuchFunctionException;
import endfield.util.NoSuchVariableException;
import endfield.util.Reflects;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import static endfield.desktop.DesktopImpl.lookup;
import static endfield.desktop.Unsafer.unsafe;

public class DesktopClassHelper implements ClassHelper {
	static final MethodHandle getFields, getMethods, getConstructors;
	static final VarHandle mtypes, ctypes, ptypes;

	static final Function<Class<?>, Field[]> function4;
	static final Function<Class<?>, Method[]> function5;
	static final Function<Class<?>, Constructor<?>[]> function6;

	/*static final CollectionObjectMap<Class<?>, Field[]> fieldsMap;
	static final CollectionObjectMap<Class<?>, Method[]> methodsMap;
	static final CollectionObjectMap<Class<?>, Constructor<?>[]> constructorsMap;*/

	static {
		try {
			getFields = lookup.findVirtual(Class.class, "getDeclaredFields0", MethodType.methodType(Field[].class, boolean.class));
			getMethods = lookup.findVirtual(Class.class, "getDeclaredMethods0", MethodType.methodType(Method[].class, boolean.class));
			getConstructors = lookup.findVirtual(Class.class, "getDeclaredConstructors0", MethodType.methodType(Constructor[].class, boolean.class));

			mtypes = lookup.findVarHandle(Method.class, "parameterTypes", Class[].class);
			ctypes = lookup.findVarHandle(Constructor.class, "parameterTypes", Class[].class);
			ptypes = lookup.findVarHandle(MethodType.class, "ptypes", Class[].class);
		} catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}

		function4 = clazz -> {
			try {
				return (Field[]) getFields.invokeExact(clazz, false);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
		function5 = clazz -> {
			try {
				return (Method[]) getMethods.invokeExact(clazz, false);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
		function6 = clazz -> {
			try {
				return (Constructor<?>[]) getConstructors.invokeExact(clazz, false);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}

	@Override
	public @Nullable Field findField(Class<?> clazz, String name) {
		Field[] fields = function4.apply(clazz);
		for (Field field : fields) {
			if (field.getName().equals(name)) return field;
		}
		return null;
	}

	@Override
	public @Nullable Method findMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		Method[] methods = function5.apply(clazz);
		for (Method method : methods) {
			if (method.getName().equals(name) && Arrays.equals((Class<?>[]) mtypes.get(method), parameterTypes)) return method;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> @Nullable Constructor<T> findConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		Constructor<T>[] constructors = (Constructor<T>[]) function6.apply(clazz);
		for (Constructor<T> constructor : constructors) {
			if (Arrays.equals((Class<?>[]) ctypes.get(constructor), parameterTypes)) return constructor;
		}
		return null;
	}

	@Override
	public Field getField(Class<?> clazz, String name) {
		Field[] fields = function4.apply(clazz);
		for (Field field : fields) {
			if (field.getName().equals(name)) return field;
		}

		throw new NoSuchVariableException(name);
	}

	@Override
	public Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		Method[] methods = function5.apply(clazz);
		for (Method method : methods) {
			if (method.getName().equals(name) && Arrays.equals((Class<?>[]) mtypes.get(method), parameterTypes)) return method;
		}

		throw new NoSuchFunctionException(Reflects.methodToString(clazz, name, parameterTypes));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		Constructor<T>[] constructors = (Constructor<T>[]) function6.apply(clazz);
		for (Constructor<T> constructor : constructors) {
			if (Arrays.equals((Class<?>[]) ctypes.get(constructor), parameterTypes)) return constructor;
		}

		throw new NoSuchFunctionException(Reflects.methodToString(clazz, "<init>", parameterTypes));
	}

	@Override
	public Field[] getFields(Class<?> clazz) {
		return function4.apply(clazz);
	}

	@Override
	public Method[] getMethods(Class<?> clazz) {
		return function5.apply(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Constructor<T>[] getConstructors(Class<T> clazz) {
		return (Constructor<T>[]) function6.apply(clazz);
	}

	@Override
	public @Nullable Field findField(Class<?> clazz, Boolf<Field> filler) {
		Field[] fields = function4.apply(clazz);
		for (Field field : fields) {
			if (filler.get(field)) {
				return field;
			}
		}
		return null;
	}

	@Override
	public @Nullable Method findMethod(Class<?> clazz, Boolf<Method> filler) {
		Method[] methods = function5.apply(clazz);
		for (Method method : methods) {
			if (filler.get(method)) return method;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> @Nullable Constructor<T> findConstructor(Class<T> clazz, Boolf<Constructor<T>> filler) {
		Constructor<T>[] constructors = (Constructor<T>[]) function6.apply(clazz);
		for (Constructor<T> constructor : constructors) {
			if (filler.get(constructor)) return constructor;
		}
		return null;
	}

	@Override
	public Field getField(Class<?> clazz, Boolf<Field> filler) {
		Field[] fields = function4.apply(clazz);
		for (Field field : fields) {
			if (filler.get(field)) {
				return field;
			}
		}

		throw new NoSuchVariableException("Field not found");
	}

	@Override
	public Method getMethod(Class<?> clazz, Boolf<Method> filler) {
		Method[] methods = function5.apply(clazz);
		for (Method method : methods) {
			if (filler.get(method)) return method;
		}

		throw new NoSuchFunctionException("Method not found");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Constructor<T> getConstructor(Class<T> clazz, Boolf<Constructor<T>> filler) {
		Constructor<T>[] constructors = (Constructor<T>[]) function6.apply(clazz);
		for (Constructor<T> constructor : constructors) {
			if (filler.get(constructor)) return constructor;
		}

		throw new NoSuchFunctionException("Constructor not found");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T allocateInstance(Class<? extends T> clazz) {
		Objects.requireNonNull(clazz);

		try {
			return (T) unsafe.allocateInstance(clazz);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Class<?> defineClass(String name, byte[] bytes, ClassLoader loader) throws ClassFormatError {
		return unsafe.defineClass(name, bytes, 0, bytes.length, loader, null);
	}
}
