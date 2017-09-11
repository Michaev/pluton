package REST;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import Config.Configuration;
import Data.Funds;
import Data.TickSize;

public class Rest_CEX extends REST {
	
	private boolean verbose;
	private Authentication auth;
	
	public Rest_CEX(boolean verbose) {
		this.verbose = verbose;
		this.auth = new Authentication(verbose);
	}
	
	public String getData(String url) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		try {
			jsonResponse = Unirest.post(url)
					  .header("accept", "application/json")
					  .asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		jsonResponse.getBody();
		
		return "";
	}
	
	public double getOrderRemaning(long orderId) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://cex.io/api/get_order/";
		
		try {
			auth.upNonce();
			jsonResponse = Unirest.post(url)
					  .header("accept", "application/json")
					  .field("key", auth.getApi())
					  .field("signature", auth.getSignature())
					  .field("nonce", auth.getNonce())
					  .field("id", orderId)
					  .asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		System.out.println("getOrder: " + jsonResponse.getBody().getObject());
		
		double remaining = -1;
		try {
			remaining = jsonResponse.getBody().getObject().getDouble("remains");
		} catch (JSONException e) {
		}
		
		return remaining;
	}
	
	public void getOpenOrders(long orderId) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://cex.io/api/open_orders/";
		
		try {
			auth.upNonce();
			jsonResponse = Unirest.post(url)
					  .header("accept", "application/json")
					  .field("key", auth.getApi())
					  .field("signature", auth.getSignature())
					  .field("nonce", auth.getNonce())
					  .asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		System.out.println(orderId);
		
		System.out.println("getOpenOrders: " + jsonResponse.getBody().getObject());
		
	}
	
	public List<Funds> getFunds(String[] currencies) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://cex.io/api/balance/";
		List<Funds> funds = new ArrayList<Funds>();
		
		try {
			auth.upNonce();
			jsonResponse = Unirest.post(url)
					  .header("accept", "application/json")
					  .field("key", auth.getApi())
					  .field("signature", auth.getSignature())
					  .field("nonce", auth.getNonce())
					  .asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		System.out.println("getFunds: " + jsonResponse.getBody().getArray());
	
		for(int i = 0; i < jsonResponse.getBody().getArray().length(); i++) {
			JSONObject jsonObj = jsonResponse.getBody().getArray().getJSONObject(i);

			for(String currency: currencies) {

				double amountAvailable = jsonObj.getJSONObject(currency).getDouble("available");
				double amountOrder = jsonObj.getJSONObject(currency).getDouble("orders");
				funds.add(new Funds(currency, amountAvailable, amountOrder));
			}
		}

		return funds;
		
	}
	
	public long replaceOrder(String cur1, String cur2, String type, double amount, double price, long orderId) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://cex.io/api/cancel_replace_order/" + cur1 + "/" + cur2 +"/";
		
		try {
			auth.upNonce();
			jsonResponse = Unirest.post(url)
					  .header("accept", "application/json")
					  .field("key", auth.getApi())
					  .field("signature", auth.getSignature())
					  .field("nonce", auth.getNonce())
					  .field("type", type.toLowerCase())
					  .field("amount", amount)
					  .field("price", price)
					  .field("order_id", orderId)
					  .asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		System.out.println("replaceOrder: " + jsonResponse.getBody().getObject());
		long id = -1;
		
		try {
			id = jsonResponse.getBody().getObject().getLong("id");
		} catch (JSONException e) {
			System.out.println(jsonResponse);
		}
		
		return id;
	}
	
