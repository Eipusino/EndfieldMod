package endfield.ui.markdown

import org.commonmark.renderer.NodeRenderer

abstract class LayoutNodeRenderer<P : MarkdownProvider>(
	@JvmField
	protected val context: RendererContext,
	@JvmField
	protected val provider: P
) : NodeRenderer