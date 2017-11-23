package cn.liutaixiong.hapi.route;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>Annotation Route</b>
 * <br/><br/>
 * value=[route], e.g: http://www.xxx.com/a/b, value=a
 * @author taixiong.liu
 *
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
	String value() default "";
}
