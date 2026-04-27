package endfield.ui.markdown.extensions.ins

import endfield.ui.markdown.LayoutNodeRenderer
import endfield.ui.markdown.NoActionVisitor
import endfield.ui.markdown.RendererContext
import org.commonmark.ext.ins.Ins
import org.commonmark.node.CustomNode
import org.commonmark.node.Node

class InsNodeRenderer(
	element: RendererContext,
	provider: InsProvider
) : LayoutNodeRenderer<InsProvider>(element, provider) {
	private val visitorInst = object : NoActionVisitor() {
		override fun visit(customNode: CustomNode) {
			if (customNode is Ins) provider.apply { context.add(customNode) }
		}
	}

	override fun getNodeTypes(): Set<Class<out Node>> {
		return setOf(Ins::class.java)
	}

	override fun render(node: Node) {
		node.accept(visitorInst)
	}
}