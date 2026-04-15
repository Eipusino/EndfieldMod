package endfield.world.blocks.environment;

import arc.struct.Seq;
import endfield.type.shape.CustomShape;
import endfield.util.Constant;

public interface MultiPropI {
	Seq<CustomShape> shapes();

	default Runnable removed(MultiPropGroup from) {
		return Constant.RUNNABLE_NOTHING;
	}
}
