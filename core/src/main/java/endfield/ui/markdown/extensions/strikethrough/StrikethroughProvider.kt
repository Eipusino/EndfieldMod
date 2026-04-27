package endfield.ui.markdown.extensions.strikethrough

import endfield.ui.markdown.MarkdownProvider
import endfield.ui.markdown.RendererContext
import org.commonmark.ext.gfm.strikethrough.Strikethrough

interface StrikethroughProvider : MarkdownProvider {
	fun RendererContext.add(node: Strikethrough)
}