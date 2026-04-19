package aspector.classes;

import java.util.List;

import static endfield.util.Arrays2.map;

public class EConstructor<T> extends ClassElement {
	public final List<Parameter> parameters;

	public EConstructor(ClassDecl<T> decl, List<Parameter> params, int flag, List<EAnnotation> annos) {
		super(decl, "<init>", flag, annos);
		parameters = params;
	}

	MethodSignature descriptor;

	public final MethodSignature descriptor() {
		if (descriptor == null) descriptor = new MethodSignature("<init>", map(parameters, p -> p.annotatedType.type.name), ClassName.V);

		return descriptor;
	}

	public final List<ClassDecl<?>> parameterTypes() {
		return map(parameters, p -> p.annotatedType.type);
	}

	public final List<AnnotatedType<?>> annotatedParameterTypes() {
		return map(parameters, p -> p.annotatedType);
	}
}
