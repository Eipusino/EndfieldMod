package endfield.world.blocks;

import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.gen.Buildingc;
import mindustry.world.Block;
import mindustry.world.Tile;

public interface IBuilding extends Buildingc {
	Block block();

	Tile tile();

	float efficiency();

	Seq<Building> proximity();
}
