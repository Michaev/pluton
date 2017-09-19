package REST;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import Config.Configuration;

public class Rest_BTF extends REST {
	
	private boolean verbose;
	private Auth_BTF auth;
	
	public Rest_BTF(boolean verbose) {
		this.verbose = verbose;
		this.auth = new Auth_BTF(verbose);
	}
	
	public long placeOrder(String cur1, String cur2, String type, double amount, double price) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://api.bitfinex.com/v1/order/new";
		
		price = (double) Math.round(price * 10000d) / 10000d;
		
		auth.upNonce();
		JSONObject jo = new JSONObject();
		jo.put("request", "/v1/order/new");
		jo.put("symbol", cur1.toUpperCase() + cur2.toUpperCase());
		jo.put("signature", auth.getSignature());
		jo.put("nonce", Long.toString(auth.getNonce()));
		jo.put("side", type.toLowerCase());
		jo.put("type", "exchange limit");
		jo.put("amount", Double.toString(amount));
		jo.put("price", Double.toString(price));
		
		String payload = jo.toString();
		
		String payload_base64 = Base64.getEncoder().encodeToString(payload.getBytes());
		
		String payload_sha384hmac = hmacDigest(payload_base64, auth.getSecret(), "HmacSHA384");

		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("x-bfx-payload", payload_base64);
		headers.put("x-bfx-apikey", auth.getApi());
		headers.put("x-bfx-signature", payload_sha384hmac);
		
