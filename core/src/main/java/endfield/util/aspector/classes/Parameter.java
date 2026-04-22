package endfield.util.aspector.classes;

import endfield.util.Arrays2;

import java.util.List;

public final class Parameter {
	public final String name;
	public final EAnnotatedType<?> annotatedType;
	public final List<EAnnotation> annotations;

	public Parameter(String paramName, EAnnotatedType<?> annoType, List<EAnnotation> annos) {
		name = paramName;
		annotatedType = annoType;
		annotations = annos;
	}

	public ClassDecl<?> type() {
		return annotatedType.type;
	}

	public EAnnotation getAnnotation(ClassName annoTypeName) {
		return Arrays2.find(annotations, a -> a.type.equals(annoTypeName));
	}
}
