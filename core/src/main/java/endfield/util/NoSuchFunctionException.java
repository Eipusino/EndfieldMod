package endfield.util;

public class NoSuchFunctionException extends RuntimeException {
	private static final long serialVersionUID = -9156157988575156106l;

	public NoSuchFunctionException() {
		super();
	}

	public NoSuchFunctionException(String message) {
		super(message);
	}

	public NoSuchFunctionException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchFunctionException(Throwable cause) {
		super(cause);
	}
}
