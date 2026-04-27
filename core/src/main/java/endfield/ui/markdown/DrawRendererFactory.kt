package endfield.ui.markdown

import org.commonmark.renderer.NodeRenderer

@FunctionalInterface
fun interface DrawRendererFactory<P : MarkdownProvider> {
	fun create(context: RendererContext, provider: P): NodeRenderer
}