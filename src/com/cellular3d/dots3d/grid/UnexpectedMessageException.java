package com.cellular3d.dots3d.grid;

public class UnexpectedMessageException extends Exception {

	public UnexpectedMessageException() {
	}

	public UnexpectedMessageException(String message) {
		super(message);
	}

	public UnexpectedMessageException(Throwable cause) {
		super(cause);
	}

	public UnexpectedMessageException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnexpectedMessageException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
