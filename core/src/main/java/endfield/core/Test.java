package endfield.core;

import arc.util.Log;
import aspector.RuntimeAspector;
import aspector.RuntimeAspector.AspectDelegate;
import aspector.annotations.Stub;
import aspector.classes.BytecodeClassLoader;
import aspector.generate.AspectMaker;
import endfield.util.CollectionObjectMap;
import endfield.util.ExtraVariable;
import org.jetbrains.annotations.TestOnly;

import java.util.Map;

import static endfield.Vars2.aspectHelper;

/** Classes for testing purposes only, do not use. */
@TestOnly
public class Test implements Cloneable, ExtraVariable {
	private static short count;

	public Map<String, Object> extraVar = new CollectionObjectMap<>(String.class, Object.class);
	public short id;

	public Object target;
	public int index;

	public Test() {
		id = count++;
	}

	public static void test() throws Throwable {
		BytecodeClassLoader loader = new BytecodeClassLoader(RuntimeAspector.class.getClassLoader());

		AspectDelegate d = RuntimeAspector.withMaker(AspectMaker::new, aspectHelper::packageAccessHandler);
		d.use(loader);
		Aspect instance = (Aspect) d.applyAspect(LoaderAspect.class, d.open(ClassLoader.class)).instance();
		instance.definePackage(Object.class);
	}

	public static void call() {
		try {
			test();
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	public Test copy() {
		try {
			return (Test) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public Map<String, Object> extra() {
		return extraVar;
	}

	public interface Aspect {
		Package definePackage(Class<?> c);
	}

	public interface AccessStub {
		default Package definePackage(Class<?> c) {
			Log.infoTag(toString(), "TODO");
			return null;
		}
	}

	public static class LoaderAspect extends @Stub ClassLoader implements @Stub AccessStub, Aspect {
		@Override
		public Package definePackage(Class<?> c) {
			Log.infoTag(toString(), "definePackage: " + c);
			return AccessStub.super.definePackage(c);
		}
	}
}
