package com.github.taixiongliu.hapi.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.taixiongliu.hapi.route.HapiRouteType;

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
	private HapiRouteType routeType;
	public DefaultHapiHttpResponseImpl() {
		// TODO Auto-generated constructor stub
		this(null, null);
	}
	public DefaultHapiHttpResponseImpl(HttpResponseStatus status, String content) {
		// TODO Auto-generated constructor stub
		this.status = status;
		this.content = content;
		heads = new ConcurrentHashMap<String, String>();
		routeType = HapiRouteType.BODY;
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

	public HapiRouteType getRouteType() {
		return routeType;
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
	public void setRouteType(HapiRouteType routeType) {
		// TODO Auto-generated method stub
		this.routeType = routeType;
	}
}
