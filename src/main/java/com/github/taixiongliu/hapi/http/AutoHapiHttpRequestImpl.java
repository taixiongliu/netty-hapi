package com.github.taixiongliu.hapi.http;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

/**
 * <b>Automatic parse request uri and parameter</b>
 * <br><br>
 * parse request body text with JSON or normal
 * @author taixiong.liu
 *
 */
public class AutoHapiHttpRequestImpl extends BaseHapiHttpRequestImpl{

	@Override
	public Map<String, String> parseParameter(String content) throws HttpUrlErrorException {
		// TODO Auto-generated method stub
		if(content == null || content.equals("")){
			return new HashMap<String, String>();
		}
		//support JSON branch
		boolean isJson = true;
		JSONObject jo = null;
    	try {
    		jo = JSONObject.parseObject(content);
		} catch (Exception e) {
			// TODO: handle exception
			isJson = false;
		}
    	if(isJson){
    		return parseFromJson(jo);
    	}
		return getParameterMapByUrlParameter(content);
	}
	
	/**
	 * parse JSON
	 * @param jo
	 * @return map
	 */
	private Map<String, String> parseFromJson(JSONObject jo){
		if(jo == null){
			return new HashMap<String, String>();
		}
		Map<String, String> map = new HashMap<String, String>();
		for(String key : jo.keySet()){
			map.put(key, jo.getString(key));
		}
		return map;
	}
}
