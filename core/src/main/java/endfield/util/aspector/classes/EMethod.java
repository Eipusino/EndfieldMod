package endfield.util.aspector.classes;

import kotlin.collections.CollectionsKt;

import java.util.List;

public class EMethod extends EExecutable {
	public final EAnnotatedType<?> annotatedReturnType;

	MethodSignature descriptor;

	public EMethod(ClassDecl<?> decl, String name, List<Parameter> params, EAnnotatedType<?> annoRType, int flag, List<EAnnotation> annos) {
		super(decl, name, params, flag, annos);
		annotatedReturnType = annoRType;
	}

	@Override
	public final MethodSignature descriptor() {
		if (descriptor == null) descriptor = new MethodSignature(name, CollectionsKt.map(parameters, p -> p.annotatedType.type.name), returnType().name);

		return descriptor;
	}

	public final ClassDecl<?> returnType() {
		return annotatedReturnType.type;
	}
}
