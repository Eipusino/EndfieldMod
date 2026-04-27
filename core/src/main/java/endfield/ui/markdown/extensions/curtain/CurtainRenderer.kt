package endfield.ui.markdown.extensions.curtain

import endfield.ui.markdown.LayoutNodeRenderer
import endfield.ui.markdown.NoActionVisitor
import endfield.ui.markdown.RendererContext
import org.commonmark.node.CustomNode
import org.commonmark.node.Node

class CurtainRenderer (
	context: RendererContext,
	provider: CurtainProvider,
) : LayoutNodeRenderer<CurtainProvider>(context, provider) {
	private val visitorInst = object : NoActionVisitor() {
		override fun visit(customNode: CustomNode) {
			if (customNode is Curtain) provider.apply { context.add(customNode) }
		}
	}

	override fun getNodeTypes(): Set<Class<out Node>> {
		return setOf(Curtain::class.java)
	}

	override fun render(node: Node) {
		node.accept(visitorInst)
	}
}