package Mission;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import Config.Configuration;
import Engine.Pluton;

public class MACDAgent {
	
	Pluton parent;
	long startTime;
	boolean keepRunning;
	Date d;
	int seq = 0;
	
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

			parent.dataHandler.historyMACD_prices.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.historyMACD_EMA1.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.historyMACD_EMA2.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.historyMACD_macd.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.historyMACD_signal.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.macd_direction.put(cur1 + cur2, -1);
			parent.dataHandler.macd_funds.put(cur1 + cur2, (double)1000);
			parent.dataHandler.max_macd_histogram.put(cur1 + cur2, new ArrayList<Double>());
			
			if(Configuration.TEST) {
				cal.add(Calendar.DATE, - Configuration.NUMBER_OF_DAYS_BACKLOAD);
				startTime = cal.getTimeInMillis();
			} else
				cal.add(Calendar.MILLISECOND, - Configuration.MACD_TIME_PERIOD * (Configuration.MACD_EMA_2 * 2));
			
			List<Double> closingPrices = new ArrayList<Double>();
			// Get enough data for the MACD, according to the MACD strategy
			startTime = cal.getTimeInMillis();
			
			int seqInit = 0;
			long interval = startTime;
			JSONArray jArray = new JSONArray();	
			
			int arraySize = -1;
			
			if(Configuration.TEST)
				arraySize = 50000;
			else
				arraySize = 1000;
			
