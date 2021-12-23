package test;

import com.github.taixiongliu.hapi.HapiHttpContextFactory;

/** 
 * @author taixiong.liu
 */
public class Test {
	public static void main(String[] args) {
		HapiHttpContextFactory.getInstance().createContext("hapi-context.xml", "test.route");
	}
}
