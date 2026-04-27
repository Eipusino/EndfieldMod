package endfield.ui.markdown

import arc.scene.event.SceneEvent

class UrlClickedEvent(
	@JvmField
	val clickedUrl: String,
) : SceneEvent()