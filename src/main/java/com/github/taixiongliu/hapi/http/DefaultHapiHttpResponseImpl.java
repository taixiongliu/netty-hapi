package com.github.taixiongliu.hapi.http;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * <b>Response status object</b>
 * <br><br>
 * response context
 * @author taixiong.liu
 * 
 */
public class DefaultHapiHttpResponseImpl implements HapiHttpResponse{
	private HttpResponseStatus status;
	private String content;
	public DefaultHapiHttpResponseImpl() {
		// TODO Auto-generated constructor stub
	}
	public DefaultHapiHttpResponseImpl(HttpResponseStatus status, String content) {
		// TODO Auto-generated constructor stub
		this.status = status;
		this.content = content;
	}
	
	public HttpResponseStatus getStatus() {
		return status;
	}

	public String getContent() {
		return content;
	}

	public void setStatus(HttpResponseStatus status) {
		// TODO Auto-generated method stub
		this.status = status;
	}
	public void setContent(String content) {
		// TODO Auto-generated method stub
		this.content = content;
	}
}
