package REST;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

public class Rest_News extends REST {

	boolean verbose;
	String apiKey = "a11876f8c39344cf9f8d1963c4f77f55";
	
	public Rest_News(boolean verbose) {
		this.verbose = verbose;
	}
	
	public JSONArray getNews(String source, String sort) {
		HttpResponse<JsonNode> jsonResponse = null;
		
		String url = "https://newsapi.org/v1/articles?source=" + source + "&sortBy=" + sort + "&apiKey=" + apiKey;
		
		// System.out.println("URL: " + url);
		
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
		
//		if(source.equals("techcrunch"))
//			System.out.println("found");
		
		JSONArray news = jsonResponse.getBody().getObject().getJSONArray("articles");
		
		return news;
	}

	
}
	
