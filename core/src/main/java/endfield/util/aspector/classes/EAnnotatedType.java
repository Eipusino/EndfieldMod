package endfield.util.aspector.classes;

import endfield.util.Arrays2;

import java.util.List;

public class EAnnotatedType<T> {
	public ClassDecl<T> type;
	public List<EAnnotation> annotations;

	public EAnnotatedType(ClassDecl<T> decl, List<EAnnotation> annos) {
		type = decl;
		annotations = annos;
	}

	public EAnnotation getAnnotation(ClassName ClassName) {
		return Arrays2.find(annotations, a -> a.type.equals(ClassName));
	}
}
