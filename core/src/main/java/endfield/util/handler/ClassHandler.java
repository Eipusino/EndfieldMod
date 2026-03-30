package endfield.util.handler;

import dynamilize.DynamicMaker;
import dynamilize.classmaker.AbstractClassGenerator;

public interface ClassHandler {
	ClassHandler newInstance(Class<?> modMain);

	AbstractClassGenerator getGenerator();

	DynamicMaker getDynamicMaker();

	//AbstractFileClassLoader currLoader();

	void finishGenerate();
}