	public boolean cancelOrder(long orderId) {
		HttpResponse<String> jsonResponse = null;
		
		String url = "https://cex.io/api/cancel_order/";
		
		try {
			auth.upNonce();
			jsonResponse = Unirest.post(url)
					  .header("accept", "application/json")
					  .field("key", auth.getApi())
					  .field("signature", auth.getSignature())
					  .field("nonce", auth.getNonce())
					  .field("id", orderId)
					  .asString();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		System.out.println(jsonResponse.toString());
		
		return jsonResponse.toString().equals("true");
	}
	
	public long placeOrder(String cur1, String cur2, String type, double amount, double price) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://cex.io/api/place_order/" + cur1 + "/" + cur2 +"/";
		
		price = (double) Math.round(price * 10000d) / 10000d;
		
		try {
			auth.upNonce();
			jsonResponse = Unirest.post(url)
					  .header("accept", "application/json")
					  .field("key", auth.getApi())
					  .field("signature", auth.getSignature())
					  .field("nonce", auth.getNonce())
					  .field("type", type.toLowerCase())
					  .field("amount", amount)
					  .field("price", price)
					  .asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		System.out.println("placeOrder: " + jsonResponse.getBody().getObject());
		
		long id = -1;
		try {
			id = jsonResponse.getBody().getObject().getLong("id");
		} catch (JSONException e) {
			System.out.println(jsonResponse);
		}
		return id;
	}
	
	public String getPublicOrders(String cur1, String cur2) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://cex.io/api/order_book/" + cur1 + "/" + cur2 +"/";
		
		try {
			auth.upNonce();
			jsonResponse = Unirest.post(url)
					  .header("accept", "application/json")
					  .field("key", auth.getApi())
					  .field("signature", auth.getSignature())
					  .field("nonce", auth.getNonce())
					  .asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		if(verbose)	System.out.println("getPublicOrders: " + jsonResponse.getBody().getObject());
		
		return jsonResponse.getBody().getObject().toString();
	}
	
	public String getOwnTradeHistory(String cur1, String cur2) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://cex.io/api/archived_orders/" + cur1 + "/" + cur2 +"/";
		
		try {
			auth.upNonce();
			jsonResponse = Unirest.post(url)
					  .header("accept", "application/json")
					  .field("key", auth.getApi())
					  .field("signature", auth.getSignature())
					  .field("nonce", auth.getNonce())
					  .asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		if(verbose)	System.out.println("getOwnTradeHist: " + jsonResponse.getBody().getArray());
		
		return jsonResponse.getBody().getArray().toString();
		
	}
	
	public List<TickSize> getTickSizes() {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://cex.io/api/currency_limits";
		
		try {
			auth.upNonce();
			jsonResponse = Unirest.post(url)
					  .header("accept", "application/json")
					  .asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		JSONObject tickSizes = jsonResponse.getBody().getObject();
		tickSizes = tickSizes.getJSONObject("data");
		
		String value = tickSizes.get("pairs").toString();
		
		String[] ts = value.split("\\},\\{");
		
		List<TickSize> tickSizeList = new ArrayList<TickSize>();
		
		for(String tick: ts) {
			String symbol1 = tick.split("symbol1\":\"")[1];
			symbol1 = symbol1.split("\"")[0];
			
			String symbol2 = tick.split("symbol2\":\"")[1];
			symbol2 = symbol2.split("\"")[0];
			
			String tickNumberTemp1 = tick.split("minLotSize\":")[1];
			double tickNumber1 = Double.parseDouble(tickNumberTemp1.split(",")[0]);
			
			String tickNumberTemp2 = tick.split("minLotSizeS2\":")[1];
			double tickNumber2 = Double.parseDouble(tickNumberTemp2.split(",")[0]);
			
			double tickNumber3 = 0.01;
			
			if(Configuration.EXCHANGE.equals("cex") && symbol1.equals("BTC") && symbol2.equals("USD")) tickNumber3 = 0.0001;
			if(Configuration.EXCHANGE.equals("cex") && symbol1.equals("BCH") && symbol2.equals("USD")) tickNumber3 = 0.0001;
			if(Configuration.EXCHANGE.equals("cex") && symbol1.equals("ETH") && symbol2.equals("USD")) tickNumber3 = 0.00000001;
			
			tickSizeList.add(new TickSize(symbol1, symbol2, tickNumber1, tickNumber2, tickNumber3));
		}
		
		return tickSizeList;
		
	}
	
	public String getOpenOrders(String cur1, String cur2) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://cex.io/api/open_orders/" + cur1 + "/" + cur2 +"/";
		
		try {
			auth.upNonce();
			jsonResponse = Unirest.post(url)
					  .header("accept", "application/json")
					  .field("key", auth.getApi())
					  .field("signature", auth.getSignature())
					  .field("nonce", auth.getNonce())
					  .asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		if(verbose)	System.out.println("getOpenOrders: " + jsonResponse.getBody().getArray());
		
		return jsonResponse.getBody().getArray().toString();
		
	}
}
