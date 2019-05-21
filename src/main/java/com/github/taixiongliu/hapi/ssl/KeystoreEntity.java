package com.github.taixiongliu.hapi.ssl;

/**
 * <b>Keystore entity</b>
 * <br/><br/>
 * Keystore configuration entity.
 * @author taixiong.liu
 * 
 */
public class KeystoreEntity {
	private String keystorePath;
	private String certificatePassword;
	private String keystorePassword;
	
	public KeystoreEntity(String keystorePath, String certificatePassword, String keystorePassword) {
		// TODO Auto-generated constructor stub
		this.keystorePath = keystorePath;
		this.certificatePassword = certificatePassword;
		this.keystorePassword = keystorePassword;
	}
	public KeystoreEntity() {
		// TODO Auto-generated constructor stub
	}
	
	public String getKeystorePath() {
		return keystorePath;
	}
	public void setKeystorePath(String keystorePath) {
		this.keystorePath = keystorePath;
	}
	public String getCertificatePassword() {
		return certificatePassword;
	}
	public void setCertificatePassword(String certificatePassword) {
		this.certificatePassword = certificatePassword;
	}
	public String getKeystorePassword() {
		return keystorePassword;
	}
	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}
}
