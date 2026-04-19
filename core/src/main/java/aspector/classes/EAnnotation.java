package aspector.classes;

import java.util.Map;

public class EAnnotation {
	public final ClassName type;

	final Map<String, AnnotationValue<?, ?>> values;

	public EAnnotation(ClassName t, Map<String, AnnotationValue<?, ?>> m) {
		type = t;
		values = m;
	}

	@SuppressWarnings("unchecked")
	public <T extends AnnotationValue<?, ?>> T getValue(String name) {
		return (T) values.get(name);
	}
}
