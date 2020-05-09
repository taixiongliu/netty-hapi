package com.github.taixiongliu.hapi.route;

import java.lang.reflect.Method;

/**
 * <b>Entity Router</b>
 * @author taixiong.liu
 *
 */
public class Router {
	private String path;
	private Object clazz;
	private Method md;
	private String position;
	private HapiHttpMethod httpMethod;
	private HapiRouteType routeType;
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Object getClazz() {
		return clazz;
	}
	public void setClazz(Object clazz) {
		this.clazz = clazz;
	}
	public Method getMd() {
		return md;
	}
	public void setMd(Method md) {
		this.md = md;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public HapiHttpMethod getHttpMethod() {
		return httpMethod;
	}
	public void setHttpMethod(HapiHttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}
	public HapiRouteType getRouteType() {
		return routeType;
	}
	public void setRouteType(HapiRouteType routeType) {
		this.routeType = routeType;
	}
}
