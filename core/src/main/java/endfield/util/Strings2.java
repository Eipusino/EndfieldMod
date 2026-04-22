package endfield.util;

import arc.Core;
import arc.math.Mathf;
import arc.util.Strings;
import kotlin.text.StringsKt;

import java.util.Arrays;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Strings2 {
	static final String[] byteUnit = {"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB", "BB"};

	static final String printableString = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
	static final char[] printableChars = {
			' ', '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			':', ';', '<', '=', '>', '?', '@',
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'[', '\\', ']', '^', '_', '`',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'{', '|', '}', '~'
	};

	public static final Pattern integerNumeric = Pattern.compile("^[+-]?\\d+$");
	public static final Pattern floatNumeric = Pattern.compile("^[+-]?(\\d+\\.\\d*|\\.\\d+|\\d+)([eE][+-]?\\d+)?$");

	private Strings2() {}

	/**
	 * The {@code char} version of {@code String.repeat(int)}.
	 *
	 * @throws IllegalArgumentException if the {@code count} is negative.
	 */
	public static String repeat(char key, int count) {
		if (count < 0) throw new IllegalArgumentException("count is negative: " + count);

		char[] data = new char[count];
		Arrays.fill(data, key);

		return String.valueOf(data);
	}

	@KotlinIn
	public static String substringAfter(String str, String delimiter) {
		return StringsKt.substringAfter(str, delimiter, str);
	}

	@KotlinIn
	public static String substringAfterLast(String str, String delimiter) {
		return StringsKt.substringAfterLast(str, delimiter, str);
	}

	@KotlinIn
	public static String substringBeforeLast(String str, String delimiter) {
		return StringsKt.substringBeforeLast(str, delimiter, str);
	}

	@KotlinIn
	public static MatchResult matchAt(Pattern pattern, String input, int index) {
		Matcher matcher = pattern.matcher(input);
		matcher.region(index, input.length());
		return matcher.lookingAt() ? matcher.toMatchResult() : null;
	}

	@KotlinIn
	public static int sumOf(CharSequence cs, Selector selector) {
		var sum = 0;
		for (int i = 0; i < cs.length(); i++) {
			sum += selector.get(cs.charAt(i));
		}
		return sum;
	}

	/** Determine whether the string is composed entirely of numbers. */
	public static boolean isNumeric4(String key) {
		return key != null && integerNumeric.matcher(key).matches();
	}

	public static boolean isNumeric(String key) {
		return key != null && floatNumeric.matcher(key).matches();
	}

	/** Randomly generate a string of length within the specified range. */
	public static String randomString(int min, int max) {
		if (min < 0 || max < min || max > 1000000) return Core.bundle.format("text.generate-random-string-2", min, max);

		int length = min + Mathf.random(max - min + 1);
		return randomString(length);
	}

	/**
	 * Randomly generate a string of specified length.
	 *
	 * @throws NegativeArraySizeException If the {@code length} is negative.
	 */
	public static String randomString(int length) {
		return randomString(printableChars, length);
	}

	public static String randomString(String keys, int length) {
		return randomString(keys.toCharArray(), length);
	}

	/**
	 * @param keys chars
	 */
	public static String randomString(char[] keys, int length) {
		char[] result = new char[length];
		int range = keys.length - 1;

		for (int i = 0; i < length; i++) {
			result[i] = keys[Mathf.random(range)];
		}
		return String.valueOf(result);
	}

	/**
	 * Convert numbers to computer storage capacity count representation without units.
	 *
	 * @param number The number to be converted
	 * @param retain Reserved decimal places
	 */
	public static String toByteFixNonUnit(double number, int retain) {
		boolean isNegative = false;
		if (number < 0) {
			number = -number;
			isNegative = true;
		}

		double base = 1d;
		for (int i = 0; i < byteUnit.length; i++) {
			if (base * 1024 > number) {
				break;
			}
			base *= 1024;
		}

		String[] arr = Double.toString(number / base).split("\\.");
		int realRetain = Math.min(retain, arr[1].length());

		String end = repeat('0', Math.max(0, retain - realRetain));

		return (isNegative ? "-" : "") + arr[0] + (retain == 0 ? "" : "." + arr[1].substring(0, realRetain) + end);
	}

	/**
	 * Convert numbers to computer storage capacity count representation.
	 *
	 * @param number The number to be converted
	 * @param retain Reserved decimal places
	 */
	public static String toByteFix(double number, int retain) {
		boolean isNegative = false;
		if (number < 0) {
			number = -number;
			isNegative = true;
		}

		double base = 1d;
		for (int i = 0; i < byteUnit.length; i++) {
			if (base * 1024 > number) {
				break;
			}
			base *= 1024;
		}

		String str = Double.toString(number / base);
		int dotIndex = str.indexOf('.');

		String integerPart = str.substring(0, dotIndex);
		String fractionalPart = str.substring(dotIndex + 1);

		int realRetain = Math.min(retain, fractionalPart.length());

		StringBuilder builder = new StringBuilder();
		if (isNegative) {
			builder.append('-');
		}
		builder.append(integerPart);
		if (retain != 0) {
			builder.append('.');
			builder.append(fractionalPart, 0, realRetain);
			for (int i = 0; i < Math.max(0, retain - realRetain); i++) {
				builder.append('0');
			}
		}
		return builder.toString();
	}

	public static String toStoreSize(float num) {
		float v = num;
		int n = 0;

		while (v > 1024) {
			v /= 1024;
			n++;
		}

		return Strings.fixed(v, 2) + "[lightgray]" + byteUnit[n];
	}

	public static double parseDouble(String key, double defaultValue) {
		if (key == null) return defaultValue;

		key = key.trim();
		if (key.isEmpty()) return defaultValue;

		if (key.equals("NaN") || key.equals("Infinity") || key.equals("-Infinity")) return defaultValue;

		int length = key.length();
		int index = 0;
		boolean negative = false;

		char first = key.charAt(index);
		if (first == '+') {
			index++;
		} else if (first == '-') {
			negative = true;
			index++;
		}

		double intPair = 0;
		boolean hssInteger = false;
		while (index < length) {
			char c = key.charAt(index);
			if (c >= '0' && c <= '9') {
				intPair = intPair * 10 + (c - '0');
				hssInteger = true;
				index++;
			} else {
				break;
			}
		}

		double fracPair = 0;
		int fracDigits = 0;
		boolean hasFraction = false;
		if (index < length && key.charAt(index) == '.') {
			index++;
			double factor = 0.1;
			while (index < length) {
				char c = key.charAt(index);
				if (c >= '0' && c <= '9') {
					fracPair += (c - '0') * factor;
					factor *= 0.1;
					fracDigits++;
					hasFraction = true;
					index++;
				} else {
					break;
				}
			}
		}

		boolean hasExponent = false;
		int exponent = 0;
		boolean expNegative = false;
		if (index < length && (key.charAt(index) == 'e' || key.charAt(index) == 'E')) {
			index++;
			hasExponent = true;

			if (index < length && (key.charAt(index) == '+') || key.charAt(index) == '-') {
				expNegative = (key.charAt(index) == '-');
				index++;
			}

			int expValue = 0;
			int digitCount = 0;
			while (index < length) {
				char c = key.charAt(index);
				if (c >= '0' && c <= '9') {
					if (expValue <= 1000) {
						expValue = expValue * 10 + (c - '0');
					}
					digitCount++;
					index++;
				} else {
					break;
				}
			}
			if (digitCount == 0) return defaultValue;

			exponent = expNegative ? -expValue : expValue;

			if (Math.abs(exponent) > 1000) {
				exponent = exponent > 0 ? 1000 : -1000;
			}
		}

		if (!hssInteger && !hasFraction) return defaultValue;
		if (index != length) return defaultValue;

		double value = intPair + fracPair;
		if (hasExponent) value *= Math.pow(10, exponent);
		if (negative) value = -value;

		return value;
	}

	public interface Selector {
		int get(char c);
	}
}