			do {
				
				if(!Configuration.TEST)
					jArray = parent.restHandler_btf.getPriceIntervals(cur1, cur2, startTime);
				else
					jArray = parent.dbHandler.getPriceIntervals(cur1, cur2, startTime, arraySize);
					
				for(Object obj: jArray) {
					
					double currentPrice = -1;
					
					if(!Configuration.TEST) {
						JSONArray jObj = (JSONArray) obj;
						startTime = jObj.getLong(1);
						currentPrice = jObj.getDouble(3);
					}
					else {
						JSONObject jObj = (JSONObject) obj;
						startTime = (Long) jObj.get("timestamp");
						currentPrice =  (Double) jObj.get("price");
					}
						
					parent.dataHandler.macd_current_timestamp.put(cur1 + cur2, startTime);
					if(startTime > interval) {
						interval += Configuration.MACD_TIME_PERIOD;
						closingPrices.add(currentPrice);
						seqInit++;
						
						if(seqInit > 50)
							break;
					}
				}
				
				try {
					Thread.sleep(60000 / Configuration.NUMBER_OF_API_CALLS_MINUTE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			} while ((!Configuration.TEST && jArray.length() % 1000 == 0) || (Configuration.TEST && seqInit < 50));
			
			for(double close: closingPrices) {
				parent.dataHandler.historyMACD_prices.get(cur1 + cur2).add(close);
			}				
				
			try {
				Thread.sleep((60000 / Configuration.NUMBER_OF_API_CALLS_MINUTE));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			initializeEMAs(cur1, cur2);
			//initializeRSI(cur1, cur2);
			
			System.out.println(cur1 + "/" + cur2 + " list initiated as: " + parent.dataHandler.historyMACD_prices.get(cur1 + cur2));
		}
		
		double startDate = new Date().getTime();
		
		while(keepRunning) {
			d = new Date();

			boolean newTick = seq % 5 == 0;
			
			for(String currency: Configuration.CURRENCIES) {
				String cur1 = currency.split("/")[1];
				String cur2 = currency.split("/")[2];
				
				long timestamp = d.getTime();
				
				if(Configuration.TEST) {
					timestamp = parent.dataHandler.macd_current_timestamp.get(cur1 + cur2);
				}
				
				double price = -1;
				if(!Configuration.TEST)
					price = parent.restHandler_btf.getLastPrice(cur1, cur2, timestamp);
				else
					price = parent.dbHandler.getLastPrice(cur1, cur2, timestamp);
				
				parent.dataHandler.historyMACD_prices.get(cur1 + cur2).add(price);

				calculateEMAs(cur1, cur2, newTick);
				//calculateRSI(cur1, cur2, newTick);
				
				if(Configuration.TEST) {
					parent.dataHandler.macd_current_timestamp.put(cur1 + cur2,
							parent.dataHandler.macd_current_timestamp.get(cur1 + cur2) + (Configuration.MACD_TIME_PERIOD / 5));
					
					if(parent.dataHandler.macd_current_timestamp.get(cur1 + cur2) > startDate)
						keepRunning = false;
					
				}

				if(newTick)
					parent.dataHandler.historyMACD_prices.get(cur1 + cur2).remove(0);
				else
					parent.dataHandler.historyMACD_prices.get(cur1 + cur2).remove(parent.dataHandler.historyMACD_prices.get(cur1 + cur2).size() - 1);
			}			
			
			long sleepTime = (Configuration.MACD_TIME_PERIOD / 5 ) - (new Date().getTime() - d.getTime());
			
			if(Configuration.TEST)
				sleepTime = 0;
			
			seq++;
			
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				
			}
			
		}
		
		System.out.println("Finished loop");
	}
	
//	private void initializeRSI(String cur1, String cur2) {
//		double firstAvgGain = 0;
//		double firstAvgLoss = 0;
//
//		List<Double> rsValues = new ArrayList<Double>();
//		List<Double> rsiValues = new ArrayList<Double>();
//		List<Double> rsiGains = new ArrayList<Double>();
//		List<Double> rsiLosses = new ArrayList<Double>();
//		List<Double> stochRsi = new ArrayList<Double>();
//		List<Double> prices = parent.dataHandler.historyMACD_prices.get(cur1 + cur2);
//		
//		parent.dataHandler.historyRSValues.put(cur1 + cur2, rsValues);
//		parent.dataHandler.historyRSIValues.put(cur1 + cur2, rsiValues);
//		parent.dataHandler.historyRSIGain.put(cur1 + cur2, rsiGains);
//		parent.dataHandler.historyRSILoss.put(cur1 + cur2, rsiLosses);
//		parent.dataHandler.historyStochRSI.put(cur1 + cur2, stochRsi);
//		
//		parent.dataHandler.rsi_direction.put(cur1 + cur2, -1);
//		parent.dataHandler.rsi_funds.put(cur1 + cur2, 1000.0);
//		
//		for(int i = 1; i < Configuration.RSI1 + 1; i++) {
//			double gain = parent.dataHandler.historyMACD_prices.get(cur1 + cur2).get(i) - parent.dataHandler.historyMACD_prices.get(cur1 + cur2).get(i-1);
//			double loss = 0;
//			
//			if(gain < 0) {
//				loss = Math.abs(gain);
//				firstAvgLoss += loss;
//			}
//			else
//				firstAvgGain += (gain);
//		}
//
//		firstAvgGain = firstAvgGain / Configuration.RSI1;
//		firstAvgLoss = firstAvgLoss / Configuration.RSI1;
//
//		rsiGains.add(firstAvgGain);
//		rsiLosses.add(firstAvgLoss);
//		rsValues.add(firstAvgGain / firstAvgLoss);
//
//		System.out.println(parent.timestampToDate(new Date().getTime()) + ": First RS: " + rsValues.get(rsValues.size()-1));
//		
//		for(int i = Configuration.RSI1 + 1; i < prices.size(); i++) {
//			double prevGain = rsiGains.get(i - Configuration.RSI1 - 1);
//			double gain = prices.get(i-1) - prices.get(i-2);
//			if(gain < 0)
//				gain = 0;
//			
//			rsiGains.add(((prevGain * (Configuration.RSI1-1)) + gain) / Configuration.RSI1);
//			
//			double prevLoss = rsiLosses.get(i - Configuration.RSI1 - 1);
//			double loss = prices.get(i-1) - prices.get(i-2);
//			if(loss > 0)
//				loss = 0;
//			
//			loss = Math.abs(loss);
//			
//			rsiLosses.add(((prevLoss * (Configuration.RSI1-1)) + loss) / Configuration.RSI1);
//			
//			rsValues.add(rsiGains.get(rsiGains.size() - 1) / rsiLosses.get(rsiLosses.size() - 1));
//			rsiValues.add(100 - (100/(1 + rsValues.get(rsValues.size() - 1))));
//			
//			System.out.println("New RS: " + rsValues.get(rsValues.size() - 1));
//			System.out.println("New RSI: " + rsiValues.get(rsiValues.size() - 1));
//			
//			double lowestRSI = getEdgeRSI(cur1, cur2, 0);
//			double highestRSI = getEdgeRSI(cur1, cur2, 1);
//			
//			double stochRSI = ((rsiValues.get(rsiValues.size()-1) - lowestRSI) / (highestRSI - lowestRSI)) * 100;
//			stochRsi.add(stochRSI);
//			
//			double stochSMA = getSMA(stochRsi, Configuration.RSISTOCH2);
//			
//			System.out.println(parent.timestampToDate(new Date().getTime()) + ": Stoch RSI: " + stochRSI);
//			System.out.println(parent.timestampToDate(new Date().getTime()) + ": Stoch RSI SMA: " + stochSMA);
//		}
//	}
//	
//	private void calculateRSI(String cur1, String cur2, boolean newTick) {
//		List<Double> prices = parent.dataHandler.historyMACD_prices.get(cur1 + cur2);
//		List<Double> rsiGains = parent.dataHandler.historyRSIGain.get(cur1 + cur2);
//		List<Double> rsiLosses = parent.dataHandler.historyRSILoss.get(cur1 + cur2);
//		List<Double> rsValues = parent.dataHandler.historyRSValues.get(cur1 + cur2);
//		List<Double> rsiValues = parent.dataHandler.historyRSIValues.get(cur1 + cur2);
//		List<Double> stochRsiValues = parent.dataHandler.historyStochRSI.get(cur1 + cur2);
//		double price = prices.get(prices.size()-1);
//		
//		double prevGain = rsiGains.get(rsiGains.size()-1);
//		double gain = prices.get(prices.size()-1) - prices.get(prices.size()-2);
//		if(gain < 0)
//			gain = 0;
//		
//		rsiGains.add(((prevGain * (Configuration.RSI1-1)) + gain) / Configuration.RSI1);
//		
//		double prevLoss = rsiLosses.get(rsiLosses.size()-1);
//		double loss = prices.get(prices.size()-1) - prices.get(prices.size()-2);
//		if(loss > 0)
//			loss = 0;
//		
//		loss = Math.abs(loss);
//		
//		rsiLosses.add(((prevLoss * (Configuration.RSI1-1)) + loss) / Configuration.RSI1);
//		
//		rsValues.add(rsiGains.get(rsiGains.size() - 1) / rsiLosses.get(rsiLosses.size() - 1));
//		rsiValues.add(100 - (100/(1 + rsValues.get(rsValues.size() - 1))));
//		
//		System.out.println(parent.timestampToDate(new Date().getTime()) + ": RS: " + (rsValues.get(rsValues.size() -1)));
//		System.out.println(parent.timestampToDate(new Date().getTime()) + ": RSI: " + (rsiValues.get(rsiValues.size() -1)));
//		
//		double lowestRSI = getEdgeRSI(cur1, cur2, 0);
//		double highestRSI = getEdgeRSI(cur1, cur2, 1);
//		
//		double stochRSI = ((rsiValues.get(rsiValues.size()-1) - lowestRSI) / (highestRSI - lowestRSI)) * 100;
//		stochRsiValues.add(stochRSI);
//		
//		double stochSMA = getSMA(stochRsiValues, Configuration.RSISTOCH2);
//		
//		System.out.println(parent.timestampToDate(new Date().getTime()) + ": Stoch RSI: " + stochRSI);
//		System.out.println(parent.timestampToDate(new Date().getTime()) + ": Stoch RSI SMA: " + stochSMA);
//		
//		int direction = parent.dataHandler.rsi_direction.get(cur1 + cur2);
//		if(stochRSI > stochSMA && (direction == 1 || direction == -1)) {
//			
//			if(direction == 1) {
//				parent.logger.logCustom("Sell signal at " + price + "\nnewTick: " + newTick,  "rsi\\" + cur1 + cur2 + "rsi.txt");
//				parent.dataHandler.sellPrices.put(cur1 + cur2 + "RSI", Double.toString(price));
//				double tradeGain = price / Double.parseDouble(parent.dataHandler.buyPrices.get(cur1 + cur2 + "RSI"));
//				tradeGain -= 0.004;
//				
//				parent.dataHandler.rsi_funds.put(cur1 + cur2,  
//						parent.dataHandler.rsi_funds.get(cur1 + cur2) + (1000 * tradeGain) - 1000);
//
//				parent.dataHandler.totalResults += (1000 * gain) - 1000;
//				
//				parent.logger.logCustom("New funds: " + parent.dataHandler.rsi_funds.get(cur1 + cur2), "rsi\\" + cur1 + cur2 + "rsi.txt");
//				
//				String mailMessage = "Bought at " + parent.dataHandler.buyPrices.get(cur1 + cur2 + "RSI") + "\nSold at " + price + 
//						"\nGain: " + tradeGain + "\n\nNew funds: " + parent.dataHandler.rsi_funds.get(cur1 + cur2) + 
//						 "\n\nNew total funds: " + parent.dataHandler.totalResults +
//						 "\nnewTick: " + newTick;
//				parent.mailService.sendMail("Trade report: RSI / " + cur1 + cur2, mailMessage);
//			}
//			else
//				System.out.println("Setting StochRSI trend for " + cur1 + cur2 + " to Down.");
//			
//			parent.dataHandler.rsi_direction.put(cur1 + cur2, 0);
//		}
//		else if(stochRSI < stochSMA && (direction == 0 || direction == -1)) {
//			
//			parent.logger.logCustom("Buy signal at " + price + "newTick: " + newTick,  "rsi\\" + cur1 + cur2 + "rsi.txt");
//			parent.dataHandler.buyPrices.put(cur1 + cur2 + "RSI", Double.toString(price));
//			parent.dataHandler.rsi_direction.put(cur1 + cur2, 1);
//		}
//		
//		if(!newTick) {
//			rsiGains.remove(rsiGains.size() -1 );
//			rsiLosses.remove(rsiLosses.size() - 1);
//			rsValues.remove(rsValues.size() - 1);
//			rsiValues.remove(rsiValues.size() - 1);
//			stochRsiValues.remove(stochRsiValues.size() - 1);
//		}
//	}
	
