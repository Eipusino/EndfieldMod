package endfield.ui.markdown.url;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.Mesh;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.graphics.Texture;
import arc.graphics.VertexAttribute;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import endfield.ui.markdown.UrlHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class AtlasHandler implements UrlHandler {
	static final Shader shader = new Shader("""
			attribute vec4 a_position;
			attribute vec2 a_texCoord0;
			varying vec2 v_texCoord;
			void main() {
				gl_Position = a_position;
				v_texCoord = a_texCoord0;
			}
			""", """
			varying vec2 v_texCoord;
			uniform sampler2D u_texture;
			void main() {
				gl_FragColor = texture2D(u_texture, v_texCoord);
			}
			""");

	final Mesh mesh = new Mesh(false, 4, 0, VertexAttribute.position, VertexAttribute.texCoords);
	final FrameBuffer buffer = new FrameBuffer();
	final PixmapIO.PngWriter pixmapWriter = new PixmapIO.PngWriter();

	@Override
	public List<String> matchedSchemes() {
		return List.of("atlas");
	}

	@Override
	public void openUrl(String url) {
		throw new UnsupportedOperationException("Cannot open a atlas url directly.");
	}

	@Override
	public ResourceHandle getResource(String url) {
		String name = url.replaceFirst("atlas:", "");
		AtlasRegion region = Core.atlas.find(name);
		float u = region.u;
		float v = region.v;
		float u2 = region.u2;
		float v2 = region.v2;
		Texture texture = region.texture;
		Pixmap pixmap = new Pixmap(region.width, region.height);
		ByteBuffer pixels = pixmap.pixels;

		float[] vertices = new float[]{
				-1f,-1f, u, v,
				1f, -1f, u2, v,
				1f,  1f, u2, v2,
				-1f, 1f, u, v2
		};
		mesh.setVertices(vertices);

		buffer.resize(region.width, region.height);
		buffer.begin(Color.clear);
		shader.bind();
		shader.setUniformi("u_texture", 0);
		texture.bind();
		mesh.render(shader, Gl.triangleFan);
		Gl.readPixels(0, 0, region.width, region.height, Gl.rgba, Gl.unsignedByte, pixels);
		buffer.end();

		try (ByteArrayOutputStream res = new ByteArrayOutputStream()) {
			pixmapWriter.write(res, pixmap.flipY());

			return new ByteArrayHandle(res.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
