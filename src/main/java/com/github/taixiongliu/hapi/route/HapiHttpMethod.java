package com.github.taixiongliu.hapi.route;

/**
 * <b>enum HapiHttpMethod</b>
 * <br><br>
 * BOTH=1, GET=2, POST=3
 * @author taixiong.liu
 *
 */
public enum HapiHttpMethod {
	BOTH(1), GET(2), POST(3);
	private int value;
	private HapiHttpMethod(int value) {
		// TODO Auto-generated constructor stub
		this.value = value;
	}
	
	public boolean equals(HapiHttpMethod method){
		if(method.value == this.value){
			return true;
		}
		return false;
	}
}
