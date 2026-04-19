package aspector.classes;

import java.util.List;

import static endfield.util.Arrays2.map;

public class EMethod extends ClassElement {
	public final List<Parameter> parameters;
	public final AnnotatedType<?> annotatedReturnType;

	MethodSignature descriptor;

	public EMethod(ClassDecl<?> decl, String name, List<Parameter> params, AnnotatedType<?> annoRType, int flag, List<EAnnotation> annos) {
		super(decl, name, flag, annos);
		parameters = params;
		annotatedReturnType = annoRType;
	}

	public final MethodSignature descriptor() {
		if (descriptor == null) descriptor = new MethodSignature(name, map(parameters, p -> p.annotatedType.type.name), returnType().name);

		return descriptor;
	}

	public final List<ClassDecl<?>> parameterTypes() {
		return map(parameters, p -> p.annotatedType.type);
	}

	public final List<AnnotatedType<?>> annotatedParameterTypes() {
		return map(parameters, p -> p.annotatedType);
	}

	public final ClassDecl<?> returnType() {
		return annotatedReturnType.type;
	}
}
