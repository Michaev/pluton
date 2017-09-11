package REST;

import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

import org.apache.commons.codec.binary.Base64;

public class Authentication {

	private long nonce;
	private boolean verbose;
	
	public Authentication(boolean verbose) {
		this.nonce = new Date().getTime()/1000;
		this.verbose = verbose;
	}
	
	public void upNonce() {
		nonce = new Date().getTime();
	}
	
	public String getSignature() {
		
		String signature = "";
		String message = getNonce() + getUserID() + getApi();
		
		try {
			String secret = "DZyUJ6dImVqP5IIkIjlD0quUA";

			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			
			byte[] bytes = sha256_HMAC.doFinal(message.getBytes());
			signature = Hex.encodeHexString(bytes);
		}
		catch (Exception e){
			System.out.println("Error");
		}
		
		if(verbose) System.out.println("Returning sig: " + signature.toUpperCase());
		return signature.toUpperCase();
	}
	
	public String getUserID() {
		return "up101109238";
	}
	
	public String getApi() {
		return "3uiHzbJcQwS6O2VWwgEcar6FR4";
	}
	
	public long getNonce() {
		if(verbose) System.out.println("Returning nonce: " + this.nonce);
		return this.nonce;
	}
}
