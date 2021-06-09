package com.github.taixiongliu.hapi.route;

public enum HapiRouteType {
	BODY(1), FILE(2), PATH(3), STREAM(4);
	private int value;
	private HapiRouteType(int value) {
		// TODO Auto-generated constructor stub
		this.value = value;
	}
	
	public boolean equals(HapiRouteType type){
		if(type.value == this.value){
			return true;
		}
		return false;
	}
}
