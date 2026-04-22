package endfield.util.aspector.classes;

import java.util.List;

public class EField extends ClassElement {
	public final EAnnotatedType<?> annotatedType;
	public final Object constant;

	public EField(ClassDecl<?> decl, String name, EAnnotatedType<?> annoType, int flag, Object cons, List<EAnnotation> annos) {
		super(decl, name, flag, annos);
		annotatedType = annoType;
		constant = cons;
	}

	public ClassDecl<?> type() {
		return annotatedType.type;
	}
}
