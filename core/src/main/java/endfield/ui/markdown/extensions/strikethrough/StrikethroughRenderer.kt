package endfield.ui.markdown.extensions.strikethrough

import endfield.ui.markdown.LayoutNodeRenderer
import endfield.ui.markdown.NoActionVisitor
import endfield.ui.markdown.RendererContext
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.node.CustomNode
import org.commonmark.node.Node

class StrikethroughRenderer(
	context: RendererContext,
	provider: StrikethroughProvider,
) : LayoutNodeRenderer<StrikethroughProvider>(context, provider) {
	private val visitorInst = object : NoActionVisitor() {
		override fun visit(customNode: CustomNode) {
			if (customNode is Strikethrough) provider.apply { context.add(customNode) }
		}
	}

	override fun getNodeTypes(): Set<Class<out Node>> {
		return setOf(Strikethrough::class.java)
	}

	override fun render(node: Node) {
		node.accept(visitorInst)
	}
}