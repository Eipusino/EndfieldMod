package endfield.util;

import endfield.util.handler.FieldHandler;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface SharedObject {
	String sharedID();

	default List<Field> sharedReferenceFields() {
		Field[] fields = getClass().getDeclaredFields();
		List<Field> list = new CollectionList<>(Field.class);

		for (Field field : fields) {
			if (field.getAnnotation(SharedField.class) != null) list.add(field);
		}

		return list;
	}

	default Properties sharedDataSwapProp() {
		return System.getProperties();
	}

	@NonExtendable
	default void setupSharedReferences() {
		Properties properties = sharedDataSwapProp();
		Object className = properties.get("shared-" + sharedID());

		if (className == null) {
			setupProperties(properties);
		} else {
			getByProperties(properties);
		}
	}

	default void setupProperties(Properties properties) {
		List<Field> sharedFields = sharedReferenceFields();
		properties.put("shared-" + sharedID(), this);
		properties.put("shared-" + sharedID() + "-fields", sharedFields);
	}

	@SuppressWarnings("unchecked")
	default void getByProperties(Properties properties) {
		Object existingSharedObject = properties.get("shared-" + sharedID());
		List<Field> sharedFields = (List<Field>) properties.get("shared-" + sharedID() + "-fields");

		Map<String, Field> fieldMap = new CollectionObjectMap<>(String.class, Field.class);

		for (Field field : sharedFields) {
			fieldMap.put(field.getName(), field);
		}

		for (Field field : sharedReferenceFields()) {
			var existing = fieldMap.get(field.getName());
			if (existing == null)
				throw new IllegalArgumentException("Field " + field.getName() + " not found in existed shared object.");

			Reflects.setAccessible(existing);
			Reflects.setAccessible(field);

			FieldHandler.set(this, field, FieldHandler.get(existingSharedObject, existing));
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface SharedField {
	}
}
