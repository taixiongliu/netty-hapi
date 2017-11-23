package cn.liutaixiong.hapi.http;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * <b>Response context interface</b>
 * @author taixiong.liu
 * 
 */
public interface HapiHttpResponse {
	public void setStatus(HttpResponseStatus status);
	public void setContent(String content);
}
