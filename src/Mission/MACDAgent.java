package Mission;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import Config.Configuration;
import Engine.Pluton;
import jdk.nashorn.internal.runtime.regexp.joni.Config;

public class MACDAgent {
	
	Pluton parent;
	long startTime;
	boolean keepRunning;
	Date d;
	
	public MACDAgent(Pluton parent) {
		this.parent = parent;
		this.keepRunning = true;
		d = new Date();
	}
	
	public void start() {
		
		// Initialize
		for(String currency: Configuration.CURRENCIES) {
			String cur1 = currency.split("/")[1];
			String cur2 = currency.split("/")[2];
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MILLISECOND, - Configuration.MACD_TIME_PERIOD * Configuration.MACD_EMA_2);
			parent.dataHandler.historyMACD_prices.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.historyMACD_EMA1.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.historyMACD_EMA2.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.historyMACD_macd.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.historyMACD_signal.put(cur1 + cur2, new ArrayList<Double>());
			
			// Get enough data for the MACD, according to the MACD strategy
			for(int i = 0; i < Configuration.MACD_EMA_2; i++) {
				startTime = cal.getTimeInMillis();
				
				double close = parent.restHandler_btf.getLastPrice(cur1, cur2, startTime);
				parent.dataHandler.historyMACD_prices.get(cur1 + cur2).add(close);
				
				cal.add(Calendar.MILLISECOND, Configuration.MACD_TIME_PERIOD);
				
				
				try {
					Thread.sleep((60000 / Configuration.NUMBER_OF_API_CALLS_MINUTE));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			calculateEMAs(cur1, cur2);
			
			System.out.println(cur1 + "/" + cur2 + " list initiated as: " + parent.dataHandler.historyMACD_prices.get(cur1 + cur2));
		}
		
		while(keepRunning) {
			d = new Date();
			
			for(String currency: Configuration.CURRENCIES) {
				String cur1 = currency.split("/")[1];
				String cur2 = currency.split("/")[2];
				
				long timestamp = d.getTime();
				double price = parent.restHandler_btf.getLastPrice(cur1, cur2, timestamp);
				
				parent.dataHandler.historyMACD_prices.get(cur1 + cur2).add(price);
				parent.dataHandler.historyMACD_prices.get(cur1 + cur2).remove(0);
				
				calculateEMAs(cur1, cur2);
				
				
			}
			
			
			long sleepTime = new Date().getTime() - d.getTime() - (60000 / Configuration.NUMBER_OF_API_CALLS_MINUTE);
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void calculateEMAs(String cur1, String cur2) {
		
		List<Double> prices = parent.dataHandler.historyMACD_prices.get(cur1 + cur2);
		List<Double> EMA1 = parent.dataHandler.historyMACD_EMA1.get(cur1 + cur2);
		List<Double> EMA2 = parent.dataHandler.historyMACD_EMA2.get(cur1 + cur2);
		List<Double> MACD = parent.dataHandler.historyMACD_macd.get(cur1 + cur2);
		List<Double> signal = parent.dataHandler.historyMACD_signal.get(cur1 + cur2);

		double ema1Initial = 0;
		int count = 0;
		for(double p: prices) {
			ema1Initial += p;
			count++;
			
			if(Configuration.MACD_EMA_1 > count)
				break;
		}
		ema1Initial /= Configuration.MACD_EMA_1;
		EMA1.add(ema1Initial);
		
		System.out.println(ema1Initial);
		
		for(int i = 1; i < Configuration.MACD_EMA_1; i++) {
			double multiplier = (2 / (Configuration.MACD_EMA_1 + 1));
			double price = prices.get(prices.size() - Configuration.MACD_EMA_1);
			double prevEma = EMA1.get(i-1);
			
			double currentEma = (price * multiplier) + (prevEma * (1 - multiplier));
			EMA1.add(currentEma);
		}
		
		for(int i = 1; i < Configuration.MACD_EMA_2; i++) {
			double multiplier = (2 / (Configuration.MACD_EMA_2 + 1));
			double price = prices.get(prices.size() - Configuration.MACD_EMA_2);
			double prevEma = EMA2.get(i-1);
			
			double currentEma = (price * multiplier) + (prevEma * (1 - multiplier));
			EMA2.add(currentEma);
		}
	}
}
