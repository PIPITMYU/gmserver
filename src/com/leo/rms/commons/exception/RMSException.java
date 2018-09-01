package com.leo.rms.commons.exception;

public class RMSException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public RMSException() {
	}
	
	public RMSException(String msg) {
		super( msg );
	}
}