		do {
			try {
				jsonResponse = Unirest.post(url)
						  .headers(headers)
						  .asJson();
				
				if(jsonResponse == null) {
					Thread.sleep(Configuration.API_TIMEOUT_RETRY);
				}
			} catch (UnirestException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while(jsonResponse == null);
		
		System.out.println("placeOrder: " + jsonResponse.getBody().getObject());
		
		long id = -1;
		try {
			id = jsonResponse.getBody().getObject().getLong("id");
		} catch (JSONException e) {
			System.out.println(jsonResponse);
		}
		return id;
	}
	
	public long cancelOrder(long orderId) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://api.bitfinex.com/v1/order/cancel";
		
		auth.upNonce();
		JSONObject jo = new JSONObject();
		jo.put("request", "/v1/order/cancel");
		jo.put("order_id", orderId);
		jo.put("signature", auth.getSignature());
		jo.put("nonce", Long.toString(auth.getNonce()));
		
		String payload = jo.toString();
		
		String payload_base64 = Base64.getEncoder().encodeToString(payload.getBytes());
		
		String payload_sha384hmac = hmacDigest(payload_base64, auth.getSecret(), "HmacSHA384");
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("x-bfx-payload", payload_base64);
		headers.put("x-bfx-apikey", auth.getApi());
		headers.put("x-bfx-signature", payload_sha384hmac);
		
		do {
			try {
				jsonResponse = Unirest.post(url)
						  .headers(headers)
						  .asJson();
				
				if(jsonResponse == null) {
					Thread.sleep(Configuration.API_TIMEOUT_RETRY);
				}
			} catch (UnirestException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while(jsonResponse == null);
		
		System.out.println("placeOrder: " + jsonResponse.getBody().getObject());
		
		long id = -1;
		try {
			id = jsonResponse.getBody().getObject().getLong("id");
		} catch (JSONException e) {
			System.out.println(jsonResponse);
		}
		return id;
	}
	
	public long replaceOrder(long orderId, String cur1, String cur2, String type, double amount, double price) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://api.bitfinex.com/v1/order/cancel/replace";
		
		price = (double) Math.round(price * 10000d) / 10000d;
		
		auth.upNonce();
		JSONObject jo = new JSONObject();
		jo.put("request", "/v1/order/cancel/replace");
		jo.put("order_id", orderId);
		jo.put("symbol", cur1.toUpperCase() + cur2.toUpperCase());
		jo.put("signature", auth.getSignature());
		jo.put("nonce", Long.toString(auth.getNonce()));
		jo.put("side", type.toLowerCase());
		jo.put("type", "exchange limit");
		jo.put("amount", Double.toString(amount));
		jo.put("price", Double.toString(price));
		
		String payload = jo.toString();
		
		String payload_base64 = Base64.getEncoder().encodeToString(payload.getBytes());
		
		String payload_sha384hmac = hmacDigest(payload_base64, auth.getSecret(), "HmacSHA384");

		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("x-bfx-payload", payload_base64);
		headers.put("x-bfx-apikey", auth.getApi());
		headers.put("x-bfx-signature", payload_sha384hmac);
		
		do {
			try {
				jsonResponse = Unirest.post(url)
						  .headers(headers)
						  .asJson();
				
				if(jsonResponse == null) {
					Thread.sleep(Configuration.API_TIMEOUT_RETRY);
				}
			} catch (UnirestException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while(jsonResponse == null);
		
		System.out.println("placeOrder: " + jsonResponse.getBody().getObject());
		
		long id = -1;
		try {
			id = jsonResponse.getBody().getObject().getLong("id");
		} catch (JSONException e) {
			System.out.println(jsonResponse);
		}
		return id;
	}
	
	public JSONArray getTrades(long timestamp, String cur1, String cur2) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		System.out.println("getTrades rest with timestamp " + timestamp);
		long timestampEnd = timestamp + (1000 * 3600 * 24);
		
		if(timestampEnd > new Date().getTime())
			timestampEnd = new Date().getTime();
		
		String url = "https://api.bitfinex.com/v2/trades/t" + cur1 + cur2 + "/hist?limit=" 
		+ Configuration.NUMBER_OF_TRADES_PER_BATCH + "&start=" + timestamp + "&end=" + timestampEnd + "&sort=1";
		
		System.out.println("URL: " + url);
		
		do {
			try {
				jsonResponse = Unirest.get(url)
						  .header("accept", "application/json")
						  .asJson();
				
				if(jsonResponse == null) {
					Thread.sleep(Configuration.API_TIMEOUT_RETRY);
				}
			} catch (UnirestException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		} while(jsonResponse == null);
		
		jsonResponse.getBody().getArray();
		
		return jsonResponse.getBody().getArray();
	}
	
	public JSONArray getRecentTrades(long start, long stop, String cur1, String cur2) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://api.bitfinex.com/v2/trades/t" + cur1 + cur2 + "/hist?start=" + start + "&end=" + stop;
		
		System.out.println("URL: " + url);
		
		do {
			try {
				jsonResponse = Unirest.get(url)
						  .header("accept", "application/json")
						  .asJson();
				
				if(jsonResponse == null) {
					Thread.sleep(Configuration.API_TIMEOUT_RETRY);
				}
			} catch (UnirestException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		} while(jsonResponse == null);
		
		// jsonResponse.getBody().getArray();
		
		return jsonResponse.getBody().getArray();
	}
	
	public String getPublicOrders(String cur1, String cur2) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://api.bitfinex.com/v2/book/t" + cur1 + cur2 +"/P0";
		System.out.println(url);
		
		do {
			try {
				jsonResponse = Unirest.get(url)
						  .header("accept", "application/json")
						  .asJson();
				if(jsonResponse == null) {
					Thread.sleep(Configuration.API_TIMEOUT_RETRY);
				}
			} catch (UnirestException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while(jsonResponse == null);
		
		if(verbose)	System.out.println("getPublicOrders: " + jsonResponse.getBody().getArray());
		
		return jsonResponse.getBody().getArray().toString();
	}
	
	public JSONArray getPrivateOrders() {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://api.bitfinex.com/v1/orders";
		System.out.println(url);

		auth.upNonce();
		JSONObject jo = new JSONObject();
		jo.put("request", "/v1/orders");
		jo.put("nonce", Long.toString(auth.getNonce()));
		
		String payload = jo.toString();
		
		String payload_base64 = Base64.getEncoder().encodeToString(payload.getBytes());
		
		String payload_sha384hmac = hmacDigest(payload_base64, auth.getSecret(), "HmacSHA384");

		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("x-bfx-payload", payload_base64);
		headers.put("x-bfx-apikey", auth.getApi());
		headers.put("x-bfx-signature", payload_sha384hmac);
		
		do {
			try {
				jsonResponse = Unirest.get(url)
						  .headers(headers)
						  .asJson();
			
				if(jsonResponse == null) {
					Thread.sleep(Configuration.API_TIMEOUT_RETRY);
				}
				
			} catch (UnirestException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (jsonResponse == null);
		
		if(verbose)	System.out.println("getPrivateOrders: " + jsonResponse.getBody().getArray());
		
		return jsonResponse.getBody().getArray();
	}
	
	public JSONArray getFunds() {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://api.bitfinex.com/v1/balances";
		System.out.println(url);

		auth.upNonce();
		JSONObject jo = new JSONObject();
		jo.put("request", "/v1/balances");
		jo.put("nonce", Long.toString(auth.getNonce()));
		
		String payload = jo.toString();
		
		String payload_base64 = Base64.getEncoder().encodeToString(payload.getBytes());
		
		String payload_sha384hmac = hmacDigest(payload_base64, auth.getSecret(), "HmacSHA384");

		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("x-bfx-payload", payload_base64);
		headers.put("x-bfx-apikey", auth.getApi());
		headers.put("x-bfx-signature", payload_sha384hmac);
		
		do {
			try {
				jsonResponse = Unirest.get(url)
						  .headers(headers)
						  .asJson();
				
				if(jsonResponse == null) {
					Thread.sleep(Configuration.API_TIMEOUT_RETRY);
				}
			} catch (UnirestException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while(jsonResponse == null);
		
		//if(verbose)	System.out.println("getFunds: " + jsonResponse.getBody().getArray());
		
		return jsonResponse.getBody().getArray();
	}
	
	public JSONArray get24HVolume(String cur1, String cur2) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://api.bitfinex.com/v2/ticker/t" + cur1 + cur2;
		
		System.out.println("Get 24H volume URL: " + url);
		
		do {
			try {
				jsonResponse = Unirest.get(url)
						  .header("accept", "application/json")
						  .asJson();
				
				if(jsonResponse == null) {
					Thread.sleep(Configuration.API_TIMEOUT_RETRY);
				}
			} catch (UnirestException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		} while(jsonResponse == null);
		
		jsonResponse.getBody().getArray();
		
		return jsonResponse.getBody().getArray();
	}
	
	 public static String hmacDigest(String msg, String keyString, String algo) {
	        String digest = null;
	        try {
	            SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), algo);
	            Mac mac = Mac.getInstance(algo);
	            mac.init(key);

	            byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));

	            StringBuffer hash = new StringBuffer();
	            for (int i = 0; i < bytes.length; i++) {
	                String hex = Integer.toHexString(0xFF & bytes[i]);
	                if (hex.length() == 1) {
	                    hash.append('0');
	                }
	                hash.append(hex);
	            }
	            digest = hash.toString();
	        } catch (UnsupportedEncodingException e) {
	            System.out.println("Exception: " + e.getMessage());
	        } catch (InvalidKeyException e) {
	            System.out.println("Exception: " + e.getMessage());
	        } catch (NoSuchAlgorithmException e) {
	        	System.out.println("Exception: " + e.getMessage());
	        }
	        return digest;
	    }
}
