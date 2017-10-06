package News;

import org.json.JSONArray;
import org.json.JSONObject;

import Config.Configuration;
import Engine.Pluton;
import REST.Rest_News;

public class NewsAgent
{
	Pluton parent;
	Rest_News rest_news = new Rest_News(true);
	
	public NewsAgent(Pluton parent) {
		this.parent = parent;
	}
	
	public void start() {
		
		String keyword = "btc";
		
		for(String source: Configuration.NEWS_SOURCES) {
			JSONArray news = rest_news.getNews(source.split("\\|")[0], source.split("\\|")[1]);
			
			for(Object n: news) {
				JSONObject jObj = (JSONObject) n;
				
				String desc = "";
				String title = "";
				
				if(!jObj.isNull("description"))
					desc = jObj.getString("description");
				
				if(!jObj.isNull("title"))
					title = jObj.getString("title");
				
				if(desc.toUpperCase().contains(keyword.toUpperCase()))
					System.out.println("word found in description, " + desc + ", source " + source);
				
				if(title.toUpperCase().contains(keyword.toUpperCase()))
					System.out.println("word found in title, " + title + ", source " + source);
			}
			
			// System.out.println("Searched source " + source);
		}
	}
}
