package endfield.math;

import arc.Core;
import arc.math.geom.Vec2;
import mindustry.Vars;

public final class Physics {
	/**
	 * @return the amount of a fluid that flows between 2 tanks.
	 */
	public static float fluidFlow(float fromPressure, float fromVolume, float toPressure, float toVolume, float density, float viscosity, float timeScl) {
		float flow = toVolume * toPressure;
		flow += fromVolume * fromPressure;
		flow /= (fromVolume + toVolume);
		flow -= fromPressure;
		flow *= -1f;
		flow *= fromVolume;
		flow *= density;
		flow /= Math.max(1, viscosity / timeScl);

		return flow;
	}

	public static Vec2 parallax(Vec2 pos, Vec2 reference, float height) {
		return pos.lerp(reference, -height / 48 * Vars.renderer.getDisplayScale());
	}

	public static Vec2 parallax(Vec2 pos, float height) {
		return parallax(pos, Core.camera.position, height);
	}
}
