package endfield.world.blocks.power;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.gl.FrameBuffer;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.entities.Effect;
import mindustry.game.EventType.DisposeEvent;
import mindustry.game.EventType.ResizeEvent;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.blocks.power.PowerNode;

public class ImpulseNode extends PowerNode {
	// TODO bring all buffers to their own class
	public static FrameBuffer lightningBuffer = Core.graphics == null ? null : new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
	public static Seq<Runnable> lightningBufferDrawCalls = new Seq<>(Runnable.class);

	static {
		Events.on(ResizeEvent.class, e -> {
			lightningBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
		});
		Events.run(Trigger.draw, () -> {
			if (!lightningBufferDrawCalls.isEmpty()) {
				var copy = lightningBufferDrawCalls.copy();
				lightningBufferDrawCalls.clear();

				Draw.draw(Layer.power, () -> {
					Draw.flush();
					lightningBuffer.begin(Color.clear);
					copy.each(Runnable::run);
					lightningBuffer.end();

					Draw.alpha(Renderer.laserOpacity);
					Draw.rect(
							Draw.wrap(lightningBuffer.getTexture()),
							Core.camera.position.x,
							Core.camera.position.y,
							Core.camera.width,
							-Core.camera.height
					);
					Draw.reset();
					Draw.flush();
				});
			}
		});
		Events.on(DisposeEvent.class, e -> lightningBuffer.dispose());
	}

	public int effectTimer = timers++;
	public float effectTime = 20;
	public Effect lightningEffect = new Effect(10, e -> {
		if (!(e.data instanceof Seq)) return;
		Seq<Vec2> lines = e.data();
		float fin = e.fin();
		float fout = e.fout();
		Color color = e.color;
		lightningBufferDrawCalls.add(() -> {
			Lines.stroke(3f * fout);
			Draw.color(color, Color.white, fin);

			for (int i = 0; i < lines.size - 1; i++) {
				Vec2 cur = lines.get(i);
				Vec2 next = lines.get(i + 1);

				Lines.line(cur.x, cur.y, next.x, next.y, false);
			}

			for (Vec2 p : lines) {
				Fill.circle(p.x, p.y, Lines.getStroke() / 2f);
			}
		});
	});

	public ImpulseNode(String name) {
		super(name);
	}

	public class ImpulseNodeBuild extends PowerNodeBuild {
		@Override
		public void draw() {
			Draw.rect(region, x, y, this.drawrot());

			if (Mathf.zero(Renderer.laserOpacity) || isPayload()) return;

			Draw.z(Layer.power);
			setupColor(power.graph.getSatisfaction());

			boolean shouldLightning = timer.get(effectTimer, effectTime);
			for (int i = 0; i < power.links.size; i++) {
				Building link = Vars.world.build(power.links.get(i));

				if (!linkValid(this, link)) continue;

				if (link.block instanceof PowerNode && link.id >= id) continue;

				Draw.alpha(Renderer.laserOpacity / (Vars.renderer.enableEffects ? 2f : 1f));
				drawLaser(x, y, link.x, link.y, size, link.block.size);

				if (shouldLightning && power.graph.getSatisfaction() > 0) {
					float angle1 = Angles.angle(x, y, link.x, link.y),
							vx = Mathf.cosDeg(angle1), vy = Mathf.sinDeg(angle1),
							len1 = size * Vars.tilesize / 2f - 1.5f, len2 = link.block.size * Vars.tilesize / 2f - 1.5f;

					lightning(
							x + vx * len1,
							y + vy * len1,
							link.x - vx * len2,
							link.y - vy * len2,
							2,
							Mathf.random(-8f, 8f),
							laserColor2.cpy().lerp(laserColor1, power.graph.getSatisfaction()),
							lightningEffect
					);
				}
			}

			Draw.reset();
		}

		/**
		 * Original code from Project HPL[<a href="https://github.com/HPL-Team/Project-HPL">...</a>]
		 */
		public void lightning(float x1, float y1, float x2, float y2, int iterations, float rndScale, Color c, Effect e) {
			Seq<Vec2> lines = new Seq<>(Vec2.class);
			boolean swap = Math.abs(y1 - y2) < Math.abs(x1 - x2);
			if (swap) {
				lines.add(new Vec2(y1, x1));
				lines.add(new Vec2(y2, x2));
			} else {
				lines.add(new Vec2(x1, y1));
				lines.add(new Vec2(x2, y2));
			}
			for (int i = 0; i < iterations; i++) {
				for (int j = 0; j < lines.size - 1; j += 2) {
					Vec2 v1 = lines.get(j), v2 = lines.get(j + 1);
					Vec2 v = new Vec2((v1.x + v2.x) / 2, ((v1.y + v2.y) / 2));
					float ang = (v2.angle(v1) + 90f) * Mathf.degRad;
					float sin = Mathf.sin(ang), cos = Mathf.cos(ang);
					float rnd = Mathf.random(rndScale);
					v.x += rnd * sin;
					v.y += rnd * cos;
					lines.insert(j + 1, v);
				}
			}
			if (swap) {
				for (int i = 0; i < lines.size; i++) {
					Vec2 v = lines.get(i);
					float px = v.x;
					v.x = v.y;
					v.y = px;
				}
			}
			e.at(x1, y1, 0f, c, lines);
		}
	}
}
