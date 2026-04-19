package aspector.classes;

import java.util.List;

import static endfield.util.Arrays2.find;

public class AnnotatedType<T> {
	public ClassDecl<T> type;
	public List<EAnnotation> annotations;

	public AnnotatedType(ClassDecl<T> decl, List<EAnnotation> annos) {
		type = decl;
		annotations = annos;
	}

	public EAnnotation getAnnotation(ClassName ClassName) {
		return find(annotations, a -> a.type.equals(ClassName));
	}
}
