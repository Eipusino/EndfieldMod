package endfield.desktop;

import arc.util.Log;
import endfield.core.EndFieldMod;
import endfield.util.CollectionObjectMap;
import endfield.util.ConstructorAccessor;
import endfield.util.MockAccessibleHelper;
import endfield.util.MockClassHelper;
import endfield.util.ReflectionFieldAccessHelper;
import endfield.util.ReflectionMethodInvokeHelper;
import endfield.util.FieldAccessor;
import endfield.util.MethodAccessor;
import endfield.util.PlatformImpl;
import endfield.util.Reflects;
import endfield.util.handler.ObjectHandler;
import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Function;

import static endfield.Vars2.accessibleHelper;
import static endfield.Vars2.aspectHelper;
import static endfield.Vars2.classHelper;
import static endfield.Vars2.fieldAccessHelper;
import static endfield.Vars2.methodInvokeHelper;
import static endfield.desktop.DesktopConstant.clone;
import static endfield.desktop.Unsafer.unsafe;

public class DesktopImpl implements PlatformImpl {
	static Lookup lookup;

	static final Class<?>[] LOOKUP_PARAMETER_TYPES = {Class.class, Class.class, int.class};

	static final CollectionObjectMap<Class<?>, Lookup> lookupMap;
	static final Function<Class<?>, Lookup> lookupBuilder;

	static {
		try {
			lookup = (Lookup) ReflectionFactory.getReflectionFactory()
					.newConstructorForSerialization(Lookup.class, Lookup.class.getDeclaredConstructor(LOOKUP_PARAMETER_TYPES))
					.newInstance(EndFieldMod.class, null, -1);

			Demodulator.openModules();
			Demodulator.ensureFieldOpen();

			classHelper = new DesktopClassHelper();
			fieldAccessHelper = new UnsafeFieldAccessHelper();
			methodInvokeHelper = new MethodHandleMethodInvokeHelper();
			accessibleHelper = new DesktopAccessibleHelper();

			aspectHelper = new DesktopAspectHelper();
		} catch (Throwable e) {
			Log.err("It seems you platform is special. (But don't worry)", e);

			lookup = Reflects.PUBLIC_LOOKUP;

			classHelper = new MockClassHelper();
			fieldAccessHelper = new ReflectionFieldAccessHelper();
			methodInvokeHelper = new ReflectionMethodInvokeHelper();
			accessibleHelper = new MockAccessibleHelper() {
				@Override
				public void makeAccessible(AccessibleObject object) {
					object.trySetAccessible();
				}
			};
		}

		lookupMap = new CollectionObjectMap<>(Class.class, Lookup.class);
		lookupBuilder = clazz -> (Lookup) methodInvokeHelper.newInstanceTyped(clazz, LOOKUP_PARAMETER_TYPES, clazz, null, 95);
	}

	@Override
	public Lookup lookup(Class<?> clazz) {
		return lookupMap.computeIfAbsent(clazz, lookupBuilder);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T clone(T object) {
		try {
			// If the object implements the Cloneable interface, call Object.clone() directly, which is faster than copyField().
			if (object instanceof Cloneable) {
				return (T) clone.invokeExact(object);
			}

			Class<?> type = object.getClass();

			if (type == Class.class || type == Field.class || type == Method.class || type == Constructor.class) return null;

			T result = (T) unsafe.allocateInstance(object.getClass());
			// The performance overhead may be high, but there is currently no other way.
			ObjectHandler.copyField(object, result);
			return result;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public FieldAccessor fieldAccessor(Field field) {
		return UnsafeFieldAccessor.getUnsafeFieldAccessor(field);
	}

	@Override
	public MethodAccessor methodAccessor(Method method) {
		return (method.getModifiers() & Modifier.STATIC) != 0 ?
				new MethodHandleStaticMethodAccessor(method) :
				new MethodHandleVirtualMethodAccessor(method);
	}

	@Override
	public <T> ConstructorAccessor<T> constructorAccessor(Constructor<T> constructor) {
		return new MethodHandleConstructorAccessor<>(constructor);
	}

	@Override
	public void put(long srcAddress, long destAddress, long bytes) {
		unsafe.copyMemory(srcAddress, destAddress, bytes);
	}

	@Override
	public void put(Object src, int srcOffset, Object dst, int dstOffset, long bytes) {
		Objects.requireNonNull(src);
		Objects.requireNonNull(dst);

		unsafe.copyMemory(src, srcOffset, dst, dstOffset, bytes);
	}

	@Override
	public int arrayBaseOffset(Class<?> arrayClass) {
		return (int) unsafe.arrayBaseOffset(arrayClass);
	}

	@Override
	public int arrayIndexScale(Class<?> arrayClass) {
		return unsafe.arrayIndexScale(arrayClass);
	}
}
