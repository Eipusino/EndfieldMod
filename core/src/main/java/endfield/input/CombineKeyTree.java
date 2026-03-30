package endfield.input;

import arc.Input;
import arc.func.Cons;
import arc.func.Cons2;
import arc.input.KeyCode;
import endfield.util.Arrays2;
import endfield.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static endfield.input.CombinedKeys.isAlt;
import static endfield.input.CombinedKeys.isCtrl;
import static endfield.input.CombinedKeys.isShift;

public class CombineKeyTree<T> {
	final Map<CombinedKeys, T> tempMap = new HashMap<>();

	final Map<CombinedKeys, T> normalBindings = new HashMap<>();
	final Map<CombinedKeys, T> ctrlBindings = new HashMap<>();
	final Map<CombinedKeys, T> altBindings = new HashMap<>();
	final Map<CombinedKeys, T> shiftBindings = new HashMap<>();
	final Map<CombinedKeys, T> altCtrlBindings = new HashMap<>();
	final Map<CombinedKeys, T> ctrlShiftBindings = new HashMap<>();
	final Map<CombinedKeys, T> altShiftBindings = new HashMap<>();
	final Map<CombinedKeys, T> altCtrlShiftBindings = new HashMap<>();

	public void putKeyBinding(CombinedKeys binding, T rec) {
		if (binding.isShift) {
			if (binding.isAlt && binding.isCtrl) altCtrlShiftBindings.put(binding, rec);
			else if (binding.isAlt) altShiftBindings.put(binding, rec);
			else if (binding.isCtrl) ctrlShiftBindings.put(binding, rec);
			else shiftBindings.put(binding, rec);
		} else {
			if (binding.isAlt && binding.isCtrl) altCtrlBindings.put(binding, rec);
			else if (binding.isAlt) altBindings.put(binding, rec);
			else if (binding.isCtrl) ctrlBindings.put(binding, rec);
			else normalBindings.put(binding, rec);
		}
	}

	@SuppressWarnings("unchecked")
	public void putKeyBinds(Pair<CombinedKeys, T>... bindings) {
		for (Pair<CombinedKeys, T> binding : bindings) {
			putKeyBinding(binding.first, binding.second);
		}
	}

	public void clear() {
		normalBindings.clear();
		altBindings.clear();
		ctrlBindings.clear();
		shiftBindings.clear();
		altShiftBindings.clear();
		ctrlShiftBindings.clear();
		altCtrlBindings.clear();
		altCtrlShiftBindings.clear();
	}

	public void each(Cons2<CombinedKeys, T> block) {
		normalBindings.forEach(block::get);
		ctrlBindings.forEach(block::get);
		altBindings.forEach(block::get);
		altCtrlBindings.forEach(block::get);
		ctrlShiftBindings.forEach(block::get);
		altShiftBindings.forEach(block::get);
		altCtrlShiftBindings.forEach(block::get);
	}

	public boolean containsKeyCode(KeyCode key) {
		if (key == null) return false;

		if (isCtrl(key))
			return !ctrlBindings.isEmpty() || !ctrlShiftBindings.isEmpty() || !altCtrlBindings.isEmpty() || !altCtrlShiftBindings.isEmpty();
		if (isAlt(key))
			return !altBindings.isEmpty() || !altShiftBindings.isEmpty() || !altCtrlBindings.isEmpty() || !altCtrlShiftBindings.isEmpty();
		if (isShift(key))
			return !shiftBindings.isEmpty() || !ctrlShiftBindings.isEmpty() || !altShiftBindings.isEmpty() || !altCtrlShiftBindings.isEmpty();

		return Arrays2.anyKey(normalBindings, k -> k.key == key)
				|| Arrays2.anyKey(ctrlBindings, k -> k.key == key)
				|| Arrays2.anyKey(altBindings, k -> k.key == key)
				|| Arrays2.anyKey(shiftBindings, k -> k.key == key)
				|| Arrays2.anyKey(altCtrlBindings, k -> k.key == key)
				|| Arrays2.anyKey(ctrlShiftBindings, k -> k.key == key)
				|| Arrays2.anyKey(altShiftBindings, k -> k.key == key)
				|| Arrays2.anyKey(altCtrlShiftBindings, k -> k.key == key);
	}

