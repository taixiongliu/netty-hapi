package com.github.taixiongliu.hapi.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	private Map<String, String> heads;
	public DefaultHapiHttpResponseImpl() {
		// TODO Auto-generated constructor stub
		this(null, null);
	}
	public DefaultHapiHttpResponseImpl(HttpResponseStatus status, String content) {
		// TODO Auto-generated constructor stub
		this.status = status;
		this.content = content;
		heads = new ConcurrentHashMap<String, String>();
	}
	
	public HttpResponseStatus getStatus() {
		return status;
	}

	public String getContent() {
		return content;
	}
	
	public Map<String, String> heads(){
		return heads;
	}

	public void setStatus(HttpResponseStatus status) {
		// TODO Auto-generated method stub
		this.status = status;
	}
	public void setContent(String content) {
		// TODO Auto-generated method stub
		this.content = content;
	}
	public void setHead(String name, String value) {
		// TODO Auto-generated method stub
		heads.put(name, value);
	}
}