	private double getSMA(List<Double> list, int length) {
		
		if(length > list.size())
			length = list.size();
		
		List<Double> subList = list.subList(list.size() - length, list.size());

		double total = 0;
		for(double item: subList)
			total += item;
		
		return total / (double)length;
	}
	
	private double getEdgeRSI(String cur1, String cur2, int high) {
		double rsi = 0;
		
		int size = parent.dataHandler.historyRSIValues.get(cur1 + cur2).size();
		if(size > Configuration.RSISTOCH1)
			size = Configuration.RSISTOCH1;
		
		List<Double> lastRSI = parent.dataHandler.historyRSIValues.get(cur1 + cur2)
				.subList(parent.dataHandler.historyRSIValues.get(cur1 + cur2).size() - size,
						parent.dataHandler.historyRSIValues.get(cur1 + cur2).size() - 1);
		
		for(Double r: lastRSI) {
			if(high == 1 && r > rsi)
				rsi = r;
			
			if(high != 1 && r < rsi)
				rsi = r;
		}
		
		return rsi;
	}
	
	
	private void calculateEMAs(String cur1, String cur2, boolean newTick) {

		List<Double> prices = parent.dataHandler.historyMACD_prices.get(cur1 + cur2);
		List<Double> EMA1 = parent.dataHandler.historyMACD_EMA1.get(cur1 + cur2);
		List<Double> EMA2 = parent.dataHandler.historyMACD_EMA2.get(cur1 + cur2);
		List<Double> MACD = parent.dataHandler.historyMACD_macd.get(cur1 + cur2);
		List<Double> signal = parent.dataHandler.historyMACD_signal.get(cur1 + cur2);
		List<Double> limits = parent.dataHandler.max_macd_histogram.get(cur1 + cur2);
		
		double price = prices.get(prices.size()-1);
		
		double multiplier = (2 / (double)(Configuration.MACD_EMA_1 + 1));
		double prevEma1 = EMA1.get(EMA1.size()-1);
		double currentEma1 = (price * multiplier) + (prevEma1 * (1 - multiplier));
		EMA1.add(currentEma1);
		
		multiplier = (2 / (double)(Configuration.MACD_EMA_2 + 1));
		double prevEma2 = EMA2.get(EMA2.size()-1);
		double currentEma2 = (price * multiplier) + (prevEma2 * (1 - multiplier));
		EMA2.add(currentEma2);
		
		MACD.add(currentEma1 - currentEma2);

		multiplier = (2 / (double)(Configuration.MACD_SIGNAL_LINE + 1));
		double prevSignal = signal.get(signal.size()-1);
		double currentSignal = (MACD.get(MACD.size()-1) * multiplier) + (prevSignal * (1 - multiplier));
		signal.add(currentSignal);
		
		System.out.println("EMA1: " + EMA1);
		System.out.println("EMA2: " + EMA2);
		System.out.println("MACD: " + MACD);
		System.out.println("Signal line: " + signal);
		
		System.out.println(parent.timestampToDate(new Date().getTime()) + ": Histogram: " + (MACD.get(MACD.size()-1) - signal.get(signal.size()-1)));
		
		limits.add(MACD.get(MACD.size()-1) - signal.get(signal.size()-1));
		if(limits.size() > Configuration.MACD_LIMIT_SCOPE)
			limits.remove(0);
		
		double limit = (double) Collections.max(limits) / (double) Configuration.MACD_LIMIT;
		
		int direction = parent.dataHandler.macd_direction.get(cur1 + cur2);
		
		if(MACD.get(MACD.size()-1) - signal.get(signal.size()-1) < - limit && (direction == 1 || direction == -1)) {
			if(direction == 1) {
				parent.logger.logCustom("Sell signal at " + price + "\nnewTick: " + newTick + "\nlimit: " + limit, "macd\\" + cur1 + cur2 + "macd.txt");
				parent.dataHandler.sellPrices.put(cur1 + cur2 + "MACD", Double.toString(price));
				parent.dataHandler.last_sell.put(cur1 + cur2, new Date().getTime());
				double gain = price / Double.parseDouble(parent.dataHandler.buyPrices.get(cur1 + cur2 + "MACD"));
				gain -= 0.004;
				
				parent.dataHandler.macd_funds.put(cur1 + cur2,  
						parent.dataHandler.macd_funds.get(cur1 + cur2) + (1000 * gain) - 1000);
				
				parent.dataHandler.totalResults = parent.dataHandler.totalResults + (1000 * gain) - 1000;
				
				parent.logger.logCustom("New funds: " + parent.dataHandler.macd_funds.get(cur1 + cur2), "macd\\" + cur1 + cur2 + "macd.txt");
				
				String mailMessage = 
						"Bought at " + parent.dataHandler.buyPrices.get(cur1 + cur2 + "MACD") + " - " +
						toDate(parent.dataHandler.last_buy.get(cur1 + cur2)) +
						"\nHistogram: " + parent.dataHandler.last_buy_histogram.get(cur1 + cur2) + ", limit: " + parent.dataHandler.last_buy_limit.get(cur1 + cur2) +
						
						"\nSold at " + price + " - " + 
						toDate(parent.dataHandler.macd_current_timestamp.get(cur1 + cur2)) +
						"\nHistogram: " + (MACD.get(MACD.size()-1) - signal.get(signal.size()-1)) + ", limit: " + limit +
						
						"\nGain: " + gain + "\n\nNew funds: " + parent.dataHandler.macd_funds.get(cur1 + cur2) + 
						"\n\nNew total funds: " + parent.dataHandler.totalResults +
						"\nnewTick: " + newTick;
				parent.mailService.sendMail("Trade report: MACD / " + cur1 + cur2, mailMessage);
			}
			else
				System.out.println("Setting MACD trend for " + cur1 + cur2 + " to Down.");

			parent.dataHandler.macd_direction.put(cur1 + cur2, 0);
		}
		else if(MACD.get(MACD.size()-1) - signal.get(signal.size()-1) > limit &&  (direction == 0 || direction == -1)) {
			
			parent.logger.logCustom("Buy signal at " + price + "\nnewTick: " + newTick + "\nlimit: " + limit, "macd\\" + cur1 + cur2 + "macd.txt");
			parent.dataHandler.buyPrices.put(cur1 + cur2 + "MACD", Double.toString(price));
			parent.dataHandler.last_buy.put(cur1 + cur2, parent.dataHandler.macd_current_timestamp.get(cur1 + cur2));
			parent.dataHandler.last_buy_histogram.put(cur1 + cur2, MACD.get(MACD.size()-1) - signal.get(signal.size()-1));
			parent.dataHandler.last_buy_limit.put(cur1 + cur2, limit);
			parent.dataHandler.macd_direction.put(cur1 + cur2, 1);
		}
		
		if(!newTick) {
			EMA1.remove(EMA1.size() - 1);
			EMA2.remove(EMA2.size() - 1);
			MACD.remove(MACD.size() - 1);
			signal.remove(signal.size() - 1);
		} else {
			EMA1.remove(0);
			EMA2.remove(0);
			MACD.remove(0);
			signal.remove(0);
		}
	}
	
