package com.github.taixiongliu.hapi.route;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>Annotation ProxyMapping</b>
 * <br><br>
 * value=[route], e.g: http://www.xxx.com/a/*
 * <br><br>
 * method=[HTTP method], default BOTH that can be request with HTTP method GET and POST
 * @author taixiong.liu
 *
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProxyMapping {
	String value() default "";
	HapiHttpMethod method() default HapiHttpMethod.ALL;
	HapiRouteType type() default HapiRouteType.BODY;
}
