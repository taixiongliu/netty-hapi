package cn.liutaixiong.hapi.http;

import io.netty.handler.codec.http.HttpHeaders;

/**
 * <b>Request context interface</b>
 * @author taixiong.liu
 * 
 */
public interface HapiHttpRequest {
	public String getUrl();
	public String getIpAddress();
	public String getParameter(String paramName);
	public String uri();
	public HttpHeaders heads();
}
