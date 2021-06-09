package com.github.taixiongliu.hapi.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.github.taixiongliu.hapi.route.HapiRouteType;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * <b>Response status object</b>
 * <br><br>
 * response context
 * @author taixiong.liu
 * 
 */
public class DefaultHapiHttpResponseImpl implements HapiHttpResponse{
	private HttpResponseStatus status;
	private String content;
	private Map<String, String> heads;
	private HapiRouteType routeType;
	private FileOutputStream fos;
	private boolean isStream;
	private Random random;
	private String verify = "0123456789abcdefghijkmnopqrstuvwxyz";
	private File tempFile;
	private String contentType;
	public DefaultHapiHttpResponseImpl() {
		// TODO Auto-generated constructor stub
		this(null, null);
	}
	public DefaultHapiHttpResponseImpl(HttpResponseStatus status, String content) {
		// TODO Auto-generated constructor stub
		this.status = status;
		this.content = content;
		random = new Random();
		heads = new ConcurrentHashMap<String, String>();
		routeType = HapiRouteType.BODY;
	}
	
	public HttpResponseStatus getStatus() {
		return status;
	}

	public String getContent() {
		return content;
	}
	
	public Map<String, String> heads(){
		return heads;
	}

	public HapiRouteType getRouteType() {
		return routeType;
	}
	public void setRouteType(HapiRouteType routeType) {
		this.routeType = routeType;
	}
	
	public String getContentType(){
		return contentType;
	}
	public File getOutputStreamFile(){
		return tempFile;
	}
	public OutputStream getOutputStream(String contentType) {
		if(contentType == null || contentType.trim().equals("")){
			return null;
		}
		isStream = true; 
		this.contentType = contentType;
		
		String fileName = getFileName();
		String filePath = "tempFile/"+fileName;
        
		try {
			File path = new File("tempFile");
			if(!path.exists()){
				path.mkdir();
			}
			
			File temp = new File(filePath);
			boolean isExists = false;
			//if exists, try again
			if(temp.exists()){
				fileName = getFileName();
				filePath = "tempFile/"+fileName;
				temp = new File(filePath);
				//give up.
				if(temp.exists()){
					isExists = true;
				}
			}
			if(!isExists){
				fos = new FileOutputStream(temp);
				tempFile = temp;
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fos;
	}
	
	public boolean isStream(){
		return isStream;
	}
	
	public void closeStream(){
		if(!isStream){
			return ;
		}
		if(fos == null){
			return ;
		}
		try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tempFile.delete();
	}
	
	public void setStatus(HttpResponseStatus status) {
		// TODO Auto-generated method stub
		this.status = status;
	}
	public void setContent(String content) {
		// TODO Auto-generated method stub
		this.content = content;
	}
	public void setHead(String name, String value) {
		// TODO Auto-generated method stub
		heads.put(name, value);
	}
	
	private String getFileName(){
		StringBuilder sb = new StringBuilder();
		sb.append(System.currentTimeMillis()).append("_");
		for(int i = 0; i < 16; i++){
			sb.append(verify.charAt(random.nextInt(verify.length())));
		}
		return sb.toString();
	}
	
}
