package endfield.util.aspector.classes;

import kotlin.collections.CollectionsKt;

import java.util.List;

public abstract class EExecutable extends ClassElement {
	public final List<Parameter> parameters;

	public EExecutable(ClassDecl<?> decl, String name, List<Parameter> params, int flag, List<EAnnotation> annos) {
		super(decl, name, flag, annos);
		parameters = params;
	}

	public abstract MethodSignature descriptor();

	public List<ClassDecl<?>> parameterTypes() {
		return CollectionsKt.map(parameters, p -> p.annotatedType.type);
	}

	public List<EAnnotatedType<?>> annotatedParameterTypes() {
		return CollectionsKt.map(parameters, p -> p.annotatedType);
	}
}
