package test.controller;

import com.github.taixiongliu.hapi.http.HapiHttpRequest;
import com.github.taixiongliu.hapi.http.HapiHttpResponse;
import com.github.taixiongliu.hapi.route.ProxyMapping;
import com.github.taixiongliu.hapi.route.RequestMapping;
import com.github.taixiongliu.hapi.route.Route;

/** 
 * @author taixiong.liu
 */
@Route("controller")
public class Controller {
	@RequestMapping("index.html")
	public void index(HapiHttpRequest request, HapiHttpResponse response){
		response.setContent("controller index page.");
	}
	@ProxyMapping("*")
	public void all(HapiHttpRequest request, HapiHttpResponse response){
		response.setContent("controller all page.>>>>>>");
	}
}
