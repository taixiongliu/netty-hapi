package com.github.taixiongliu.hapi;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.taixiongliu.hapi.autowired.Autowired;
import com.github.taixiongliu.hapi.autowired.AutowiredField;
import com.github.taixiongliu.hapi.autowired.AutowiredHandler;
import com.github.taixiongliu.hapi.dom.DOMParser;
import com.github.taixiongliu.hapi.exception.RouteException;
import com.github.taixiongliu.hapi.http.BaseHapiHttpRequestImpl;
import com.github.taixiongliu.hapi.netty.NettyHttpServer;
import com.github.taixiongliu.hapi.route.ClassScanner;
import com.github.taixiongliu.hapi.route.DeleteRequestMapping;
import com.github.taixiongliu.hapi.route.GetRequestMapping;
import com.github.taixiongliu.hapi.route.HapiHttpMethod;
import com.github.taixiongliu.hapi.route.HapiRouteType;
import com.github.taixiongliu.hapi.route.PostRequestMapping;
import com.github.taixiongliu.hapi.route.ProxyMapping;
import com.github.taixiongliu.hapi.route.PutRequestMapping;
import com.github.taixiongliu.hapi.route.RequestMapping;
import com.github.taixiongliu.hapi.route.Route;
import com.github.taixiongliu.hapi.route.Router;
import com.github.taixiongliu.hapi.route.VersionRouter;
import com.github.taixiongliu.hapi.ssl.KeystoreEntity;

/**
 * <b>Scan annotation create and cache router instance</b>
 * @author taixiong.liu
 *
 */
public class HapiHttpContextFactory {
	public final static int default_port = 8100;
	public final static String default_root_path = "webcontent";
	public final static String default_upload_path = "uploads";
	public final static String default_cache_path = "cache";
	public final static int default_max_receive = 4194304;
	
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
    private AutowiredHandler autowiredHandler;
    private String rootPath;
    private String uploadPath;
    private String cachePath;
    private Integer maxLength;
    private Set<String> versions;
	private HapiHttpContextFactory() {
		// TODO Auto-generated constructor stub
		//取消使用ConcurrentHashMap
		map = new HashMap<String, Router>();
		path = new HashMap<String, Router>();
		root = null;
		entity = null;
		autowiredHandler = null;
		versions = new HashSet<String>();
	}
	
	public HapiHttpContextFactory buildHttps(KeystoreEntity entity){
    	this.entity = entity;
    	
    	return this;
    }
	public HapiHttpContextFactory buildAutowired(AutowiredHandler handler){
    	this.autowiredHandler = handler;
    	
    	return this;
    }
	public HapiHttpContextFactory buildVersion(String version){
		addVersion(version);

    	return this;
	}
	
	/**
	 * <b>create HAPI context, default server port 8100</b>
	 * @param context configuration file name
	 */
	public void createContext(String context){
		this.createContext(context, null , null);
	}
	
	/**
	 * <b>create HAPI context, default server port 8100</b>
	 * @param context configuration file name
	 * @param defPackage package of route scan
	 */
	public void createContext(String context, String defPackage){
		this.createContext(context, defPackage , null);
	}
	
	public void createContext(String context, Class<? extends BaseHapiHttpRequestImpl> clazz){
		this.createContext(context, null , clazz);
	}
	
