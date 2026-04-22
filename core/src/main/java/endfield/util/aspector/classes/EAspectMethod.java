package endfield.util.aspector.classes;

import endfield.util.aspector.Using;

import java.util.List;

public class EAspectMethod extends EMethod {
	public final Using using;

	public EAspectMethod(ClassDecl<?> decl, String name, List<Parameter> params, EAnnotatedType<?> annoRType, int flag, Using us, List<EAnnotation> annos) {
		super(decl, name, params, annoRType, flag, annos);
		using = us;
	}
}
