package endfield.ui.markdown.extensions.curtain

import endfield.ui.markdown.MarkdownProvider
import endfield.ui.markdown.RendererContext

interface CurtainProvider : MarkdownProvider {
	fun RendererContext.add(node: Curtain)
}