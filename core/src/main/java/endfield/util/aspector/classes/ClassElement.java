package endfield.util.aspector.classes;

import endfield.util.Arrays2;

import java.util.List;

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
		return Arrays2.find(annotations, a -> a.type.equals(annoTypeName));
	}
}
