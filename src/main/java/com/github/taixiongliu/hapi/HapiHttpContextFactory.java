package com.github.taixiongliu.hapi;

import java.io.File;
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
	private Router root;
	private Map<String, Router> path;
    private KeystoreEntity entity;
    private String rootPath;
    private String uploadPath;
    private Integer maxLength;
	private HapiHttpContextFactory() {
		// TODO Auto-generated constructor stub
		map = new ConcurrentHashMap<String, Router>();
		path = new ConcurrentHashMap<String, Router>();
		root = null;
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
		rootPath = map.get("context:root-path");
		uploadPath = map.get("context:upload-path");
		maxLength = null;
		try {
			maxLength = new Integer(map.get("context:upload-max-length"));
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		if(rootPath == null || rootPath.trim().equals("")){
			rootPath = "webcontent";
		}
		if(uploadPath == null || uploadPath.trim().equals("")){
			uploadPath = "uploads";
		}
		//create web root path
		File rp = new File(rootPath);
		if(!rp.exists()){
			rp.mkdir();
		}
		//create file upload path
		String uploadPathString = rootPath+File.separatorChar+uploadPath+File.separatorChar;
		File up = new File(uploadPathString);
		if(!up.exists()){
			up.mkdir();
		}
		
		int port = mport == null ? 8100 : Integer.parseInt(mport);
		if(mpackage != null && !mpackage.trim().equals("")){
			//scan route
			loadRoute(mpackage);
		}
		
		try {
			new NettyHttpServer(port, new HapiHttpService(), uploadPathString, clazz).buildHttps(entity).run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Router getRouter(String url){
		Router temp = map.get(url);
		if(temp == null){
			return pathRouter(url);
		}
		return map.get(url);
	}
	
	public String getRootPath(){
		return rootPath;
	}
	public String getUploadPath(){
		return uploadPath;
	}
	public Integer getMaxLength(){
		return maxLength;
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
	
	/**
	 * check path router.
	 * @return Object Router
	 */
	private Router pathRouter(String url){
		if((path == null|| path.isEmpty()) && root == null){
			return null;
		}
		//normal path must start with '/[value]', value length must greater than or equal to 1. 
		if(url == null || url.length() < 2){
			return null;
		}
		Router temp = null;
		for(String key : path.keySet()){
			Router router = path.get(key);
			String path = router.getPath();
			int pathLen = path.length();
			if(url.length() <= pathLen){
				continue;
			}
			if(url.substring(0, pathLen).equals(path)){
				temp = router;
				break;
			}
		}
		//root path
		if(temp == null){
			return root;
		}
		return temp;
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
					if(mapping.type().equals(HapiRouteType.PATH)){
						addPathRouter(clazz, med, route, mapping.value(), mapping.method(),mapping.type());
					}else{
						addRouter(clazz, med, route, mapping.value(), mapping.method(),mapping.type());
					}
					break;
				}
			}
		}
	}
	
	private void addRouter(Class<?> clazz, Method med,String route, String position, HapiHttpMethod httpMethod, HapiRouteType routeType){
		if(position == null){
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
	
	private void addPathRouter(Class<?> clazz, Method med,String route, String position, HapiHttpMethod httpMethod, HapiRouteType routeType){
		if(position == null){
			position = "";
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
		if(!position.equals("")){
			sb.append(position)
			.append("/");
		}
		
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
		if(router.getPath().equals("/") || router.getPath().equals("/*/")){
			root = router;
			return ;
		}
		path.put(router.getPath(), router);
	}
}
