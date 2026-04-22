package endfield.util.aspector.classes;

import kotlin.collections.CollectionsKt;

import java.util.List;

public class EConstructor<T> extends EExecutable {
	MethodSignature descriptor;

	public EConstructor(ClassDecl<T> decl, List<Parameter> params, int flag, List<EAnnotation> annos) {
		super(decl, "<init>", params, flag, annos);
	}

	@Override
	public final MethodSignature descriptor() {
		if (descriptor == null) descriptor = new MethodSignature("<init>", CollectionsKt.map(parameters, p -> p.annotatedType.type.name), ClassName.V);

		return descriptor;
	}
}
