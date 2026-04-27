package endfield.ui.markdown.elemdraw

import arc.scene.Element
import arc.scene.ui.ScrollPane
import arc.scene.ui.layout.Table
import arc.util.pooling.Pools
import endfield.ui.markdown.Markdown
import endfield.ui.markdown.RendererContext
import kotlin.math.min

class DrawTable : Markdown.MarkdownDraw(), Markdown.ActivityDrawer {
	companion object {
		private val defSlider = ScrollPane.ScrollPaneStyle()

		fun get(
			table: Table,
			sliderStyle: ScrollPane.ScrollPaneStyle,
		): DrawTable = Pools.obtain(DrawTable::class.java) { DrawTable() }.apply {
			this.table = table
			this.sliderStyle = sliderStyle
		}
	}

	var table: Table? = null
	var sliderStyle: ScrollPane.ScrollPaneStyle = defSlider

	private lateinit var pane: ScrollPane
	private var tableWidth = 0f
	private var tableHeight = 0f

	override val activeElement: Element get() = pane

	override fun reset() {
		super.reset()
		table = null
		sliderStyle = defSlider
	}

	override fun prefWidth(): Float = tableWidth
	override fun prefHeight(): Float = tableHeight

	override fun setup(scope: RendererContext.Scope) {
		val maxWidth = scope.boundX - scope.marginRight - offsetX

		pane = ScrollPane(table, sliderStyle).also { pane ->
			pane.isScrollingDisabledY = true
			pane.pack()

			tableWidth = min(pane.width, maxWidth)
			tableHeight = pane.height
		}
	}

	override fun draw(x: Float, y: Float) {}
}