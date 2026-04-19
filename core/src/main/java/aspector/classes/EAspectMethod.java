package aspector.classes;

import aspector.Using;

import java.lang.annotation.Annotation;
import java.util.List;

public class EAspectMethod extends EMethod {
	public final Using using;

	public EAspectMethod(ClassDecl<?> decl, String name, List<Parameter> params, AnnotatedType<?> annoRType, int flag, Using us, List<EAnnotation> annos) {
		super(decl, name, params, annoRType, flag, annos);
		using = us;
	}
}
