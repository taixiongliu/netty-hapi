package com.github.taixiongliu.hapi.ssl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


/**
 * <b>Https key store</b>
 * <br/><br/>
 * Load the file generated with the JDK 'keytool' command, suffix 'keystore'.
 * @author taixiong.liu
 */
public class HttpsKeyStore {
	private KeystoreEntity entity;
	public HttpsKeyStore(KeystoreEntity entity) {
		// TODO Auto-generated constructor stub
		this.entity = entity;
	}
	
	/**
	 * read keystore file.
	 * @return File input stream.
	 */
	public InputStream getKeyStoreStream() {
	    InputStream inStream = null;
	    try {
	        inStream = new FileInputStream(entity.getKeystorePath());
	    } catch (FileNotFoundException e) {
	        System.out.println("Failed to read keystore file.");
	    }
	    return inStream;
    }
	
	/**
	 * Get char array of certificate password.
	 * @return char array
	 */
	public char[] getCertificatePassword() {
        return entity.getCertificatePassword().toCharArray();
    }
	
	/**
	 * Get char array of keystore password.
	 * @return char array
	 */
	public char[] getKeyStorePassword() {
        return entity.getKeystorePassword().toCharArray();
    }
}
