package com.github.taixiongliu.hapi.autowired;

import java.lang.reflect.Field;

/** 
 * @author taixiong.liu
 */
public class AutowiredField{
	private Field field;
	private String value;
	public AutowiredField() {
		// TODO Auto-generated constructor stub
	}
	public AutowiredField(Field field, String value) {
		// TODO Auto-generated constructor stub
		this.field = field;
		this.value = value;
	}
	
	public Field getField() {
		return field;
	}
	public void setField(Field field) {
		this.field = field;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
