package test;

import com.github.taixiongliu.hapi.HapiHttpContextFactory;
import com.github.taixiongliu.hapi.autowired.AutowiredHandler;

/** 
 * @author taixiong.liu
 */
public class Test {
	public static void main(String[] args) {
		HapiHttpContextFactory.getInstance().buildAutowired(new AutowiredHandler() {
			
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