	public Map<CombinedKeys, T> getTargetBindings(Input input) {
		return getTargetBindings(input, false);
	}

	public Map<CombinedKeys, T> getTargetBindings(Input input, boolean fuzzyMatch) {
		return getTargetBindings(input.ctrl(), input.alt(), input.shift(), fuzzyMatch);
	}

	public Map<CombinedKeys, T> getTargetBindings(CombinedKeys input, boolean fuzzyMatch) {
		return getTargetBindings(input.isCtrl, input.isAlt, input.isShift, fuzzyMatch);
	}

	public Map<CombinedKeys, T> getTargetBindings(CombinedKeys input) {
		return getTargetBindings(input, false);
	}

	public Map<CombinedKeys, T> getTargetBindings(boolean ctrlDown, boolean altDown, boolean shiftDown) {
		return getTargetBindings(ctrlDown, altDown, shiftDown, false);
	}

	public Map<CombinedKeys, T> getTargetBindings(boolean ctrlDown, boolean altDown, boolean shiftDown, boolean fuzzyMatch) {
		tempMap.clear();
		if (fuzzyMatch) {
			tempMap.putAll(normalBindings);
			if (ctrlDown) {
				tempMap.putAll(ctrlBindings);
				if (altDown) {
					tempMap.putAll(altCtrlBindings);
					if (shiftDown) tempMap.putAll(altCtrlShiftBindings);
				} else if (shiftDown) tempMap.putAll(ctrlShiftBindings);
			} else {
				if (altDown) {
					tempMap.putAll(altBindings);
					if (shiftDown) tempMap.putAll(altShiftBindings);
				} else if (shiftDown) tempMap.putAll(shiftBindings);
			}
			return tempMap;
		} else {
			return shiftDown ?
					(altDown ? (ctrlDown ? altCtrlShiftBindings : altShiftBindings)
							: (ctrlDown ? ctrlShiftBindings : shiftBindings))
					: (altDown ? (ctrlDown ? altCtrlBindings : altBindings)
					: (ctrlDown ? ctrlBindings : normalBindings));

		}
	}

	public void eachTargetBindings(Input input, boolean fuzzyMatch, Cons2<CombinedKeys, T> cons) {
		for (var entry : getTargetBindings(input, fuzzyMatch).entrySet()) cons.get(entry.getKey(), entry.getValue());
	}

	public void eachTargetBindings(Input input, Cons2<CombinedKeys, T> cons) {
		eachTargetBindings(input, false, cons);
	}

	public void eachDown(Input input, boolean fuzzyMatch, Cons<T> cons) {
		eachTargetBindings(input, fuzzyMatch, (k, r) -> {
			if (input.keyDown(k.key)) cons.get(r);
		});
	}

	public void eachRelease(Input input, boolean fuzzyMatch, Cons<T> cons) {
		eachTargetBindings(input, fuzzyMatch, (k, r) -> {
			if (input.keyRelease(k.key)) cons.get(r);
		});
	}

	public void eachTap(Input input, boolean fuzzyMatch, Cons<T> cons) {
		eachTargetBindings(input, fuzzyMatch, (k, r) -> {
			if (input.keyTap(k.key)) cons.get(r);
		});
	}

	public @Nullable T checkDown(Input input) {
		AtomicReference<T> ref = new AtomicReference<>();
		eachTargetBindings(input, (k, r) -> {
			if (input.keyDown(k.key)) ref.set(r);
		});
		return ref.get();
	}

	public @Nullable T checkRelease(Input input) {
		AtomicReference<T> ref = new AtomicReference<>();
		eachTargetBindings(input, (k, r) -> {
			if (input.keyRelease(k.key)) ref.set(r);
		});
		return ref.get();
	}

	public @Nullable T checkTap(Input input) {
		AtomicReference<T> ref = new AtomicReference<>();
		eachTargetBindings(input, (k, r) -> {
			if (input.keyTap(k.key)) ref.set(r);
		});
		return ref.get();
	}

	@Override
	public String toString() {
		var stringBuilder = new StringBuilder();
		each((keys, rec) -> stringBuilder.append(keys).append(" -> ").append(rec).append(",\n"));
		return stringBuilder.toString();
	}
}
