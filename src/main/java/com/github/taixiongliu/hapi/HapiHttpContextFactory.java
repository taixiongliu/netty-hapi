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
import com.github.taixiongliu.hapi.tc.ThreadError;

import io.netty.handler.codec.http.HttpResponseStatus;

import com.github.taixiongliu.hapi.tc.ThreadContainer;
import com.github.taixiongliu.hapi.tc.ThreadController;

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
	
	private Map<String, Router> routeMap;
	private Map<String, Router> dynamicRouteMap;
	private Router root;
	private Map<String, Router> pathRoute;
    private KeystoreEntity entity;
    private AutowiredHandler autowiredHandler;
    private String rootPath;
    private String uploadPath;
    private String cachePath;
    private Integer maxLength;
    private Set<String> versions;
    private boolean enableThreadController;
    private boolean pause;
    private ThreadError pauseError;
	private HapiHttpContextFactory() {
		// TODO Auto-generated constructor stub
		//取消使用ConcurrentHashMap
		routeMap = new HashMap<String, Router>();
		dynamicRouteMap = new HashMap<String, Router>();
		pathRoute = new HashMap<String, Router>();
		root = null;
		entity = null;
		autowiredHandler = null;
		versions = new HashSet<String>();
		enableThreadController = false;
		pause = false;
		pauseError = new ThreadError(HttpResponseStatus.BAD_GATEWAY, null, 556, null, "server pause now");
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
	public HapiHttpContextFactory buildThreadController(ThreadContainer container) {
		return buildThreadController(container, null);
	}
	public HapiHttpContextFactory buildThreadController(ThreadContainer container, ThreadError busyError) {
		boolean res = ThreadController.getInstance().initController(container, busyError);
		if(!res) {
			System.out.println("Build Thread Controller error.");
			return this;
		}
		enableThreadController = true;
		return this;
	}
	public HapiHttpContextFactory buildPauseError(ThreadError pauseError) {
		if(pauseError == null) {
			return this;
		}
		this.pauseError = pauseError;
		return this;
	}
	public boolean release() {
		if(!enableThreadController) {
			return true;
		}
		return ThreadController.getInstance().getPointer();
	}
	public boolean pause() {
		return pause;
	}
	public void setPause(boolean pause) {
		this.pause = pause;
	}
	public ThreadError getPauseError() {
		return pauseError;
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
		Router temp = routeMap.get(url+"_"+method);
		//method all.
		if(temp == null) {
			temp = routeMap.get(url+"_"+HapiHttpMethod.ALL.getName());
		}
		//dynamic router.
		if(temp == null){
			temp = dynamicRouter(url, method);
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
	
	public Router getProxyRouter(String url){
		VersionRouter versionRouter = versionRouter(url);
		if(versionRouter != null){
			url = versionRouter.getUrl();
		}
		Router temp = pathRouter(url);
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
	 * check dynamic router.
	 * @return Object Router
	 */
	private Router dynamicRouter(String url, String method){
		//normal path must start with '/', value length must greater than or equal to 1. 
		if(url == null || !url.startsWith("/")){
			return null;
		}
		if(dynamicRouteMap == null || dynamicRouteMap.isEmpty()) {
			return null;
		}
		url = url.substring(1, url.length());
		// match router.
		List<Router> temp = new ArrayList<Router>();
		for(String key : dynamicRouteMap.keySet()){
			Router router = dynamicRouteMap.get(key);
			//not dynamic route.
			if(router.getPathParameters() < 1) {
				continue;
			}
			
			String path = router.getPath().substring(1, router.getPath().length());
			// only one dynamic parameter.
			if(!url.contains("/")) {
				if(path.equals("{*}")) {
					if(router.getHttpMethod().getName().equals(HapiHttpMethod.ALL.getName()) || router.getHttpMethod().getName().equals(method)) {
						router.setPathParameterValues(new String[] {url});
						temp.add(router);
					}
				}
				continue;
			}
			//parameter must more than 1.
			if(!path.contains("/")) {
				continue;
			}
			
			//split and match every one.
			String[] pathArr = path.split("/");
			String[] urlArr = url.split("/");
			if(pathArr.length != urlArr.length) {
				continue;
			}
			boolean match = true;
			List<String> values = new ArrayList<String>();
			for(int i = 0; i < pathArr.length; i ++) {
				if(pathArr[i].equals("{*}")) {
					values.add(urlArr[i]);
					continue;
				}
				if(!pathArr[i].equals(urlArr[i])) {
					match = false;
					break;
				}
			}
			// match pass.
			if(match) {
				if(router.getHttpMethod().getName().equals(HapiHttpMethod.ALL.getName()) || router.getHttpMethod().getName().equals(method)) {
					String[] varr = new String[values.size()];
					router.setPathParameterValues(values.toArray(varr));
					temp.add(router);
				}
			}
		}
		
		if(temp.isEmpty()) {
			return null;
		}
		
		//sort and get length max of prefix.
		Router finalRouter = temp.get(0);
		// index start with 1.
		for(int j = 1; j < temp.size(); j ++) {
			int index = temp.get(j).getPath().indexOf("{*}");
			if(index > finalRouter.getPath().indexOf("{*}")) {
				//reset cache values.
				finalRouter.setPathParameterValues(null);
				finalRouter = temp.get(j);
			}else {
				//reset cache values.
				temp.get(j).setPathParameterValues(null);
			}
		}
		return finalRouter;
	}
	
	/**
	 * check path router.
	 * @return Object Router
	 */
	private Router pathRouter(String url){
		if((pathRoute == null|| pathRoute.isEmpty()) && root == null){
			return null;
		}
		//normal path must start with '/', value length must greater than or equal to 1. 
		if(url == null || url.length() < 1){
			return null;
		}
		Router temp = null;
		for(String key : pathRoute.keySet()){
			Router router = pathRoute.get(key);
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
		
		//Dynamic Router.
		if(router.getPath().contains("{")) {
			String dynamicPath = router.getPath().substring(1, router.getPath().length());
			if(dynamicPath.contains("/")) {
				String[] dynamicArr = dynamicPath.split("/");
				StringBuilder tempPath = new StringBuilder();
				List<String> tempParmeterName = new ArrayList<String>();
				for(String p : dynamicArr) {
					tempPath.append("/");
					if(p.contains("{")) {
						String parmeterName = getDynamicParmeter(p);
						if(parmeterName == null) {
							throw new RouteException(clazz.getName()+": route '"+router.getPath()+"' dynamic parmeter error.");
						}
						tempParmeterName.add(parmeterName);
						tempPath.append("{*}");
					}else {
						tempPath.append(p);
					}
				}
				router.setPath(tempPath.toString());
				router.setPathParameters(tempParmeterName.size());
				String[] arr = new String[tempParmeterName.size()];
				router.setPathParameterNames(tempParmeterName.toArray(arr));
			}else {//only one dynamic parameter.
				router.setPath("/{*}");
				router.setPathParameters(1);
				String[] arr = new String[1];
				arr[0] = getDynamicParmeter(dynamicPath);
				router.setPathParameterNames(arr);
			}
		}
		
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
		// insert into dynamic map
		if(router.getPathParameters() > 0) {
			if(dynamicRouteMap.get(router.getPath()+"_"+httpMethod.getName()) != null){
				throw new RouteException(clazz.getName()+": dynamic route '"+router.getPath()+"' was registed by other package.");
			}
			dynamicRouteMap.put(router.getPath()+"_"+httpMethod.getName(), router);
		}else {
			if(routeMap.get(router.getPath()+"_"+httpMethod.getName()) != null){
				throw new RouteException(clazz.getName()+": route '"+router.getPath()+"' was registed by other package.");
			}
			routeMap.put(router.getPath()+"_"+httpMethod.getName(), router);
		}
	}
	
	private String getDynamicParmeter(String path) {
		if(!path.startsWith("{")) {
			return null;
		}
		if(!path.endsWith("}")) {
			return null;
		}
		return path.substring(1, path.length() - 1);
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
		if(pathRoute.get(router.getPath()) != null){
			throw new RouteException(clazz.getName()+": proxy '"+router.getPath()+"' was registed by other package.");
		}
		pathRoute.put(router.getPath(), router);
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
