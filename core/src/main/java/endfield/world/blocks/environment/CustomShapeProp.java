package endfield.world.blocks.environment;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import endfield.content.Fx2;
import endfield.graphics.Pixmaps2;
import endfield.type.shape.CustomShape;
import endfield.util.BitWordList;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.graphics.MultiPacker;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Prop;

/**
 * A breakable prop with a custom shape.
 * Has a tile defined as a center (it's a corner) that is the one further in the tile index.
 * Remember, when making the mask, it is important to always paint the corner of the sprite with 00000AFF, the other occupied tiles with black and the rest should be transparent.
 */
public class CustomShapeProp extends Prop implements MultiPropI {
	public TextureRegion[] shadows, shapeRegions, underRegions;

	/**
	 * shape of this multiblock represented as offsets from the center.
	 * make all offsets connected to each other in least one of the 4 cardinal directions.
	 *
	 * @apiNote DO NOT SET MANUALLY
	 */
	public Seq<CustomShape> shapes = new Seq<>(CustomShape.class);

	/**
	 * Drawing offset for a shape. This variable should be the same size or larger than the shapes Seq!
	 *
	 * @apiNote Will throw ArrayIndexOutOfBoundsException if too small
	 */
	public Vec2[] spriteOffsets;

	public Effect deconstructEffect = Fx2.breakShapedProp;

	/**
	 * draws a region under the sprite
	 */
	public boolean drawUnder;

	/**
	 * if true, the shape can be flipped vertically and horizontally on the prop placer
	 * vertical flips(in shape index):
	 * 1-4
	 * 2-3
	 * 5-6
	 * 7-8
	 * horizontal flips(in shape index):
	 * 1-2
	 * 3-4
	 * 5-7
	 * 6-8
	 * I don't really know a better way to word this better
	 */
	public boolean canMirror;

	/**
	 * if true, the shape's region will be rotated in the same way that trees are rotated, also applies to shadow and under regions
	 */
	public boolean rotateRegions;
	/**
	 * the half-cone that the top region can rotate based on tile,
	 */
	public float rotateRegionMagnitude = 7.5f;

	public CustomShapeProp(String name) {
		super(name);
		customShadow = true;
		alwaysReplace = false;
		breakEffect = Fx.breakProp;
	}

	public static CustomShape createShape(TextureRegion region) {
		PixmapRegion pixmap = Core.atlas.getPixmap(region);
		BitWordList list = new BitWordList(pixmap.width * pixmap.height, BitWordList.WordLength.two);

		Pixmaps2.readTexturePixels(pixmap, (color, index) -> {
			switch (color) {
				case 2815 -> list.set(index, 3);
				case 255 -> list.set(index, 2);
				default -> list.set(index, 1);
			}
		});

		return new CustomShape(pixmap.width, pixmap.height, list);
	}

	@Override
	public void drawBase(Tile tile) {
		MultiPropGroup multiProp = CustomShapePropProcess.instance.multiProps.find(multiPropGroup -> multiPropGroup.center == tile);
		if (multiProp != null) {
			Draw.z(layer);
			if (drawUnder) Draw.rect(underRegions[multiProp.shape],
					tile.worldx() + spriteOffsets[multiProp.shape].x,
					tile.worldy() + spriteOffsets[multiProp.shape].y,
					rotateRegions ? Mathf.randomSeed(tile.pos() + 1, 0, 4) * 90f : 0f
			);
			Draw.rect(variantRegions[multiProp.shape],
					tile.worldx() + spriteOffsets[multiProp.shape].x,
					tile.worldy() + spriteOffsets[multiProp.shape].y,
					rotateRegions ? Mathf.randomSeed(tile.pos(), -rotateRegionMagnitude, rotateRegionMagnitude) : 0f
			);
		}
	}

	@Override
	public void drawShadow(Tile tile) {
		MultiPropGroup multiProp = CustomShapePropProcess.instance.multiProps.find(multiPropGroup -> multiPropGroup.center == tile);
		if (multiProp != null) {
			Draw.rect(shadows[multiProp.shape],
					tile.worldx() + spriteOffsets[multiProp.shape].x,
					tile.worldy() + spriteOffsets[multiProp.shape].y
			);
		}
	}

	@Override
	public void createIcons(MultiPacker packer) {
		super.createIcons(packer);

		shapeRegions = new TextureRegion[variants];

		for (int i = 0; i < variants; i++) {
			shapeRegions[i] = Core.atlas.find(name + "-shape" + (i + 1), "omaloon-shape-err");
			shapes.addUnique(createShape(shapeRegions[i]));
		}
	}

	@Override
	public void load() {
		super.load();

		shadows = new TextureRegion[variants];
		underRegions = new TextureRegion[variants];

		for (int i = 0; i < variants; i++) {
			shadows[i] = Core.atlas.find(name + "-shadow" + (i + 1));
			underRegions[i] = Core.atlas.find(name + "-under" + (i + 1));
		}
	}

	@Override
	public Runnable removed(MultiPropGroup from) {
		return () -> deconstructEffect.at(
				from.center.worldx() + spriteOffsets[from.shape].x,
				from.center.worldy() + spriteOffsets[from.shape].y,
				0, mapColor, from
		);
	}

	@Override
	public Seq<CustomShape> shapes() {
		return shapes;
	}
}
