package endfield.desktop;

import arc.func.Boolf;
import endfield.util.ClassHelper;
import endfield.util.Reflects;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import static endfield.desktop.DesktopImpl.lookup;
import static endfield.desktop.Unsafer.unsafe;

public class DesktopClassHelper implements ClassHelper {
	static final MethodHandle getFields, getMethods, getConstructors;
	static final VarHandle mtypes, ctypes, ptypes;

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
	}

	@Override
	public Field findField(Class<?> clazz, String name) {
		try {
			Field[] fields = (Field[]) getFields.invokeExact(clazz, false);
			for (Field field : fields) {
				if (field.getName().equals(name)) return field;
			}
			return null;
		} catch (Throwable e) {
			return null;
		}
	}

	@Override
	public Method findMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		try {
			Method[] methods = (Method[]) getMethods.invokeExact(clazz, false);
			for (Method method : methods) {
				if (method.getName().equals(name) && Arrays.equals((Class<?>[]) mtypes.get(method), parameterTypes)) return method;
			}
			return null;
		} catch (Throwable e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Constructor<T> findConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		try {
			Constructor<T>[] constructors = (Constructor<T>[]) getConstructors.invokeExact(clazz, false);
			for (Constructor<T> constructor : constructors) {
				if (Arrays.equals((Class<?>[]) ctypes.get(constructor), parameterTypes)) return constructor;
			}
			return null;
		} catch (Throwable e) {
			return null;
		}
	}

	@Override
	public Field getField(Class<?> clazz, String name) {
		try {
			Field[] fields = (Field[]) getFields.invokeExact(clazz, false);
			for (Field field : fields) {
				if (field.getName().equals(name)) return field;
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		throw new RuntimeException(name);
	}

	@Override
	public Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		try {
			Method[] methods = (Method[]) getMethods.invokeExact(clazz, false);
			for (Method method : methods) {
				if (method.getName().equals(name) && Arrays.equals((Class<?>[]) mtypes.get(method), parameterTypes)) return method;
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		throw new RuntimeException(Reflects.methodToString(clazz, name, parameterTypes));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		try {
			Constructor<T>[] constructors = (Constructor<T>[]) getConstructors.invokeExact(clazz, false);
			for (Constructor<T> constructor : constructors) {
				if (Arrays.equals((Class<?>[]) ctypes.get(constructor), parameterTypes)) return constructor;
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		throw new RuntimeException(Reflects.methodToString(clazz, "<init>", parameterTypes));
	}

	@Override
	public Field[] getFields(Class<?> clazz) {
		try {
			return (Field[]) getFields.invokeExact(clazz, false);
		} catch (Throwable e) {
			return clazz.getDeclaredFields();
		}
	}

	@Override
	public Method[] getMethods(Class<?> clazz) {
		try {
			return (Method[]) getMethods.invokeExact(clazz, false);
		} catch (Throwable e) {
			return clazz.getDeclaredMethods();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Constructor<T>[] getConstructors(Class<T> clazz) {
		try {
			return (Constructor<T>[]) getConstructors.invokeExact(clazz, false);
		} catch (Throwable e) {
			return (Constructor<T>[]) clazz.getDeclaredConstructors();
		}
	}

	@Override
	public Field findField(Class<?> clazz, Boolf<Field> filler) {
		try {
			Field[] fields = (Field[]) getFields.invokeExact(clazz, false);
			for (Field field : fields) {
				if (filler.get(field)) {
					return field;
				}
			}
			return null;
		} catch (Throwable e) {
			return null;
		}
	}

	@Override
	public Method findMethod(Class<?> clazz, Boolf<Method> filler) {
		try {
			Method[] methods = (Method[]) getMethods.invokeExact(clazz, false);
			for (Method method : methods) {
				if (filler.get(method)) return method;
			}
			return null;
		} catch (Throwable e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Constructor<T> findConstructor(Class<T> clazz, Boolf<Constructor<T>> filler) {
		try {
			Constructor<T>[] constructors = (Constructor<T>[]) getConstructors.invokeExact(clazz, false);
			for (Constructor<T> constructor : constructors) {
				if (filler.get(constructor)) return constructor;
			}
			return null;
		} catch (Throwable e) {
			return null;
		}
	}

	@Override
	public Field getField(Class<?> clazz, Boolf<Field> filler) {
		try {
			Field[] fields = (Field[]) getFields.invokeExact(clazz, false);
			for (Field field : fields) {
				if (filler.get(field)) {
					return field;
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		throw new RuntimeException("Field not found");
	}

	@Override
	public Method getMethod(Class<?> clazz, Boolf<Method> filler) {
		try {
			Method[] methods = (Method[]) getMethods.invokeExact(clazz, false);
			for (Method method : methods) {
				if (filler.get(method)) return method;
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		throw new RuntimeException("Method not found");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Constructor<T> getConstructor(Class<T> clazz, Boolf<Constructor<T>> filler) {
		try {
			Constructor<T>[] constructors = (Constructor<T>[]) getConstructors.invokeExact(clazz, false);
			for (Constructor<T> constructor : constructors) {
				if (filler.get(constructor)) return constructor;
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		throw new RuntimeException("Constructor not found");
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
