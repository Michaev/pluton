package REST;

import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

import org.apache.commons.codec.binary.Base64;

public class Auth_BTF {

	private long nonce;
	private boolean verbose;
	private String secret;
	
	public Auth_BTF(boolean verbose) {
		this.nonce = new Date().getTime()/1000;
		this.verbose = verbose;
	}
	
	public void upNonce() {
		nonce = new Date().getTime();
	}
	
	public String getSignature() {
		
		String signature = "";
		String message = "";
		
		try {
			String secret = getSecret();

			Mac sha384_HMAC = Mac.getInstance("HmacSHA384");
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA384");
			sha384_HMAC.init(secret_key);
			
			byte[] bytes = sha384_HMAC.doFinal(message.getBytes());
			signature = Hex.encodeHexString(bytes);
		}
		catch (Exception e){
			System.out.println("Error");
		}
		
		if(verbose) System.out.println("Returning sig: " + signature);
		return signature;
	}
	
	public String getUserID() {
		return "promination";
	}
	
	public String getApi() {
		return "WuGpUbENSFxlpKxRi9zESRMcLmPvgdOWbvdlmhzwMEa";
	}
	
	public String getSecret() {
		return "c76iy4SmVhwvWaI0so0roDHAJdAeAUFjTqel7jLFMz6";
	}
	
	public long getNonce() {
		if(verbose) System.out.println("Returning nonce: " + this.nonce);
		return this.nonce;
	}
}
