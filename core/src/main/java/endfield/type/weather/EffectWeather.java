package endfield.type.weather;

import arc.math.Mathf;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.WeatherState;

public class EffectWeather extends SpawnWeather {
	public Effect weatherFx = Fx.none;

	public EffectWeather(String name) {
		super(name);
		useWindVector = true;
	}

	@Override
	public void spawn(WeatherState state, float x, float y) {
		weatherFx.at(x, y, Mathf.angle(state.windVector.x, state.windVector.y));
	}
}
