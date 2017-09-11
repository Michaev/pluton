package REST;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import Config.Configuration;

public class Rest_BTF extends REST {
	
	public JSONArray getTrades(long timestamp, String cur1, String cur2) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		System.out.println("getTrades rest with timestamp " + timestamp);
		long timestampEnd = timestamp + (1000 * 3600 * 24);
		
		if(timestampEnd > new Date().getTime())
			timestampEnd = new Date().getTime();
		
		String url = "https://api.bitfinex.com/v2/trades/t" + cur1 + cur2 + "/hist?limit=" 
		+ Configuration.NUMBER_OF_TRADES_PER_BATCH + "&start=" + timestamp + "&end=" + timestampEnd + "&sort=1";
		
		System.out.println("URL: " + url);
		
		try {
			jsonResponse = Unirest.get(url)
					  .header("accept", "application/json")
					  .asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		jsonResponse.getBody().getArray();
		
		return jsonResponse.getBody().getArray();
	}
	
	public JSONArray get24HVolume(String cur1, String cur2) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://api.bitfinex.com/v2/ticker/t" + cur1 + cur2;
		
		System.out.println("Get 24H volume URL: " + url);
		
		try {
			jsonResponse = Unirest.get(url)
					  .header("accept", "application/json")
					  .asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		jsonResponse.getBody().getArray();
		
		return jsonResponse.getBody().getArray();
	}
}
