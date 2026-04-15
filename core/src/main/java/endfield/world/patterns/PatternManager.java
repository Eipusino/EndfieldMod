package endfield.world.patterns;

import arc.Core;
import arc.Events;
import arc.math.geom.Point2;
import arc.struct.Bits;
import arc.struct.IntIntMap;
import arc.struct.IntSeq;
import arc.struct.IntSet;
import endfield.type.shape.Shape;
import endfield.util.CollectionList;
import endfield.util.CollectionObjectMap;
import mindustry.Vars;
import mindustry.game.EventType.TileOverlayChangeEvent;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.world.Block;
import mindustry.world.Tile;

public final class PatternManager {
	static final CollectionObjectMap<Block, CollectionObjectMap<Tile, PatternAnchor>> anchorMap = new CollectionObjectMap<>(Block.class, CollectionObjectMap.class);
	static final CollectionObjectMap<Block, IntIntMap> blockToAnchorMap = new CollectionObjectMap<>(Block.class, IntIntMap.class);
	static final CollectionObjectMap<Block, int[]> shapeOffsets = new CollectionObjectMap<>(Block.class, int[].class);

	static final CollectionObjectMap<Block, IntSet> dirtyBlocks = new CollectionObjectMap<>(Block.class, IntSet.class);
	static final CollectionObjectMap<Block, Bits> blocksToResolve = new CollectionObjectMap<>(Block.class, Bits.class);
	static final IntSet dirtyChunks = new IntSet();
	static final CollectionObjectMap<Block, Bits> visitedPool = new CollectionObjectMap<>(Block.class, Bits.class);
	static final CollectionObjectMap<Block, Bits> claimedPool = new CollectionObjectMap<>(Block.class, Bits.class);
	static final CollectionObjectMap<Block, Bits> processedPool = new CollectionObjectMap<>(Block.class, Bits.class);
	static boolean updateQueued = false;
	static boolean initialized = false;

	public static void register() {
		Events.on(TileOverlayChangeEvent.class, event -> {
			if (event.overlay instanceof Patterned p) {
				updateAround(event.tile, p);
				for (int i = 0; i < 4; i++) {
					Tile near = event.tile.nearby(i);
					if (near != null) updateAround(near, p);
				}
			}
			if (event.previous instanceof Patterned p) {
				updateAround(event.tile, p);
				for (int i = 0; i < 4; i++) {
					Tile near = event.tile.nearby(i);
					if (near != null) updateAround(near, p);
				}
			}
		});

		Events.on(WorldLoadEvent.class, event -> rebuild());
	}

	static IntIntMap getAnchorMap(Block b) {
		IntIntMap map = blockToAnchorMap.get(b);
		if (map == null) blockToAnchorMap.put(b, map = new IntIntMap());
		return map;
	}

	static CollectionObjectMap<Tile, PatternAnchor> getAnchorObjectMap(Block b) {
		CollectionObjectMap<Tile, PatternAnchor> map = anchorMap.get(b);
		if (map == null) anchorMap.put(b, map = new CollectionObjectMap<>(Tile.class, PatternAnchor.class));
		return map;
	}

	static int[] getShapeOffsets(Patterned p) {
		Block b = (Block) p;
		int[] offsets = shapeOffsets.get(b);
		if (offsets != null) return offsets;

		CollectionList<Point2> points = new CollectionList<>(Patterned.class);
		p.getPattern().shape.each((x, y) -> {
			if (p.getPattern().shape.get(x, y)) points.add(new Point2(x, y));
		});

		int width = Vars.world.width();
		points.sort((a, b1) -> {
			int i1 = a.x + a.y * width;
			int i2 = b1.x + b1.y * width;
			return Integer.compare(i1, i2);
		});

		int[] result = new int[points.size];
		for (int i = 0; i < points.size; i++) {
			Point2 p2 = points.get(i);
			result[i] = Point2.pack(p2.x, p2.y);
		}

		shapeOffsets.put(b, result);
		return result;
	}

	static Bits getBits(CollectionObjectMap<Block, Bits> pool, Block b) {
		Bits bits = pool.get(b);
		int size = Vars.world.width() * Vars.world.height();
		if (bits == null || bits.numBits() < size) {
			pool.put(b, bits = new Bits(size));
		}
		return bits;
	}

	public static void rebuild() {
		if (Vars.world == null || Vars.world.tiles == null) return;
		anchorMap.each((b, map) -> map.clear());
		blockToAnchorMap.each((b, map) -> map.clear());
		shapeOffsets.clear();
		dirtyBlocks.clear();
		blocksToResolve.each((b, bits) -> bits.clear());
		dirtyChunks.clear();
		updateQueued = false;
		initialized = true;

		int size = Vars.world.width() * Vars.world.height();
		for (int i = 0; i < size; i++) {
			Tile tile = Vars.world.tiles.geti(i);
			if (tile.floor() instanceof Patterned p) getBits(blocksToResolve, (Block) p).set(i);
			if (tile.block() instanceof Patterned p) getBits(blocksToResolve, (Block) p).set(i);
			if (tile.overlay() instanceof Patterned p) getBits(blocksToResolve, (Block) p).set(i);
		}

		claimedPool.each((b, bits) -> bits.clear());
		resolveTiles();

		if (!Vars.headless && Vars.renderer != null && Vars.renderer.blocks != null) {
			Vars.renderer.blocks.floor.reload();
		}
	}

