package endfield.util.aspector;

import endfield.util.aspector.classes.BytecodeLoader;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.generate.ClassMaker;

public abstract class AspectDecl<T> {
	protected ClassMaker.AspectBuilder context;

	public AspectDecl(ClassMaker.AspectBuilder cont) {
		context = cont;
	}

	public abstract ClassName getClassName();

	public abstract byte[] getBytecode();

	public abstract Class<T> load(BytecodeLoader loader);
}
