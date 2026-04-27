package endfield.ui.markdown

import org.commonmark.Extension
import org.commonmark.internal.renderer.NodeRendererMap
import org.commonmark.node.Node

class MDLayoutRenderer constructor(builder: Builder) {
	private val nodeRendererFactories: List<DrawRendererFactory<*>>
	private var context: RendererContextImpl? = null

	init {
		val factories = mutableListOf<DrawRendererFactory<*>>()
		factories.addAll(builder.nodeRendererFactories)
		factories.add(DrawRendererFactory(::BaseDrawRenderer))
		nodeRendererFactories = factories.toList()
	}

	fun createContext(element: Markdown): RendererContext {
		context = this.RendererContextImpl(element)
		return context!!
	}

	fun renderLayout(node: Node) {
		checkNotNull(context) { "context must be created first" }
		context!!.init()
		context!!.render(node)
	}

	class Builder {
		internal val nodeRendererFactories = mutableListOf<DrawRendererFactory<*>>()

		fun build(): MDLayoutRenderer {
			return MDLayoutRenderer(this)
		}

		fun <P : MarkdownProvider> nodeRendererFactory(nodeRendererFactory: DrawRendererFactory<P>): Builder {
			this.nodeRendererFactories.add(nodeRendererFactory)
			return this
		}

		fun extensions(extensions: Iterable<Extension>): Builder {
			for (extension in extensions) {
				if (extension is DrawRendererExtension) {
					extension.extend(this)
				}
			}
			return this
		}
	}

	interface DrawRendererExtension : Extension {
		fun extend(rendererBuilder: Builder)
	}

	internal inner class RendererContextImpl(element: Markdown) : RendererContext(element) {
		private val nodeRendererMap: NodeRendererMap = NodeRendererMap()

		init {
			for (i in nodeRendererFactories.indices.reversed()) {
				val nodeRendererFactory = nodeRendererFactories[i]

				@Suppress("UNCHECKED_CAST")
				val nodeRenderer = (nodeRendererFactory as DrawRendererFactory<MarkdownProvider>)
					.create(this, element.provider)
				nodeRendererMap.add(nodeRenderer)
			}
		}

		override fun render(node: Node) {
			nodeRendererMap.render(node)
		}
	}

	companion object {
		fun builder(): Builder {
			return Builder()
		}
	}
}