	public static void updateAround(Tile tile, Patterned p) {
		if (tile == null || p == null || Vars.world.isGenerating() || Vars.world.tiles == null) return;
		Block b = (Block) p;
		IntSet set = dirtyBlocks.get(b);
		if (set == null) dirtyBlocks.put(b, set = new IntSet());

		if (set.add(tile.array()) && !updateQueued) {
			updateQueued = true;
			Core.app.post(PatternManager::processDirtyTiles);
		}
	}

	static void processDirtyTiles() {
		if (!initialized) rebuild();
		if (dirtyBlocks.isEmpty()) {
			updateQueued = false;
			return;
		}

		CollectionObjectMap<Block, IntSeq> dirtyCopy = new CollectionObjectMap<>(Block.class, IntSeq.class);
		for (var entry : dirtyBlocks) {
			IntSeq seq = new IntSeq();
			var it = entry.value.iterator();
			while (it.hasNext) seq.add(it.next());
			dirtyCopy.put(entry.key, seq);
		}
		dirtyBlocks.clear();
		updateQueued = false;

		visitedPool.each((b, bits) -> bits.clear());
		blocksToResolve.each((b, bits) -> bits.clear());
		dirtyChunks.clear();

		for (var entry : dirtyCopy) {
			Block pBlock = entry.key;
			IntSeq dirty = entry.value;
			Patterned p = (Patterned) pBlock;
			Bits toResolve = getBits(blocksToResolve, pBlock);

			for (int i = 0; i < dirty.size; i++) {
				Tile tile = Vars.world.tiles.geti(dirty.get(i));
				if (tile != null) handleDirty(tile, p, toResolve);
			}
		}

		if (!blocksToResolve.isEmpty()) {
			resolveTiles();

			if (!Vars.headless && Vars.renderer != null && Vars.renderer.blocks != null) {
				var it = dirtyChunks.iterator();
				while (it.hasNext) {
					int packed = it.next();
					Vars.renderer.blocks.floor.recacheTile(Point2.x(packed) * 30, Point2.y(packed) * 30);
				}
			}
		}

		if (!dirtyBlocks.isEmpty() && !updateQueued) {
			updateQueued = true;
			Core.app.post(PatternManager::processDirtyTiles);
		}
	}

	static void markChunkDirty(int x, int y) {
		dirtyChunks.add(Point2.pack(x / 30, y / 30));
	}

	static void handleDirty(Tile tile, Patterned p, Bits toResolve) {
		Block pBlock = (Block) p;
		Bits visited = getBits(visitedPool, pBlock);
		if (visited.get(tile.array())) return;

		findAndMarkContiguous(tile, p, toResolve);
	}

	static void findAndMarkContiguous(Tile startTile, Patterned patterned, Bits toResolve) {
		Block pBlock = (Block) patterned;
		Bits visited = getBits(visitedPool, pBlock);

		IntSeq stack = new IntSeq();
		stack.add(startTile.array());
		int width = Vars.world.width(), height = Vars.world.height();

		while (stack.size > 0) {
			int popped = stack.pop();
			int x = popped % width, y = popped / width;
			if (visited.get(popped)) continue;

			int x1 = x;
			while (x1 >= 0 && hasPatterned(Vars.world.tiles.geti(x1 + y * width), patterned) && !visited.get(x1 + y * width))
				x1--;
			x1++;
			boolean spanAbove = false, spanBelow = false;
			while (x1 < width && hasPatterned(Vars.world.tiles.geti(x1 + y * width), patterned) && !visited.get(x1 + y * width)) {
				int pos = x1 + y * width;
				visited.set(pos);
				toResolve.set(pos);

				if (!spanAbove && y > 0 && hasPatterned(Vars.world.tiles.geti(x1 + (y - 1) * width), patterned) && !visited.get(x1 + (y - 1) * width)) {
					stack.add(x1 + (y - 1) * width);
					spanAbove = true;
				} else if (spanAbove && !(hasPatterned(Vars.world.tiles.geti(x1 + (y - 1) * width), patterned) && !visited.get(x1 + (y - 1) * width))) {
					spanAbove = false;
				}
				if (!spanBelow && y < height - 1 && hasPatterned(Vars.world.tiles.geti(x1 + (y + 1) * width), patterned) && !visited.get(x1 + (y + 1) * width)) {
					stack.add(x1 + (y + 1) * width);
					spanBelow = true;
				} else if (spanBelow && y < height - 1 && !(hasPatterned(Vars.world.tiles.geti(x1 + (y + 1) * width), patterned) && !visited.get(x1 + (y + 1) * width))) {
					spanBelow = false;
				}
				x1++;
			}
		}
	}

