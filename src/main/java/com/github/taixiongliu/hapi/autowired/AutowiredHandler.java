package com.github.taixiongliu.hapi.autowired;
/** 
 * @author taixiong.liu
 */
public interface AutowiredHandler {
	public Object onAutowired(Class<?> clazz, String value);
}
