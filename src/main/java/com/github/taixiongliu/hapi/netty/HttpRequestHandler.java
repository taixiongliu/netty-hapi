package com.github.taixiongliu.hapi.netty;

import com.github.taixiongliu.hapi.http.DefaultHapiHttpResponseImpl;
import com.github.taixiongliu.hapi.http.HapiHttpRequest;

import io.netty.handler.codec.http.HttpMethod;

/**
 * <b>Request context interface when parse success</b>
 * @author taixiong.liu
 * 
 */
public interface HttpRequestHandler {
	public void onGet(HapiHttpRequest request, DefaultHapiHttpResponseImpl response);
	public void onPost(HapiHttpRequest request, DefaultHapiHttpResponseImpl response);
	public void onError(HttpMethod fromMethod, DefaultHapiHttpResponseImpl response, Exception e);
}
