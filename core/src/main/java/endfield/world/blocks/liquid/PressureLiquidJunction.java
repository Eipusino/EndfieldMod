package endfield.world.blocks.liquid;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import endfield.world.blocks.GenericPressureBlock;
import endfield.world.blocks.HasPressure;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import org.jetbrains.annotations.Nullable;

public class PressureLiquidJunction extends GenericPressureBlock {
	public DrawBlock drawer = new DrawDefault();

	public TextureRegion side1, side2;

	public PressureLiquidJunction(String name) {
		super(name);
		update = true;
		destructible = true;
	}

	@Override
	public void load() {
		super.load();
		drawer.load(this);
		side1 = Core.atlas.find(name + "-side1");
		side2 = Core.atlas.find(name + "-side2");
	}

	@Override
	protected TextureRegion[] icons() {
		return new TextureRegion[]{fullIcon};
	}

	public class PressureLiquidJunctionBuild extends GenericPressureBlockBuild {
		@Override
		public void draw() {
			drawer.draw(this);

			for (int i = 0; i < 4; i++) {
				Building neighbor = nearby(i);
				Building opposite = nearby((i + 2) % 4);

				if (!(neighbor instanceof HasPressure) || !(opposite instanceof HasPressure)) {
					Draw.rect(i >= 2 ? side2 : side1, x, y, i * 90);
				}
			}
		}

		@Override
		public void drawLight() {
			super.drawLight();
			drawer.drawLight(this);
		}

		@Override
		public HasPressure getFluidDestination(HasPressure source, @Nullable Liquid fluid) {
			if (!enabled) return this;

			int dir = (((Building) source).relativeTo(tile.x, tile.y) + 4) % 4;
			HasPressure next = nearby(dir) instanceof HasPressure ? (HasPressure) nearby(dir) : null;
			if (next == null) {
				return this;
			}
			return next.getFluidDestination(this, fluid);
		}

		@Override
		public boolean acceptsFluid(HasPressure from, @Nullable Liquid liquid, float amount) {
			return false;
		}

		@Override
		public boolean connects(HasPressure to) {
			return false;
		}

		@Override
		public Seq<HasPressure> connections() {
			return Seq.with();
		}

		@Override
		public boolean outputsFluid(HasPressure to, @Nullable Liquid liquid, float amount) {
			return false;
		}
	}
}
