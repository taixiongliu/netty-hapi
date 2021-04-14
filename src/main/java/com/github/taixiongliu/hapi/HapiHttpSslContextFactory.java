package com.github.taixiongliu.hapi;

import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import com.github.taixiongliu.hapi.ssl.HttpsKeyStore;
import com.github.taixiongliu.hapi.ssl.KeystoreEntity;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * <b>SSL Factory</b>
 * 
 * Build SSL engine or handler.
 * @author taixiong.liu
 */
public class HapiHttpSslContextFactory {
	private static HapiHttpSslContextFactory factory = null;
	private static Object look = new Object();
	public static HapiHttpSslContextFactory getInstance(){
		if(factory != null){
			return factory;
		}
		synchronized (look) {
			if(factory == null){
				factory = new HapiHttpSslContextFactory();
			}
		}
		return factory;
	}
	
	private final String PROTOCOL = "SSLv3";//SSL protocol
	private KeyManagerFactory kmf;
	private boolean isInit;
	private HapiHttpSslContextFactory() {
		// TODO Auto-generated constructor stub
		isInit = false;
	}
	
	/**
	 * Initialization configuration must be done.
	 * @param entity key store info
	 */
	public synchronized void init(KeystoreEntity entity){
		if(isInit){
			return ;
		}
		String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
		HttpsKeyStore httpsKeyStore = new HttpsKeyStore(entity);
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(httpsKeyStore.getKeyStoreStream(), httpsKeyStore.getKeyStorePassword());
	        kmf = KeyManagerFactory.getInstance(algorithm);
	        kmf.init(ks, httpsKeyStore.getCertificatePassword());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			kmf = null;
		}
		isInit = true;
	}
	
	/**
	 * create normal SSL engine.
	 * @return SSLEngine
	 */
	public SSLEngine createSSLEngine() {
		if(kmf == null){
			return null;
		}
		SSLContext serverContext;
		try {
			serverContext = SSLContext.getInstance(PROTOCOL);
			serverContext.init(kmf.getKeyManagers(), null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			serverContext = null;
		}
		if(serverContext == null){
			return null;
		}
        SSLEngine sslEngine = serverContext.createSSLEngine();
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(false);
        return sslEngine ;
    }
	
	/**
	 * create object SslContext.
	 * @return SslContext
	 */
	public SslContext createSslContext(){
		if(kmf == null){
			return null;
		}
		SslContext sslContext = null;
		try {
			sslContext = SslContextBuilder.forServer(kmf).build();
		} catch (SSLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sslContext = null;
		}
		return sslContext;
	}
}
