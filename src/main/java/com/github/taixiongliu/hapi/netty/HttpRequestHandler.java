package com.github.taixiongliu.hapi.netty;

import com.github.taixiongliu.hapi.http.HapiHttpRequest;
import com.github.taixiongliu.hapi.http.HapiHttpResponse;

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
