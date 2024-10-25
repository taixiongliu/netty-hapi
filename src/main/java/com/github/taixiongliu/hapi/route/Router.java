package com.github.taixiongliu.hapi.route;

import java.lang.reflect.Method;

/**
 * <b>Entity Router</b>
 * @author taixiong.liu
 *
 */
public class Router {
	private String path;
	private int pathParameters;
	private String[] pathParameterNames;
	private String[] pathParameterValues;
	private Object clazz;
	private Method md;
	private String position;
	private HapiHttpMethod httpMethod;
	private HapiRouteType routeType;
	private String version;
	private String reUrl;
	public Router() {
		// TODO Auto-generated constructor stub
		this.pathParameters = 0;
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getPathParameters() {
		return pathParameters;
	}
	public void setPathParameters(int pathParameters) {
		this.pathParameters = pathParameters;
	}
	public String[] getPathParameterNames() {
		return pathParameterNames;
	}
	public void setPathParameterNames(String[] pathParameterNames) {
		this.pathParameterNames = pathParameterNames;
	}
	public String[] getPathParameterValues() {
		return pathParameterValues;
	}

	public void setPathParameterValues(String[] pathParameterValues) {
		this.pathParameterValues = pathParameterValues;
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
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getReUrl() {
		return reUrl;
	}
	public void setReUrl(String reUrl) {
		this.reUrl = reUrl;
	}
}
