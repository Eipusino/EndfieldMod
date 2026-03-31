package endfield.android;

import arc.func.Prov;
import endfield.util.ReflectionFieldAccessHelper;
import endfield.util.CollectionObjectMap;

import java.lang.reflect.Field;

public class AndroidReflectionFieldAccessHelper extends ReflectionFieldAccessHelper {
	protected static final Prov<CollectionObjectMap<String, Field>> prov = () -> new CollectionObjectMap<>(String.class, Field.class);

	public Field getField(Class<?> clazz, String name, boolean isStatic) throws NoSuchFieldException {
		CollectionObjectMap<String, Field> map = fieldMap.get(clazz, prov);
		Field field = map.get(name);
		if (field != null) return field;

		if (isStatic) {
			Field f = getField(clazz, name);
			map.put(name, f);
			return f;
		} else {
			Class<?> curr = clazz;
			while (curr != Object.class) {
				try {
					Field f = getField(clazz, name);
					map.put(name, f);
					return f;
				} catch (NoSuchFieldException ignored) {}

				curr = curr.getSuperclass();
			}
		}

		throw new NoSuchFieldException("field " + name + " was not found in class: " + clazz);
	}
}
