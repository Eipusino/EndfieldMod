package endfield.util;

import arc.util.Reflect;
import endfield.Vars2;
import endfield.util.handler.ObjectHandler;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("removal")
public class MockPlatformImpl implements PlatformImpl {
	public static Unsafe unsafe;

	static {
		unsafe = Reflect.get(Unsafe.class, "theUnsafe");
	}

	public MockPlatformImpl() {}

	public MockPlatformImpl setup() {
		Vars2.fieldAccessHelper = new ReflectionFieldAccessHelper();
		Vars2.methodInvokeHelper = new ReflectionMethodInvokeHelper();
		Vars2.classHelper = new MockClassHelper();
		Vars2.accessibleHelper = new MockAccessibleHelper();

		return this;
	}

	@Override
	public Lookup lookup(Class<?> clazz) {
		return Reflects.publicLookup;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T clone(T object) {
		Class<?> type = object.getClass();

		if (type == Class.class || type == Field.class || type == Method.class || type == Constructor.class) return null;

		try {
			T result = (T) unsafe.allocateInstance(type);
			ObjectHandler.copyField(object, result);
			return result;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void put(long srcAddress, long destAddress, long bytes) {
		unsafe.copyMemory(srcAddress, destAddress, bytes);
	}

	@Override
	public void put(Object src, int srcOffset, Object dst, int dstOffset, long bytes) {
		unsafe.copyMemory(src, srcOffset, dst, dstOffset, bytes);
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
