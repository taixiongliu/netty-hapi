package com.github.taixiongliu.hapi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.taixiongliu.hapi.dom.DOMParser;
import com.github.taixiongliu.hapi.http.BaseHapiHttpRequestImpl;
import com.github.taixiongliu.hapi.netty.NettyHttpServer;
import com.github.taixiongliu.hapi.route.ClassScanner;
import com.github.taixiongliu.hapi.route.HapiHttpMethod;
import com.github.taixiongliu.hapi.route.HapiRouteType;
import com.github.taixiongliu.hapi.route.RequestMapping;
import com.github.taixiongliu.hapi.route.Route;
import com.github.taixiongliu.hapi.route.Router;
import com.github.taixiongliu.hapi.ssl.KeystoreEntity;

/**
 * <b>Scan annotation create and cache router instance</b>
 * @author taixiong.liu
 *
 */
public class HapiHttpContextFactory {
	private static HapiHttpContextFactory factory = null;
	private static Object look = new Object();
	public static HapiHttpContextFactory getInstance(){
		if(factory != null){
			return factory;
		}
		synchronized (look) {
			if(factory == null){
				factory = new HapiHttpContextFactory();
			}
		}
		return factory;
	}
	
	private Map<String, Router> map;
    private KeystoreEntity entity;
	private HapiHttpContextFactory() {
		// TODO Auto-generated constructor stub
		map = new ConcurrentHashMap<String, Router>();
		entity = null;
	}
	
	public HapiHttpContextFactory buildHttps(KeystoreEntity entity){
    	this.entity = entity;
    	
    	return this;
    } 
	
	/**
	 * <b>create HAPI context, default server port 8100</b>
	 * @param context configuration file name
	 */
	public void createContext(String context){
		this.createContext(context, null);
	}
	
	/**
	 * <b>create HAPI context, default server port 8100</b>
	 * @param context configuration file name
	 * @param clazz parse request realization extends {@link com.github.taixiongliu.hapi.http.BaseHapiHttpRequestImpl} class
	 */
	public void createContext(String context, Class<? extends BaseHapiHttpRequestImpl> clazz){
		DOMParser parser = new DOMParser();
		Map<String, String> map = parser.parseMap(context);
		if(map == null){
			map = new HashMap<String, String>();
		}
		String mport = map.get("context:port");
		String mpackage = map.get("context:route-scan-package");
		int port = mport == null ? 8100 : Integer.parseInt(mport);
		if(mpackage != null && !mpackage.trim().equals("")){
			//scan route
			loadRoute(mpackage);
		}
		
		try {
			
			new NettyHttpServer(port, new HapiHttpService(), clazz).buildHttps(entity).run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Router getRouter(String url){
		return map.get(url);
	}
	
	private void loadRoute(String mpackage){
		List<Class<?>> classes = ClassScanner.getClassesWithPackageName(mpackage);
		for (Class<?> clazz : classes) {
			if(clazz == null){
				continue;
			}
			Annotation[] annotations = clazz.getAnnotations();
			if(annotations == null || annotations.length < 1){
				continue;
			}
			for (int i = 0; i < annotations.length; i++) {
				if(annotations[i] instanceof Route){
					Route route = (Route) annotations[i];
					scanRoute(clazz,route.value());
					break;
				}
			}
		}
	}
	
	private void scanRoute(Class<?> clazz, String route){
		Method[] meds = clazz.getMethods();
		for (Method med : meds) {
			Annotation[] annotations = med.getAnnotations();
			if(annotations == null || annotations.length < 1){
				continue;
			}
			for (Annotation annotation : annotations) {
				if(annotation instanceof RequestMapping){
					RequestMapping mapping = (RequestMapping) annotation;
					addRouter(clazz, med, route, mapping.value(), mapping.method(),mapping.type());
					break;
				}
			}
		}
	}
	
	private void addRouter(Class<?> clazz, Method med,String route, String position, HapiHttpMethod httpMethod, HapiRouteType routeType){
		if(position == null || position.trim().equals("")){
			return ;
		}
		if(route == null){
			route = "";
		}
		if(route.contains("/")){
			route.replace("/", "");
		}
		if(position.contains("/")){
			position.replace("/", "");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		if(!route.equals("")){
			sb.append(route)
			.append("/");
		}
		sb.append(position);
		
		Router router = new Router();
		router.setPath(sb.toString());
		router.setMd(med);
		try {
			router.setClazz(clazz.newInstance());
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		router.setPosition(position);
		router.setHttpMethod(httpMethod);
		router.setRouteType(routeType);
		map.put(router.getPath(), router);
	}
}
