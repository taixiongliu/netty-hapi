package test.route;

import com.github.taixiongliu.hapi.http.HapiHttpRequest;
import com.github.taixiongliu.hapi.http.HapiHttpResponse;
import com.github.taixiongliu.hapi.route.DeleteRequestMapping;
import com.github.taixiongliu.hapi.route.GetRequestMapping;
import com.github.taixiongliu.hapi.route.HapiHttpMethod;
import com.github.taixiongliu.hapi.route.PostRequestMapping;
import com.github.taixiongliu.hapi.route.PutRequestMapping;
import com.github.taixiongliu.hapi.route.RequestMapping;
import com.github.taixiongliu.hapi.route.Route;

/** 
 * @author taixiong.liu
 */
@Route("rest")
public class RestFul {
	
	//method GET link like http://localhost/rest/entity/0/10
	@GetRequestMapping("entity/{startRow}/{pageSize}")
	public void get(HapiHttpRequest request, HapiHttpResponse response){
		//get dynamic parameter startRow in path route.
		String startRow = request.getPathParameter("startRow");
		//get dynamic parameter pageSize in path route.
		String pageSize = request.getPathParameter("pageSize");
		
		response.setContent("get entity where startRow="+startRow+",pageSize="+pageSize);
	}
	//method POST link like http://localhost/entity
	//post entity property=property
	@PostRequestMapping("entity")
	public void add(HapiHttpRequest request, HapiHttpResponse response){
		String property = request.getParameter("entity property");
		response.setContent("add:"+property);
	}
	//method PUT link like http://localhost/entity/1
	//post entity property=property
	@PutRequestMapping("entity/{id}")
	public void edit(HapiHttpRequest request, HapiHttpResponse response){
		String property = request.getParameter("entity property");
		String id =request.getPathParameter("id");
		response.setContent("set :"+property+" where id="+id);
	}
	//method DELETE link like http://localhost/entity/1
	@DeleteRequestMapping("entity/{id}")
	public void delete(HapiHttpRequest request, HapiHttpResponse response){
		String id =request.getPathParameter("id");
		response.setContent("delete entity where id="+id);
	}
	//other
	@RequestMapping(value = "entity", method = HapiHttpMethod.OPTIONS)
	public void other(HapiHttpRequest request, HapiHttpResponse response){
		response.setContent("other is OPTIONS:"+request.getMethod().name().equals(HapiHttpMethod.OPTIONS.getName()));
	}
	
}
