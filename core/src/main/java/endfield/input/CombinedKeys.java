package endfield.input;

import arc.Input;
import arc.input.KeyCode;
import arc.util.Structs;
import endfield.util.Arrays2;

import java.io.Serializable;
import java.util.Objects;

/**
 * A tagged object that records standard combination keys in a specific format.
 * <p>A combination key consists of a primary key and control keys, where the control key is`Ctrl`.
 */
public class CombinedKeys implements Serializable {
	private static final long serialVersionUID = -3887748232403728770l;

	public final boolean isCtrl, isAlt, isShift;
	public final KeyCode key;

	public CombinedKeys(KeyCode... keys) {
		isCtrl = Structs.contains(keys, CombinedKeys::isCtrl);
		isAlt = Structs.contains(keys, CombinedKeys::isAlt);
		isShift = Structs.contains(keys, CombinedKeys::isShift);

		key = Objects.requireNonNull(Structs.find(keys, k -> !isCtrl(k) && !isAlt(k) && !isShift(k)));
	}

	boolean filter(Input input) {
		if ((isCtrl && !input.ctrl()) || (!isCtrl && input.alt())) return false;
		if ((isAlt && !input.alt()) || (!isAlt && input.alt())) return false;
		return (!isShift || input.shift()) && (isShift || !input.shift());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		if (isCtrl) builder.append("Ctrl").append(" + ");
		if (isAlt) builder.append("Alt").append(" + ");
		if (isShift) builder.append("Shift").append(" + ");

		builder.append(key.value);

		return builder.toString();
	}

	@Override
	public int hashCode() {
		int res = key.hashCode();
		res = res * 31 + Boolean.hashCode(isShift);
		res = res * 31 + Boolean.hashCode(isAlt);
		res = res * 31 + Boolean.hashCode(isCtrl);
		return res;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CombinedKeys keys
				&& keys.key == key
				&& keys.isCtrl == isCtrl
				&& keys.isAlt == isAlt
				&& keys.isShift == isShift;
	}

	public static String toString(Iterable<KeyCode> res) {
		StringBuilder builder = new StringBuilder();

		if (Arrays2.contains(res, KeyCode.controlLeft) || Arrays2.contains(res, KeyCode.controlRight))
			builder.append("Ctrl")
					.append(" + ");
		if (Arrays2.contains(res, KeyCode.altLeft) || Arrays2.contains(res, KeyCode.altRight))
			builder.append("Alt").append(" + ");
		if (Arrays2.contains(res, KeyCode.shiftLeft) || Arrays2.contains(res, KeyCode.shiftRight))
			builder.append("Shift")
					.append(" + ");

		KeyCode controllerKey = Arrays2.find(res, CombinedKeys::isControllerKey);
		builder.append(controllerKey == null ? "" : controllerKey.value);

		return builder.toString();
	}

	public static boolean isControllerKey(KeyCode key) {
		return key == KeyCode.controlLeft || key == KeyCode.controlRight
				|| key == KeyCode.altLeft || key == KeyCode.altRight
				|| key == KeyCode.shiftLeft || key == KeyCode.shiftRight;
	}

	public static boolean isCtrl(KeyCode key) {
		return key == KeyCode.controlLeft || key == KeyCode.controlRight;
	}

	public static boolean isAlt(KeyCode key) {
		return key == KeyCode.altLeft || key == KeyCode.altRight;
	}

	public static boolean isShift(KeyCode key) {
		return key == KeyCode.shiftLeft || key == KeyCode.shiftRight;
	}
}
