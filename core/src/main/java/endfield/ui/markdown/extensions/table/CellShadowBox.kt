package endfield.ui.markdown.extensions.table

import endfield.ui.markdown.Markdown
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.node.CustomNode
import org.commonmark.node.Node

class CellShadowBox(
	val shadowedCell: TableCell,
	val cellBox: Markdown.Box
) : CustomNode() {
	init {
		sourceSpans = shadowedCell.sourceSpans.toList()
	}

	override fun getParent(): Node? = null
	override fun getNext(): Node? = null
	override fun getPrevious(): Node? = null

	override fun getFirstChild(): Node? {
		return shadowedCell.firstChild
	}

	override fun getLastChild(): Node? {
		return shadowedCell.lastChild
	}
}