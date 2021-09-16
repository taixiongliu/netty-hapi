package com.github.taixiongliu.hapi.http;

import java.io.OutputStream;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * <b>Response context interface</b>
 * @author taixiong.liu
 * 
 */
public interface HapiHttpResponse {
	public void setStatus(HttpResponseStatus status);
	public void setContent(String content);
	public void setHead(String name, String value);
	public OutputStream getOutputStream();
	public OutputStream getOutputStream(String contentType);
}
