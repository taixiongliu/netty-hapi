package com.github.taixiongliu.hapi.http;

import java.util.Map;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

/**
 * <b>Request context interface</b>
 * @author taixiong.liu
 * 
 */
public interface HapiHttpRequest {
	public String getUrl();
	public String getIpAddress();
	public String getParameter(String paramName);
	public String getUrlParameter(String paramName);
	public String getBodyParameter(String paramName);
	public Map<String, String> mapUrlParameter();
	public Map<String, String> mapBodyParameter();
	public String uri();
	public HttpHeaders heads();
	public String getVersion();
	public String getReUrl();
	public HttpMethod getMethod();
	public String getPathParameter(String pathParamName);
}
