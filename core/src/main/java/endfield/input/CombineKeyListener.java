package endfield.input;

import arc.Core;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.struct.Seq;

import java.util.Map;

import static endfield.input.CombinedKeys.isControllerKey;

public class CombineKeyListener<T> extends InputListener {
	protected Seq<KeyCode> keysDown = new Seq<>(KeyCode.class);

	public final CombineKeyTree<T> keysTree;
	public final boolean fuzzed;

	public CombineKeyListener(CombineKeyTree<T> tree, boolean fuzz) {
		keysTree = tree;
		fuzzed = fuzz;
	}

	@Override
	public boolean keyDown(InputEvent event, KeyCode keycode) {
		if (!keysTree.containsKeyCode(keycode)) return false;
		keysDown.addUnique(keycode);

		if (isControllerKey(keycode)) return true;

		if (!fuzzed) {
			Map<CombinedKeys, T> map = keysTree.getTargetBindings(Core.input);
			if (map.isEmpty()) return false;

			CombinedKeys keys = new CombinedKeys(keysDown.toArray());
			if (map.containsKey(keys)) {
				keysDown(event, keycode, keys, map.get(keys));
			}
		} else {
			keysTree.eachTargetBindings(Core.input, true, (k, r) -> {
				if (k.key == keycode) keysDown(event, keycode, k, r);
			});
		}

		return true;
	}

	@Override
	public boolean keyUp(InputEvent event, KeyCode keycode) {
		if (!keysTree.containsKeyCode(keycode)) return false;

		if (isControllerKey(keycode)) {
			return keysDown.remove(keycode);
		}

		if (!fuzzed) {
			Map<CombinedKeys, T> map = keysTree.getTargetBindings(Core.input);
			if (map.isEmpty()) return false;

			keysDown.add(keycode); // When the button is lifted, it will inevitably listen to the last button that is lifted
			CombinedKeys keys = new CombinedKeys(keysDown.toArray());
			if (map.containsKey(keys)) {
				keysUp(event, keycode, keys, map.get(keys));
			}
			keysDown.remove(keysDown.size - 1);
		} else {
			keysTree.eachTargetBindings(Core.input, true, (k, r) -> {
				if (k.key == keycode) keysUp(event, keycode, k, r);
			});
		}
		return keysDown.remove(keycode);
	}

	public void keysDown(InputEvent event, KeyCode keycode, CombinedKeys combinedKeys, T rec) {}

	public void keysUp(InputEvent event, KeyCode keycode, CombinedKeys combinedKeys, T rec) {}
}
