package endfield.ui.markdown.extensions.table

import arc.scene.ui.layout.Table
import endfield.ui.markdown.LayoutNodeRenderer
import endfield.ui.markdown.NoActionVisitor
import endfield.ui.markdown.RendererContext
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.node.CustomBlock
import org.commonmark.node.CustomNode
import org.commonmark.node.Node

open class TableRenderer(
	context: RendererContext,
	provider: TableProvider
) : LayoutNodeRenderer<TableProvider>(context, provider) {
	private val visitorInst = object : NoActionVisitor() {
		override fun visit(customBlock: CustomBlock) {
			if (customBlock is TableBlock) {
				provider.apply { context.add(customBlock) }
			}
		}

		override fun visit(customNode: CustomNode) {
			if (customNode is TableHead) {
				provider.apply { context.add(customNode) }
			}
			if (customNode is TableBody) {
				provider.apply { context.add(customNode) }
			}
			if (customNode is TableRow) {
				provider.apply { context.add(customNode) }
			}
			if (customNode is TableCell) {
				provider.apply { context.add(customNode) }
			}
			if (customNode is CellShadowBox) {
				provider.apply { context.add(customNode) }
			}
		}
	}

	private var currentTable: Table? = null

	override fun getNodeTypes() = setOf(
		TableBlock::class.java,
		TableHead::class.java,
		TableBody::class.java,
		TableRow::class.java,
		TableCell::class.java,
		CellShadowBox::class.java,
	)

	override fun render(node: Node) {
		node.accept(visitorInst)
	}
}