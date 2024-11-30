package com.github.taixiongliu.hapi.tc;

import io.netty.handler.codec.http.HttpResponseStatus;

public class ThreadController implements Runnable{
	private static ThreadController controller = null;
	private static Object look = new Object();
	public static ThreadController getInstance(){
		if(controller != null){
			return controller;
		}
		synchronized (look) {
			if(controller == null){
				controller = new ThreadController();
			}
		}
		return controller;
	}
	
	private ThreadContainer container;
	private ThreadError busyError;
	
	private int poolCount;
	
	private ThreadController() {
		// TODO Auto-generated constructor stub
	}
	// only execute one times.
	public boolean initController(ThreadContainer container) {
		return this.initController(container, null);
	}
	public boolean initController(ThreadContainer container, ThreadError busyError) {
		if(container == null || this.container != null) {
			return false;
		}
		if(container.getInitCount() < 1) {
			return false;
		}
		if(container.getInterval() < 1) {
			return false;
		}
		if(container.getIntervalCount() < 1) {
			return false;
		}
		this.container = container;
		if(busyError == null) {
			busyError = new ThreadError(HttpResponseStatus.BAD_GATEWAY, null, 555, null, "server busy now");
		}
		this.busyError = busyError;
		this.poolCount = container.getInitCount();
		//start Thread
		new Thread(this).start();
		return true;
	}
	public synchronized boolean getPointer() {
		if(this.poolCount < 1) {
			return false;
		}
		poolCount --;
		return true;
	}
	public ThreadError getThreadBusyError() {
		return busyError;
	}
	private synchronized void inject() {
		if(poolCount == container.getInitCount()) {
			return ;
		}
		poolCount += container.getIntervalCount();
		if(poolCount > container.getInitCount()) {
			poolCount = container.getInitCount();
		}
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true) {
			try {
				Thread.sleep(this.container.getInterval());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			inject();
		}
	}
}