	private void initializeEMAs(String cur1, String cur2) {
		
		List<Double> prices = parent.dataHandler.historyMACD_prices.get(cur1 + cur2);
		List<Double> EMA1 = parent.dataHandler.historyMACD_EMA1.get(cur1 + cur2);
		List<Double> EMA2 = parent.dataHandler.historyMACD_EMA2.get(cur1 + cur2);
		List<Double> MACD = parent.dataHandler.historyMACD_macd.get(cur1 + cur2);
		List<Double> signal = parent.dataHandler.historyMACD_signal.get(cur1 + cur2);
		List<Double> limits = parent.dataHandler.max_macd_histogram.get(cur1 + cur2);
		
		double currentEma1 = -1;
		double currentEma2 = -1;
		
		double ema1Initial = 0;
		int count = 0;
		for(double p: prices) {
			ema1Initial += p;
			count++;
			
			if(count >= Configuration.MACD_EMA_1)
				break;
		}
		ema1Initial /= Configuration.MACD_EMA_1;
		EMA1.add(ema1Initial);
		
		for(int i = 0; i < prices.size() - Configuration.MACD_EMA_1; i++) {
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
			
			if(count2 >= Configuration.MACD_EMA_2)
				break;
		}
		ema2Initial /= Configuration.MACD_EMA_2;
		EMA2.add(ema2Initial);
		
		for(int i = 0; i < prices.size() - Configuration.MACD_EMA_2; i++) {
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
			
			if(countS >= Configuration.MACD_SIGNAL_LINE)
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
		
		limits.add(Math.abs(MACD.get(MACD.size()-1) - signal.get(signal.size()-1)));
		if(limits.size() > Configuration.MACD_LIMIT_SCOPE)
			limits.remove(0);
		
		System.out.println("EMA1: " + EMA1);
		System.out.println("EMA2: " + EMA2);
		System.out.println("MACD: " + MACD);
		System.out.println("Signal line: " + signal);
		
	}
	
	private String toDate(long date)  {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date);
		
		return new Date(c.getTimeInMillis()).toLocaleString();
	}
}