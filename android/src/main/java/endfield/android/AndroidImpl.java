package endfield.android;

import arc.util.Log;
import dalvik.system.VMStack;
import endfield.util.CollectionObjectMap;
import endfield.util.FieldAccessor;
import endfield.util.PlatformImpl;
import endfield.util.ReflectionMethodInvokeHelper;
import libcore.io.Memory;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;

import static endfield.Vars2.accessibleHelper;
import static endfield.Vars2.classHelper;
import static endfield.Vars2.fieldAccessHelper;
import static endfield.Vars2.methodInvokeHelper;
import static endfield.android.Unsafer.unsafe;

@SuppressWarnings("removal")
public class AndroidImpl implements PlatformImpl {
	static Constructor<Lookup> constructor;

	static final CollectionObjectMap<Class<?>, Lookup> lookupMap = new CollectionObjectMap<>(Class.class, Lookup.class);
	static final Function<Class<?>, Lookup> lookupBuilder = clazz -> {
		try {
			return constructor.newInstance(clazz, 15);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	};
	static Method clone;

	static {
		try {
			HiddenApi.setup();
		} catch (Throwable e) {
			Log.err("It seems you platform is special. (But don't worry)", e);
		}

		accessibleHelper = new AndroidAccessibleHelper();
		classHelper = new AndroidClassHelper();
		fieldAccessHelper = new UnsafeFieldAccessHelper();
		methodInvokeHelper = new ReflectionMethodInvokeHelper();

		try {
			constructor = Lookup.class.getDeclaredConstructor(Class.class, int.class);
			constructor.setAccessible(true);
		} catch (Throwable e) {
			Log.err(e);
		}

		try {
			clone = Object.class.getDeclaredMethod("internalClone");
			clone.setAccessible(true);
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T clone(T object) {
		Class<?> type = object.getClass();

		if (type == Class.class) return null;

		try {
			return (T) clone.invoke(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// Due to the lack of TRUSTED lookup in Android, each class needs to create an ALL_MODES lookup.
	@Override
	public Lookup lookup(Class<?> clazz) {
		return lookupMap.computeIfAbsent(clazz, lookupBuilder);
	}

	@Override
	public FieldAccessor fieldAccessor(Field field) {
		return UnsafeFieldAccessor.getUnsafeFieldAccessor(field);
	}

	@Override
	public Class<?> getCallerClass() {
		return VMStack.getStackClass2();
	}

	@Override
	public void put(long srcAddress, long destAddress, long bytes) {
		unsafe.copyMemory(srcAddress, destAddress, bytes);
	}

	@Override
	public void put(Object src, int srcOffset, Object dst, int dstOffset, long bytes) {
		Objects.requireNonNull(src);
		Objects.requireNonNull(dst);

		Memory.memmove(dst, dstOffset, src, srcOffset, bytes);
	}

	@Override
	public int arrayBaseOffset(Class<?> arrayClass) {
		return unsafe.arrayBaseOffset(arrayClass);
	}

	@Override
	public int arrayIndexScale(Class<?> arrayClass) {
		return unsafe.arrayIndexScale(arrayClass);
	}
}
