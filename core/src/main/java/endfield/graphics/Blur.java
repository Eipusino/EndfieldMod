package endfield.graphics;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.Pixmap;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;

import static endfield.graphics.Shaders2.base;
import static endfield.graphics.Shaders2.blur;

public class Blur {
	final FrameBuffer stencil = new FrameBuffer(Pixmap.Format.rgba8888, 2, 2, false, false);
	final FrameBuffer pingpong1 = new FrameBuffer(Pixmap.Format.rgba8888, 2, 2, false, true);
	final FrameBuffer pingpong2 = new FrameBuffer(Pixmap.Format.rgba8888, 2, 2, false, true);

	public float blurSpace = 1.5f;
	public int blurScl = 2;
	public int blurLevel = 2;

	public void drawBlur(Runnable block) {
		stencil.resize(Core.graphics.getWidth() / blurScl, Core.graphics.getHeight() / blurScl);
		pingpong1.resize(Core.graphics.getWidth() / blurScl, Core.graphics.getHeight() / blurScl);
		pingpong2.resize(Core.graphics.getWidth() / blurScl, Core.graphics.getHeight() / blurScl);

		stencil.begin(Color.clear);
		block.run();
		stencil.end();

		Gl.enable(Gl.stencilTest);
		Gl.stencilMask(0xff);
		Gl.stencilFunc(Gl.always, 1, 0xff);
		Gl.stencilOp(Gl.keep, Gl.keep, Gl.replace);

		pingpong1.begin();
		Gl.clear(Gl.stencilBufferBit | Gl.colorBufferBit);
		block.run();
		pingpong1.end();

		pingpong2.begin();
		Gl.clear(Gl.stencilBufferBit | Gl.colorBufferBit);
		block.run();
		pingpong2.end();

		Gl.disable(Gl.stencilTest);

		render();
	}

	void render() {
		Blending.disabled.apply();

		ScreenSampler.toBuffer(pingpong1);
		ScreenSampler.toBuffer(pingpong2);

		blur.bind();
		blur.apply();
		blur.setUniformi("u_stencil", 0);
		blur.setUniformi("u_sample", 1);
		blur.setUniformf("u_screenSize", Core.graphics.getWidth(), Core.graphics.getHeight());

		Gl.enable(Gl.stencilTest);
		Gl.stencilMask(0x00);
		Gl.stencilFunc(Gl.equal, 1, 0xff);
		Gl.stencilOp(Gl.keep, Gl.keep, Gl.keep);
		for (int n = 0; n < blurLevel; n++) {
			pingpong2.begin();
			blur.bind();
			blur.setUniformf("u_blurDirection", blurSpace, 0f);
			stencil.getTexture().bind(0);
			pingpong1.getTexture().bind(1);
			Draw.blit(blur);
			pingpong2.end();

			pingpong1.begin();
			blur.bind();
			blur.setUniformf("u_blurDirection", 0f, blurSpace);
			stencil.getTexture().bind(0);
			pingpong2.getTexture().bind(1);
			Draw.blit(blur);
			pingpong1.end();
		}
		Gl.disable(Gl.stencilTest);

		pingpong1.blit(base);

		Blending.normal.apply();
	}
}