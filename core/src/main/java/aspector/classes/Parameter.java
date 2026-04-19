package aspector.classes;

import java.util.List;

import static endfield.util.Arrays2.find;

public final class Parameter {
	public final String name;
	public final AnnotatedType<?> annotatedType;
	public final List<EAnnotation> annotations;

	public Parameter(String pname, AnnotatedType<?> annoType, List<EAnnotation> annos) {
		name = pname;
		annotatedType = annoType;
		annotations = annos;
	}

	public ClassDecl<?> type() {
		return annotatedType.type;
	}

	public EAnnotation getAnnotation(ClassName annoTypeName) {
		return find(annotations, a -> a.type.equals(annoTypeName));
	}
}
