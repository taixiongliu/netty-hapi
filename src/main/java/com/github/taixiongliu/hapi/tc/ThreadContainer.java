package com.github.taixiongliu.hapi.tc;

public class ThreadContainer {
	//pool initial count
	private int initCount;
	//interval of put to pool 
	private int interval;
	//count of put to pool per 'interval'
	private int intervalCount;
	public ThreadContainer() {
		// TODO Auto-generated constructor stub
	}
	public ThreadContainer(int initCount, int interval, int intervalCount) {
		// TODO Auto-generated constructor stub
		this.initCount = initCount;
		this.interval = interval;
		this.intervalCount = intervalCount;
	}
	public int getInitCount() {
		return initCount;
	}
	public void setInitCount(int initCount) {
		this.initCount = initCount;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public int getIntervalCount() {
		return intervalCount;
	}
	public void setIntervalCount(int intervalCount) {
		this.intervalCount = intervalCount;
	}
}
