package com.github.taixiongliu.hapi.route;
/** 
 * @author taixiong.liu
 */
public class VersionRouter {
	private String version;
	private String url;
	public VersionRouter(String version, String url) {
		// TODO Auto-generated constructor stub
		this.version = version;
		this.url = url;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