	static void resolveTiles() {
		processedPool.each((b, bits) -> bits.clear());

		for (var entry : blocksToResolve) {
			Block pBlock = entry.key;
			Bits toResolve = entry.value;
			Patterned p = (Patterned) pBlock;
			Bits processed = getBits(processedPool, pBlock);
			Bits claimed = getBits(claimedPool, pBlock);
			IntIntMap map = getAnchorMap(pBlock);
			CollectionObjectMap<Tile, PatternAnchor> aMap = getAnchorObjectMap(pBlock);
			int[] offsets = getShapeOffsets(p);

			if (offsets.length == 0) continue;

			for (int i = toResolve.nextSetBit(0); i >= 0; i = toResolve.nextSetBit(i + 1)) {
				claimed.clear(i);
				aMap.remove(Vars.world.tiles.geti(i));
			}

			int firstOffsetX = Point2.x(offsets[0]);
			int firstOffsetY = Point2.y(offsets[0]);

			for (int i = toResolve.nextSetBit(0); i >= 0; i = toResolve.nextSetBit(i + 1)) {
				if (claimed.get(i)) continue;

				Tile tile = Vars.world.tiles.geti(i);
				Tile potentialAnchor = Vars.world.tile(tile.x - firstOffsetX, tile.y - firstOffsetY);
				if (potentialAnchor != null) {
					int apos = potentialAnchor.array();
					if (!processed.get(apos)) {
						processed.set(apos);
						if (isPatternInternal(p, potentialAnchor)) {
							addAnchor(p, potentialAnchor);
						}
					}
				}
			}

			int width = Vars.world.width();
			for (int i = toResolve.nextSetBit(0); i >= 0; i = toResolve.nextSetBit(i + 1)) {
				if (!claimed.get(i)) {
					if (map.remove(i, -1) != -1) {
						markChunkDirty(i % width, i / width);
					}
				}
			}
		}
	}

	static void addAnchor(Patterned p, Tile anchor) {
		Block pBlock = (Block) p;
		int anchorPos = anchor.array();
		int[] offsets = getShapeOffsets(p);
		Bits claimed = getBits(claimedPool, pBlock);
		IntIntMap map = getAnchorMap(pBlock);
		CollectionObjectMap<Tile, PatternAnchor> aMap = getAnchorObjectMap(pBlock);

		if (!aMap.containsKey(anchor)) {
			aMap.put(anchor, new PatternAnchor(anchor, p));
		}

		int width = Vars.world.width();
		for (int offset : offsets) {
			int tx = anchor.x + Point2.x(offset);
			int ty = anchor.y + Point2.y(offset);
			int mpos = tx + ty * width;
			claimed.set(mpos);

			if (map.get(mpos, -1) != anchorPos) {
				map.put(mpos, anchorPos);
				markChunkDirty(tx, ty);
			}
		}
	}

	static boolean isPatternInternal(Patterned patterned, Tile anchor) {
		Block pBlock = (Block) patterned;
		int[] offsets = getShapeOffsets(patterned);
		Bits claimed = claimedPool.get(pBlock);

		int width = Vars.world.width(), height = Vars.world.height();
		for (int offset : offsets) {
			int tx = anchor.x + Point2.x(offset);
			int ty = anchor.y + Point2.y(offset);
			if (tx < 0 || tx >= width || ty < 0 || ty >= height) return false;
			int pos = tx + ty * width;
			Tile other = Vars.world.tiles.geti(pos);
			if (!hasPatterned(other, patterned)) return false;
			if (claimed.get(pos)) return false;
		}
		return true;
	}

	public static boolean isPatternComplete(Patterned patterned, Tile anchor) {
		if (!(patterned instanceof Block)) return false;
		int[] offsets = getShapeOffsets(patterned);
		for (int offset : offsets) {
			Tile other = Vars.world.tile(anchor.x + Point2.x(offset), anchor.y + Point2.y(offset));
			if (!hasPatterned(other, patterned)) return false;
			if (getAnchor(other, patterned) != anchor) return false;
		}
		return true;
	}

	public static Tile getAnchor(Tile tile, Patterned p) {
		if (tile == null || !(p instanceof Block pBlock)) return null;
		IntIntMap map = blockToAnchorMap.get(pBlock);
		if (map == null) return null;
		int anchorPos = map.get(tile.array(), -1);
		return anchorPos == -1 ? null : Vars.world.tiles.geti(anchorPos);
	}

	public static boolean hasPatterned(Tile tile, Patterned p) {
		if (tile == null) return false;
		Block b = (Block) p;
		if (tile.overlay() == b) return true;
		if (tile.floor() == b) return true;
		return tile.block() == b;
	}

	static class PatternAnchor {
		public final Tile tile;
		public final Patterned patterned;
		public final Shape shape;

		public PatternAnchor(Tile tile, Patterned patterned) {
			this.tile = tile;
			this.patterned = patterned;
			this.shape = patterned.getPattern().shape;
		}
	}
}
