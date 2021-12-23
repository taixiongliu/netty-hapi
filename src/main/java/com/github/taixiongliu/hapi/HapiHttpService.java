package com.github.taixiongliu.hapi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

import com.github.taixiongliu.hapi.http.DefaultHapiHttpResponseImpl;
import com.github.taixiongliu.hapi.http.HapiHttpRequest;
import com.github.taixiongliu.hapi.http.HapiHttpResponse;
import com.github.taixiongliu.hapi.netty.HttpRequestHandler;
import com.github.taixiongliu.hapi.route.HapiHttpMethod;
import com.github.taixiongliu.hapi.route.Router;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * <b>Proxy route to call method</b>
 * @author taixiong.liu
 *
 */
public class HapiHttpService implements HttpRequestHandler{
	
	public HapiHttpService() {
		// TODO Auto-generated constructor stub
	}

	public void onGet(HapiHttpRequest request, DefaultHapiHttpResponseImpl response) {
		// TODO Auto-generated method stub
		Router router = HapiHttpContextFactory.getInstance().getRouter(request.getUrl());
		if(router == null){
			response.setStatus(HttpResponseStatus.NOT_FOUND);
			response.setContent("404 context not found...");
			return ;
		}
		if(router.getHttpMethod().equals(HapiHttpMethod.POST)){
			response.setStatus(HttpResponseStatus.FORBIDDEN);
			response.setContent("403 context not support post request...");
			return ;
		}
		//set route type.
		response.setRouteType(router.getRouteType());
		
		response.setStatus(HttpResponseStatus.OK);
		Parameter[] parameters = router.getMd().getParameters();
		int len = parameters.length;
		Object[] args = new Object[len];
		for(int i = 0; i < len; i++){
			Class<?> clazz = parameters[i].getType();
			if(clazz.isPrimitive()){
				args[i] = getPrimitiveParameter(clazz.getName());
				continue;
			}
			if(clazz.isAssignableFrom(HapiHttpRequest.class)) {
				args[i] = request;
				continue;
			}
			if(clazz.isAssignableFrom(HapiHttpResponse.class)) {
				args[i] = response;
				continue;
			}
			args[i] = null;
		}
		try {
			router.getMd().invoke(router.getClazz(), args);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onPost(HapiHttpRequest request, DefaultHapiHttpResponseImpl response) {
		// TODO Auto-generated method stub
		Router router = HapiHttpContextFactory.getInstance().getRouter(request.getUrl());
		if(router == null){
			response.setStatus(HttpResponseStatus.NOT_FOUND);
			response.setContent("404 context not found...");
			return ;
		}
		if(router.getHttpMethod().equals(HapiHttpMethod.GET)){
			response.setStatus(HttpResponseStatus.FORBIDDEN);
			response.setContent("403 context not support get request...");
			return ;
		}
		response.setRouteType(router.getRouteType());
		
		response.setStatus(HttpResponseStatus.OK);
		Parameter[] parameters = router.getMd().getParameters();
		int len = parameters.length;
		Object[] args = new Object[len];
		for(int i = 0; i < len; i++){
			Class<?> clazz = parameters[i].getType();
			if(clazz.isPrimitive()){
				args[i] = getPrimitiveParameter(clazz.getName());
				continue;
			}
			if(clazz.isAssignableFrom(HapiHttpRequest.class)) {
				args[i] = request;
				continue;
			}
			if(clazz.isAssignableFrom(HapiHttpResponse.class)) {
				args[i] = response;
				continue;
			}
			args[i] = null;
		}
		try {
			router.getMd().invoke(router.getClazz(), args);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onError(HttpMethod fromMethod, DefaultHapiHttpResponseImpl response, Exception e) {
		// TODO Auto-generated method stub
		e.printStackTrace();
	}
	
	private Object getPrimitiveParameter(String name){
		if(name.equals("int")){
			return 0;
		}
		if(name.equals("boolean")){
			return false;
		}
		if(name.equals("short")){
			return (short)0;
		}
		if(name.equals("long")){
			return 0l;
		}
		if(name.equals("double")){
			return 0d;
		}
		if(name.equals("char")){
			return '0';
		}
		if(name.equals("byte")){
			return (byte)0;
		}
		if(name.equals("float")){
			return 0f;
		}
		return new Object();
	}
}
