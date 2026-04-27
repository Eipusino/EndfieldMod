package endfield.ui.markdown.extensions.ins

import endfield.ui.markdown.MarkdownProvider
import endfield.ui.markdown.RendererContext
import org.commonmark.ext.ins.Ins

interface InsProvider : MarkdownProvider {
	fun RendererContext.add(node: Ins)
}