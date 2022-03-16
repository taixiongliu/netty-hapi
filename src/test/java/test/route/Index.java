package test.route;

import com.github.taixiongliu.hapi.autowired.Autowired;
import com.github.taixiongliu.hapi.http.HapiHttpRequest;
import com.github.taixiongliu.hapi.http.HapiHttpResponse;
import com.github.taixiongliu.hapi.route.ProxyMapping;
import com.github.taixiongliu.hapi.route.RequestMapping;
import com.github.taixiongliu.hapi.route.Route;

/** 
 * @author taixiong.liu
 */
@Route
public class Index {
	@Autowired
	private String name;
	
	@RequestMapping("index.html")
	public void index(HapiHttpRequest request, HapiHttpResponse response){
		System.out.println(name);
		response.setContent("index page.");
	}
	
	@ProxyMapping(value="*")
	public void all(HapiHttpRequest request, HapiHttpResponse response){
		response.setContent("all page."+request.getUrl());
	}
}
