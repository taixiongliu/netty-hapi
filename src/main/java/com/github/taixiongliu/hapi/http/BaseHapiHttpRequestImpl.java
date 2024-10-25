package com.github.taixiongliu.hapi.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

/**
 * <b>Parse request uri and parameter</b>
 * @author taixiong.liu
 *
 */
public abstract class BaseHapiHttpRequestImpl implements HapiHttpRequest{
	private String url;
	private HttpRequest request;
	private String ip;
	private Map<String, String> parameters;
	private String version;
	private String reUrl;
	private HttpMethod method;
	private String[] pathParameters;
	private String[] pathValues;
	
	public abstract Map<String, String> parseParameter(String content) throws HttpUrlErrorException;
	
	public BaseHapiHttpRequestImpl() {
		// TODO Auto-generated constructor stub
		parameters = new HashMap<String, String>();
		
	}
	public void initUrl(HttpRequest request) throws HttpUrlErrorException{
		this.request = request;
		init(request.uri());
	} 
	private void init(String uri) throws HttpUrlErrorException{
		if(uri == null){
			return ;
		}
		//get url and filter parameter 
		if(uri.contains("?")){
			String[] strs = uri.split("\\?");
			if(strs.length > 2){
				throw new HttpUrlErrorException("invalid parameter of too many characters '?'");
			}
			url = strs[0];
			if(strs.length == 2){
				parameters = getParameterMapByUrlParameter(strs[1]);
			}
			return ;
		}
		url = uri;
	}
	
	public void addParameters(Map<String, String> map){
		parameters.putAll(map);
	}
	
	/**
	 * <b>default request parse method</b>
	 * 
	 * @param urlParameter string of parameter text, linked with character {@code '=' and '&'}
	 * @return parameter map list
	 * @throws HttpUrlErrorException invalid url
	 */
	protected Map<String, String> getParameterMapByUrlParameter(String urlParameter) throws HttpUrlErrorException{
		if(urlParameter == null || urlParameter.equals("")){
			return new HashMap<String, String>();
		}
		
		if(!urlParameter.contains("&")){
			return getMapByStr(new String[]{urlParameter});
		}
		String[] parakvs = urlParameter.split("&");
		return getMapByStr(parakvs);
	}
	
	private Map<String, String> getMapByStr(String[] strs) throws HttpUrlErrorException{
		Map<String, String> map = new HashMap<String, String>();
		boolean isValid = true; 
		for(String parakv : strs){
			if(!parakv.contains("=")){
				isValid = false;
				break;
			}
			String[] para = parakv.split("=");
			//parameter not empty
			if(para[0].trim().equals("")){
				isValid = false;
				break;
			}
			String value = "";
			if(para.length > 1){
				value = para[1];
			}
			String temp = "";
			try {
				temp = URLDecoder.decode(value,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			map.put(para[0], temp);
		}
		if(!isValid){
			map = null;
			throw new HttpUrlErrorException("invalid url format error");
		}
		return map;
	}
	
	public String getPathParameter(String pathParamName) {
		if(pathParameters == null || pathParameters.length < 1) {
			return null;
		}
		int index = -1;
		for(int i = 0; i < pathParameters.length; i ++) {
			if(pathParameters[i].equals(pathParamName)) {
				index = i;
				break;
			}
		}
		if(index == -1) {
			return null;
		}
		return pathValues[index];
	}
	
	public void setIpAddress(String ip){
		this.ip = ip;
	} 
	
	public String getUrl() {
		// TODO Auto-generated method stub
		return url;
	}
	public String getIpAddress() {
		// TODO Auto-generated method stub
		return ip;
	}
	public String getParameter(String paramName) {
		// TODO Auto-generated method stub
		return parameters.get(paramName);
	}
	public String uri() {
		// TODO Auto-generated method stub
		return request.uri();
	}
	public HttpHeaders heads() {
		// TODO Auto-generated method stub
		return request.headers();
	}
	public HttpMethod getMethod() {
		return method;
	}
	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getReUrl() {
		return reUrl;
	}

	public void setReUrl(String reUrl) {
		this.reUrl = reUrl;
	}

	public void setPathParameters(String[] pathParameters) {
		this.pathParameters = pathParameters;
	}

	public void setPathValues(String[] pathValues) {
		this.pathValues = pathValues;
	}
}
