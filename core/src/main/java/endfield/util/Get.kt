package endfield.util

import arc.func.Func
import arc.graphics.g2d.TextureRegion
import arc.struct.OrderedMap
import arc.struct.Seq
import mindustry.gen.Building
import mindustry.ui.Bar
import mindustry.world.Block
import mindustry.world.consumers.Consume

@JvmField
val consumeBuilder: FieldAccessor = Reflects.newFieldAccessor(Block::class.java.getDeclaredField("consumeBuilder"))
@JvmField
val barMap: FieldAccessor = Reflects.newFieldAccessor(Block::class.java.getDeclaredField("barMap"))
@JvmField
val icons: MethodAccessor = Reflects.newMethodAccessor(Block::class.java.getDeclaredMethod("icons"))

fun Block.consumeBuilder(): Seq<Consume> = consumeBuilder.getObject(this)
fun Block.barMap(): OrderedMap<String, Func<Building, Bar>> = barMap.getObject(this)
fun Block.icons(): Array<TextureRegion> = icons.invoke(this)