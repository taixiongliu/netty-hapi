package test;

import com.github.taixiongliu.hapi.HapiHttpContextFactory;
import com.github.taixiongliu.hapi.autowired.AutowiredHandler;

/** 
 * @author taixiong.liu
 */
public class Test {
	public static void main(String[] args) {
		//version request:http://127.0.0.1:8100/v1/index.html  or   http://127.0.0.1:8100/v2/index.html
		HapiHttpContextFactory.getInstance().buildVersion("v1").buildVersion("v2").buildAutowired(new AutowiredHandler() {
			
			@Override
			public Object onAutowired(Class<?> clazz, String value) {
				// TODO Auto-generated method stub
				//spring autowired
				//ClassPathXmlApplicationContext.getBean(clazz);
				System.out.println(clazz.toString());
				return new String("nice");
			}
		}).createContext("hapi-context.xml", "test.route");
	}
}
