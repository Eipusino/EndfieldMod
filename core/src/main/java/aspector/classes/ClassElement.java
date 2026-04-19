package aspector.classes;

import java.util.List;

import static endfield.util.Arrays2.find;

public abstract class ClassElement {
	public final ClassDecl<?> declaring;
	public final String name;
	public final int flags;
	public final List<EAnnotation> annotations;

	public ClassElement(ClassDecl<?> decl, String cName, int flag, List<EAnnotation> annos) {
		declaring = decl;
		name = cName;
		flags = flag;
		annotations = annos;
	}

	public EAnnotation getAnnotation(ClassName annoTypeName) {
		return find(annotations, a -> a.type.equals(annoTypeName));
	}
}
