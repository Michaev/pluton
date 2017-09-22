package Mission;

import static Config.Configuration.MACD_EMA_2;

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
			//cal.add(Calendar.DATE, -15);
			cal.add(Calendar.MILLISECOND, - Configuration.MACD_TIME_PERIOD * (Configuration.MACD_EMA_2 * 2));
			parent.dataHandler.historyMACD_prices.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.historyMACD_EMA1.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.historyMACD_EMA2.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.historyMACD_macd.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.historyMACD_signal.put(cur1 + cur2, new ArrayList<Double>());
			
			// Get enough data for the MACD, according to the MACD strategy
			for(int i = 0; i < Configuration.MACD_EMA_2*2; i++) {
				startTime = cal.getTimeInMillis();
				
				System.out.println("Getting close for " + i);
				double close = parent.restHandler_btf.getLastPrice(cur1, cur2, startTime);
				//double close = parent.dbHandler.getCurrentPrice(cur1, cur2, startTime, -1); // Testing with historic data
				parent.dataHandler.historyMACD_prices.get(cur1 + cur2).add(close);
				
				cal.add(Calendar.MILLISECOND, Configuration.MACD_TIME_PERIOD);
				
				
				try {
					Thread.sleep((60000 / Configuration.NUMBER_OF_API_CALLS_MINUTE));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			initializeEMAs(cur1, cur2);
			
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
			
			long sleepTime = Configuration.MACD_TIME_PERIOD - (new Date().getTime() - d.getTime());
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
		double price = prices.get(prices.size()-1);
		
		double multiplier = (2 / (double)(Configuration.MACD_EMA_1 + 1));
		double prevEma1 = EMA1.get(EMA1.size()-1);
		double currentEma1 = (price * multiplier) + (prevEma1 * (1 - multiplier));
		EMA1.add(currentEma1);
		EMA1.remove(0);
		
		multiplier = (2 / (double)(Configuration.MACD_EMA_2 + 1));
		double prevEma2 = EMA2.get(EMA2.size()-1);
		double currentEma2 = (price * multiplier) + (prevEma2 * (1 - multiplier));
		EMA2.add(currentEma2);
		EMA2.remove(0);
		
		MACD.add(currentEma1 - currentEma2);
		MACD.remove(0);

		multiplier = (2 / (double)(Configuration.MACD_SIGNAL_LINE + 1));
		double prevSignal = signal.get(signal.size()-2);
		double currentSignal = (MACD.get(MACD.size()-1) * multiplier) + (prevSignal * (1 - multiplier));
		signal.add(currentSignal);
		signal.remove(0);
		
		System.out.println("EMA1: " + EMA1);
		System.out.println("EMA2: " + EMA2);
		System.out.println("MACD: " + MACD);
		System.out.println("Signal line: " + signal);
		
		System.out.println(parent.timestampToDate(new Date().getTime()) + ": Histogram: " + (MACD.get(MACD.size()-1) - signal.get(signal.size()-1)));
	}
	
	private void initializeEMAs(String cur1, String cur2) {
		
		List<Double> prices = parent.dataHandler.historyMACD_prices.get(cur1 + cur2);
		List<Double> EMA1 = parent.dataHandler.historyMACD_EMA1.get(cur1 + cur2);
		List<Double> EMA2 = parent.dataHandler.historyMACD_EMA2.get(cur1 + cur2);
		List<Double> MACD = parent.dataHandler.historyMACD_macd.get(cur1 + cur2);
		List<Double> signal = parent.dataHandler.historyMACD_signal.get(cur1 + cur2);
		double currentEma1 = -1;
		double currentEma2 = -1;
		
		double ema1Initial = 0;
		int count = 0;
		for(double p: prices) {
			ema1Initial += p;
			count++;
			
			if(count > Configuration.MACD_EMA_1)
				break;
		}
		ema1Initial /= Configuration.MACD_EMA_1;
		EMA1.add(ema1Initial);
		
		for(int i = 1; i < prices.size() - Configuration.MACD_EMA_1; i++) {
			double multiplier = (2 / (double)(Configuration.MACD_EMA_1 + 1));
			double price = prices.get(Configuration.MACD_EMA_1 + i);
			double prevEma = EMA1.get(EMA1.size()-1);
			
			currentEma1 = (price * multiplier) + (prevEma * (1 - multiplier));
			EMA1.add(currentEma1);
			if(EMA1.size() > Configuration.MACD_EMA_1)
				EMA1.remove(0);
		}
		
		double ema2Initial = 0;
		int count2 = 0;
		for(double p: prices) {
			ema2Initial += p;
			count2++;
			
			if(count2 > Configuration.MACD_EMA_2)
				break;
		}
		ema2Initial /= Configuration.MACD_EMA_2;
		EMA2.add(ema2Initial);
		
		for(int i = 1; i < prices.size() - Configuration.MACD_EMA_2; i++) {
			double multiplier = (2 / (double)(Configuration.MACD_EMA_2 + 1));
			double price = prices.get(Configuration.MACD_EMA_2 + i);
			double prevEma = EMA2.get(EMA2.size()-1);
			
			currentEma2 = (price * multiplier) + (prevEma * (1 - multiplier));
			EMA2.add(currentEma2);
			if(EMA2.size() > Configuration.MACD_EMA_2)
				EMA2.remove(0);
		}
		
		for(int i = 0; i < Configuration.MACD_SIGNAL_LINE; i++) {
			double ema1 = EMA1.get(EMA1.size() - Configuration.MACD_SIGNAL_LINE + i);
			double ema2 = EMA2.get(EMA2.size() - Configuration.MACD_SIGNAL_LINE + i);
			
			MACD.add(ema1 - ema2);
		}
			
		double signalInitial = 0;
		int countS = 0;
		for(double macd: MACD) {
			signalInitial += macd;
			countS++;
			
			if(countS > Configuration.MACD_SIGNAL_LINE)
				break;
		}
		signalInitial /= Configuration.MACD_SIGNAL_LINE;
		signal.add(signalInitial);
		
		for(int i = 1; i < Configuration.MACD_SIGNAL_LINE; i++) {
			double multiplier = (2 /(double) (Configuration.MACD_SIGNAL_LINE + 1));
			double prevSignal = signal.get(i-1);
			
			double currentSignal = (MACD.get(i) * multiplier) + (prevSignal * (1 - multiplier));
			signal.add(currentSignal);
		}
		
		System.out.println("EMA1: " + EMA1);
		System.out.println("EMA2: " + EMA2);
		System.out.println("MACD: " + MACD);
		System.out.println("Signal line: " + signal);
	}
}
