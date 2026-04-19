package aspector.classes;

import java.util.List;

public class EField extends ClassElement {
	public final AnnotatedType<?> annotatedType;
	public final Object constant;

	public EField(ClassDecl<?> decl, String name, AnnotatedType<?> annoType, int flag, Object cons, List<EAnnotation> annos) {
		super(decl, name, flag, annos);
		annotatedType = annoType;
		constant = cons;
	}

	public ClassDecl<?> type() {
		return annotatedType.type;
	}
}
