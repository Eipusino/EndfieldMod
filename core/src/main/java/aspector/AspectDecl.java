package aspector;

import aspector.classes.BytecodeLoader;
import aspector.classes.ClassName;
import aspector.generate.ClassMaker;

public abstract class AspectDecl<T> {
	protected ClassMaker.AspectBuilder context;

	public AspectDecl(ClassMaker.AspectBuilder cont) {
		context = cont;
	}

	public abstract ClassName getClassName();

	public abstract byte[] getBytecode();

	public abstract Class<T> load(BytecodeLoader loader);
}
