package endfield.type.shape;

import arc.Core;
import arc.func.Intc2;
import arc.graphics.g2d.PixmapRegion;
import arc.util.Log;
import endfield.graphics.Pixmaps2;
import endfield.util.BitWordList;

public class CustomPatternShape implements Shape {
	public final String maskName;
	private int width = 1;
	private int height = 1;
	private BitWordList blocks;
	private boolean built = false;

	public CustomPatternShape(String name) {
		maskName = name;
		blocks = new BitWordList(1, BitWordList.WordLength.two);
		blocks.set(0, 1);
	}

	@Override
	public void load() {
		if (built) return;

		PixmapRegion region = Core.atlas.getPixmap(Core.atlas.find(maskName));

		if (region == null) {
			Log.err("Pixmap for CustomPatternShape is null for mask: @", maskName);
			return;
		}

		width = region.width;
		height = region.height;
		blocks = new BitWordList(width * height, BitWordList.WordLength.two);

		Pixmaps2.readTexturePixels(region, (color, index) -> {
			int x = index % width;
			int y_pix = index / width;
			// Convert Pixmap Y (top-down) to World Y (bottom-up)
			int y_world = (height - 1) - y_pix;
			int newIndex = x + y_world * width;

			switch (color) {
				case 2815 -> blocks.set(newIndex, 3); // blue, center
				case 255 -> blocks.set(newIndex, 2); // black, part of shape
				default -> blocks.set(newIndex, 1);
			}
		});
		built = true;
	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height;
	}

	@Override
	public boolean get(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) return false;
		byte id = blocks.get(x + y * width);
		return id == 2 || id == 3;
	}

	@Override
	public void each(Intc2 consumer) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (get(x, y)) consumer.get(x, y);
			}
		}
	}
}
