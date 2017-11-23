package cn.liutaixiong.hapi.route;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>Annotation RequestMapping</b>
 * <br/><br/>
 * value=[route], e.g: http://www.xxx.com/a/b, value=b
 * <br/>
 * method=[HTTP method], default BOTH that can be request with HTTP method GET and POST
 * @author taixiong.liu
 *
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
	String value() default "";
	HapiHttpMethod method() default HapiHttpMethod.BOTH; 
}
