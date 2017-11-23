package cn.liutaixiong.hapi.netty;

import cn.liutaixiong.hapi.http.HapiHttpRequest;
import cn.liutaixiong.hapi.http.HapiHttpResponse;
import io.netty.handler.codec.http.HttpMethod;

/**
 * <b>Request context interface when parse success</b>
 * @author taixiong.liu
 * 
 */
public interface HttpRequestHandler {
	public void onGet(HapiHttpRequest request, HapiHttpResponse response);
	public void onPost(HapiHttpRequest request, HapiHttpResponse response);
	public void onError(HttpMethod fromMethod, HapiHttpResponse response, Exception e);
}
