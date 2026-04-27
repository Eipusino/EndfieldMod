package endfield.ui.markdown.elemdraw

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.util.pooling.Pools
import endfield.ui.markdown.Markdown
import endfield.ui.markdown.RendererContext

open class DrawThematicBreak : Markdown.MarkdownDraw() {
	companion object {
		fun get(
			color: Color,
			stroke: Float
		): DrawThematicBreak = Pools.obtain(DrawThematicBreak::class.java) { DrawThematicBreak() }.apply {
			this.color = color
			this.stroke = stroke
		}
	}

	var color: Color = Color.white
	var stroke: Float = 0f

	private var realWidth: Float = 0f

	override fun reset() {
		super.reset()
		color = Color.white
		stroke = 0f
	}

	override fun prefWidth(): Float = realWidth
	override fun prefHeight(): Float = stroke

	override fun setup(scope: RendererContext.Scope) {
		realWidth = scope.boundX - scope.marginRight - offsetX
	}

	override fun draw(x: Float, y: Float) {
		Draw.color(color)
		Fill.rect(
			x + offsetX + width / 2,
			y - offsetY - height / 2,
			width,
			height
		)
	}
}