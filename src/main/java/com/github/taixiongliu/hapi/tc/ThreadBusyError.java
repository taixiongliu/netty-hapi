package com.github.taixiongliu.hapi.tc;

import io.netty.handler.codec.http.HttpResponseStatus;

public class ThreadBusyError {
	private HttpResponseStatus status;
	private String keyCode;
	private int code;
	private String keyMessage;
	private String message;
	public ThreadBusyError() {
		// TODO Auto-generated constructor stub
	}
	public ThreadBusyError(HttpResponseStatus status,String keyCode, int code, String keyMessage, String message) {
		// TODO Auto-generated constructor stub
		this.status = status;
		this.keyCode = keyCode;
		this.code = code;
		this.keyMessage = keyMessage;
		this.message = message;
	}
	public HttpResponseStatus getStatus() {
		return status;
	}
	public void setStatus(HttpResponseStatus status) {
		this.status = status;
	}
	public String getKeyCode() {
		return keyCode;
	}
	public void setKeyCode(String keyCode) {
		this.keyCode = keyCode;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getKeyMessage() {
		return keyMessage;
	}
	public void setKeyMessage(String keyMessage) {
		this.keyMessage = keyMessage;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