	/**
	 * <b>create HAPI context, default server port 8100</b>
	 * @param context configuration file name
	 * @param clazz parse request realization extends {@link com.github.taixiongliu.hapi.http.BaseHapiHttpRequestImpl} class
	 */
	public void createContext(String context, String defPackage, Class<? extends BaseHapiHttpRequestImpl> clazz){
		DOMParser parser = new DOMParser();
		Map<String, String> map = parser.parseMap(context);
		if(map == null){
			map = new HashMap<String, String>();
		}
		StringBuilder sb = new StringBuilder();
		if(defPackage != null && !defPackage.trim().equals("")){
			sb.append(defPackage);
		}
		
		String mport = map.get("context:port");
		String mpackage = map.get("context:route-scan-package");
		rootPath = map.get("context:root-path");
		uploadPath = map.get("context:upload-path");
		cachePath = map.get("context:cache-path");
		int maxReceive = default_max_receive;
		try {
			maxReceive = Integer.valueOf(map.get("context:receive-max-length"));
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		maxLength = null;		
		try {
			maxLength = Integer.valueOf(map.get("context:upload-max-length"));
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		if(rootPath == null || rootPath.trim().equals("")){
			rootPath = default_root_path;
		}
		if(uploadPath == null || uploadPath.trim().equals("")){
			uploadPath = default_upload_path;
		}
		if(cachePath == null || cachePath.trim().equals("")){
			cachePath = default_cache_path;
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
		
		int port = mport == null ? default_port : Integer.parseInt(mport);
		if(mpackage != null && !mpackage.trim().equals("")){
			sb.append(";");
			sb.append(mpackage);
		}
		String strPackage = sb.toString();
		if(strPackage != null && !strPackage.trim().equals("")){
			//scan route
			if(strPackage.contains(";")){
				String[] arr = strPackage.split(";");
				for(String pk : arr){
					if(pk == null || pk.trim().equals("")){
						continue;
					}
					loadRoute(pk);
				}
			}else{
				loadRoute(strPackage);
			}
		}
		
		try {
			NettyHttpServer server = new NettyHttpServer(port, new HapiHttpService(), uploadPathString, clazz).buildHttps(entity);
			server.setMaxReceiveLength(maxReceive);
			server.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Router getRouter(String url, String method){
		VersionRouter versionRouter = versionRouter(url);
		if(versionRouter != null){
			url = versionRouter.getUrl();
		}
		Router temp = map.get(url+"_"+method);
		if(temp == null){
			temp = pathRouter(url);
		}
		if(temp == null){
			return null;
		}
		if(versionRouter != null){
			temp.setVersion(versionRouter.getVersion());
			temp.setReUrl(url);
		}
		return temp;
	}
	
	public String getRootPath(){
		return rootPath;
	}
	public String getUploadPath(){
		return uploadPath;
	}
	public String getCachePath() {
		return cachePath;
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
		//normal path must start with '/', value length must greater than or equal to 1. 
		if(url == null || url.length() < 1){
			return null;
		}
		Router temp = null;
		for(String key : path.keySet()){
			Router router = path.get(key);
			String path = router.getPath();
			int pathLen = path.length();
			//all path
			if(path.endsWith("*/")){
				pathLen -= 2;
				path = path.substring(0, pathLen);
			}
			if(url.length() < pathLen){
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
	
	/**
	 * check version router.
	 * @return Object Router
	 */
	private VersionRouter versionRouter(String url){
		if((versions == null || versions.isEmpty())){
			return null;
		}
		//normal path must start with '/', value length must greater than or equal to 1. 
		if(url == null || url.length() < 1){
			return null;
		}
		StringBuilder versionSuffix = new StringBuilder();
		String versionValue = null;
		String reUrl = url;
		for(String version : versions){
			versionSuffix.append("/").append(version).append("/");
			String temp = versionSuffix.toString();
			if(url.length() < temp.length()){
				//clear
				versionSuffix.setLength(0);
				continue;
			}
			if(url.startsWith(temp)){
				versionValue = version;
				// keep separator
				reUrl = url.substring(temp.length() - 1, url.length());
				break;
			}
			
			//clear
			versionSuffix.setLength(0);
		}
		if(versionValue == null){
			return null;
		}
		return new VersionRouter(versionValue, reUrl);
	}
	
	private void scanRoute(Class<?> clazz, String route){
		AutowiredField[] fields = null;
		if(autowiredHandler != null){
			Field[] array = clazz.getDeclaredFields();
			List<AutowiredField> list = new ArrayList<AutowiredField>();
			for(Field field : array){
				Annotation[] annotations = field.getAnnotations();
				if(annotations == null || annotations.length < 1){
					continue;
				}
				for (Annotation annotation : annotations) {
					if(annotation instanceof Autowired){
						Autowired anno = (Autowired) annotation;
						list.add(new AutowiredField(field, anno.value()));
					}
				}
			}
			fields = new AutowiredField[list.size()];
			list.toArray(fields);
		}
		Method[] meds = clazz.getMethods();
		for (Method med : meds) {
			Annotation[] annotations = med.getAnnotations();
			if(annotations == null || annotations.length < 1){
				continue;
			}
			for (Annotation annotation : annotations) {
				if(annotation instanceof RequestMapping){
					RequestMapping mapping = (RequestMapping) annotation;
					try {
						addRouter(clazz, fields, med, route, mapping.value(), mapping.method(), mapping.type());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
				if(annotation instanceof GetRequestMapping){
					GetRequestMapping mapping = (GetRequestMapping) annotation;
					try {
						addRouter(clazz, fields, med, route, mapping.value(), HapiHttpMethod.GET, mapping.type());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
				if(annotation instanceof PostRequestMapping){
					PostRequestMapping mapping = (PostRequestMapping) annotation;
					try {
						addRouter(clazz, fields, med, route, mapping.value(), HapiHttpMethod.POST, mapping.type());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
				if(annotation instanceof PutRequestMapping){
					PutRequestMapping mapping = (PutRequestMapping) annotation;
					try {
						addRouter(clazz, fields, med, route, mapping.value(), HapiHttpMethod.PUT, mapping.type());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
				if(annotation instanceof DeleteRequestMapping){
					DeleteRequestMapping mapping = (DeleteRequestMapping) annotation;
					try {
						addRouter(clazz, fields, med, route, mapping.value(), HapiHttpMethod.DELETE, mapping.type());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
				if(annotation instanceof ProxyMapping){
					ProxyMapping mapping = (ProxyMapping) annotation;
					try {
						addPathRouter(clazz, fields, med, route, mapping.value(), mapping.method(), mapping.type());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
		}
	}
	
	private synchronized void addRouter(Class<?> clazz, AutowiredField[] fields, Method med,String route, String position, HapiHttpMethod httpMethod, HapiRouteType routeType) throws Exception{
		if(position == null){
			return ;
		}
		if(route == null){
			route = "";
		}
		if(route.startsWith("/")){
			route = route.substring(1, route.length());
		}
		if(route.endsWith("/")){
			route = route.substring(0, route.length() - 1);
		}
		if(position.startsWith("/")){
			position = position.substring(1, position.length());
		}
		if(position.endsWith("/")){
			position = position.substring(0, position.length() - 1);
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
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			Object instance = constructor.newInstance();
			
			if(fields != null && fields.length > 0 && autowiredHandler != null){
				for(AutowiredField autowiredField : fields){
					Field field = autowiredField.getField();
					Object obj = autowiredHandler.onAutowired(field.getType(), autowiredField.getValue());
					if(obj != null){
						field.setAccessible(true);
						field.set(instance, obj);
					}
				}
			}
			router.setClazz(instance);
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
		if(map.get(router.getPath()+"_"+httpMethod.getName()) != null){
			throw new RouteException(clazz.getName()+": route '"+router.getPath()+"' was registed by other package.");
		}
		map.put(router.getPath()+"_"+httpMethod.getName(), router);
	}
	
	private synchronized void addPathRouter(Class<?> clazz, AutowiredField[] fields, Method med,String route, String position, HapiHttpMethod httpMethod, HapiRouteType routeType) throws Exception{
		if(position == null){
			position = "";
		}
		if(route == null){
			route = "";
		}
		if(route.startsWith("/")){
			route = route.substring(1, route.length());
		}
		if(route.endsWith("/")){
			route = route.substring(0, route.length() - 1);
		}
		if(position.startsWith("/")){
			position = position.substring(1, position.length());
		}
		if(position.endsWith("/")){
			position = position.substring(0, position.length() - 1);
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
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			Object instance = constructor.newInstance();
			//Object instance = clazz.newInstance();
			if(fields != null && fields.length > 0 && autowiredHandler != null){
				for(AutowiredField autowiredField : fields){
					Field field = autowiredField.getField();
					Object obj = autowiredHandler.onAutowired(field.getType(), autowiredField.getValue());
					if(obj != null){
						field.setAccessible(true);
						field.set(instance, obj);
					}
				}
			}
			router.setClazz(instance);
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
			if(root != null){
				throw new RouteException(clazz.getName()+": root proxy '"+router.getPath()+"' was registed by other package.");
			}
			root = router;
			return ;
		}
		if(path.get(router.getPath()) != null){
			throw new RouteException(clazz.getName()+": proxy '"+router.getPath()+"' was registed by other package.");
		}
		path.put(router.getPath(), router);
	}
	
	private synchronized void addVersion(String version){
		if(version == null || version.trim().equals("")){
			return ;
		}
		if(version.startsWith("/")){
			version = version.substring(1, version.length());
		}
		if(version.endsWith("/")){
			version = version.substring(0, version.length() - 1);
		}
		versions.add(version);
	}
}
