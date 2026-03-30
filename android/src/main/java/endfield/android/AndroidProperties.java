package endfield.android;

import android.util.Property;
import arc.util.Log;
import dalvik.system.VMRuntime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public final class AndroidProperties {
	static Property<Class, Method[]> methods;
	static Property<Class, Constructor[]> constructors;
	static Property<Class, Field[]> fields;

	private AndroidProperties() {}

	static boolean load() {
		try {
			methods = Property.of(Class.class, Method[].class, "declaredMethods");
			constructors = Property.of(Class.class, Constructor[].class, "declaredConstructors");
			fields = Property.of(Class.class, Field[].class, "declaredFields");

			for (Method method : methods.get(VMRuntime.class)) {
				if (method.getName().equals("setHiddenApiExemptions")) {
					method.invoke(VMRuntime.getRuntime(), (Object) HiddenApi.values);

					HiddenApi.method = method;

					return true;
				}
			}

			Log.info("Property not found setHiddenApiExemptions");

			return false;
		} catch (Throwable e) {
			Log.err(e);

			return false;
		}
	}
}
