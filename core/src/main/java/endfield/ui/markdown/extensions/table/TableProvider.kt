package endfield.ui.markdown.extensions.table

import endfield.ui.markdown.MarkdownProvider
import endfield.ui.markdown.RendererContext
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow

interface TableProvider : MarkdownProvider {
	fun RendererContext.add(node: TableBlock)
	fun RendererContext.add(node: TableHead)
	fun RendererContext.add(node: TableBody)
	fun RendererContext.add(node: TableRow)
	fun RendererContext.add(node: TableCell)
	fun RendererContext.add(node: CellShadowBox)
}