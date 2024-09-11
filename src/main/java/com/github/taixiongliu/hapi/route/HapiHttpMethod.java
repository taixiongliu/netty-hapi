package com.github.taixiongliu.hapi.route;

/**
 * <b>enum HapiHttpMethod</b>
 * <br><br>
 * BOTH=1, GET=2, POST=3
 * @author taixiong.liu
 *
 */
public enum HapiHttpMethod {
	OPTIONS(1), GET(2), HEAD(3), POST(4), PUT(5), PATCH(6), DELETE(7), TRACE(8), CONNECT(9), ALL(10);
	private int value;
	private String name;
	private HapiHttpMethod(int value) {
		// TODO Auto-generated constructor stub
		this.value = value;
		switch (value) {
		case 1:
			name = "OPTIONS";
			break;
		case 2:
			name = "GET";
			break;
		case 3:
			name = "HEAD";
			break;
		case 4:
			name = "POST";
			break;
		case 5:
			name = "PUT";
			break;
		case 6:
			name = "PATCH";
			break;
		case 7:
			name = "DELETE";
			break;
		case 8:
			name = "TRACE";
			break;
		case 9:
			name = "CONNECT";
			break;
		case 10:
			name = "ALL";
			break;

		default:
			break;
		}
	}
	
	public boolean equals(HapiHttpMethod method){
		if(method.value == this.value){
			return true;
		}
		return false;
	}
	
	public String getName() {
		return name;
	}
}
