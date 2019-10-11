package cn.com.pan.live.util.exception;

import lombok.Getter;

public class RestRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Getter
	private Integer code;

	@Getter
	private Object[] args;

	public RestRuntimeException(Integer code, Object... args) {
		super(code.toString());
		this.code = code;
		this.args = args;
	}

}
