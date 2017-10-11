package Mission;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import Config.ConfigLoader;
import Config.Configuration;
import Data.DataHandler;
import Data.Funds;
import Engine.Pluton;

public class MACDAgent05 {
	
	Pluton parent;
	long startTime;
	boolean keepRunning;
	Date d;
	int seq = 0;
	
	public MACDAgent05(Pluton parent) {
		this.parent = parent;
		this.keepRunning = true;
		d = new Date();
	}
	
	public void start() {

		if(!Configuration.TEST) {
			parent.dataHandler.loadFunds();
			parent.dataHandler.loadShortPositions();
		}

		ConfigLoader configLoader = new ConfigLoader(parent);
		configLoader.fillConfigValues();
		
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
			parent.dataHandler.volume_long_term.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.volume_short_term.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.volume_long_term_sell.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.volume_short_term_sell.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.macd_direction.put(cur1 + cur2, -1);
			parent.dataHandler.macd_funds.put(cur1 + cur2, (double)1000);
			parent.dataHandler.macd_funds_short.put(cur1 + cur2, (double)1000);
			parent.dataHandler.max_macd_histogram.put(cur1 + cur2, new ArrayList<Double>());
			parent.dataHandler.reports.put(cur1 + cur2, "");
			parent.dataHandler.reportsShort.put(cur1 + cur2, "");
			parent.dataHandler.macd_stoploss_long.put(cur1 + cur2, false);
			parent.dataHandler.macd_stoploss_short.put(cur1 + cur2, false);
			parent.dataHandler.tradingHaltLong.put(cur1 + cur2, 0L);
			parent.dataHandler.tradingHaltShort.put(cur1 + cur2, 0L);
			parent.dataHandler.macd_lowest_price.put(cur1 + cur2, 0.0);
			parent.dataHandler.macd_highest_price.put(cur1 + cur2, 0.0);
			
			
			if(Configuration.TEST) {
				parent.dataHandler.getFunds().add(new Funds(cur1, 0, 0));
				parent.dataHandler.getMarginFunds().add(new Funds(cur1, 0, 0));
				parent.dataHandler.testFinished.put(cur1 + cur2, false);
			}
			
			if(parent.dataHandler.getFunds(cur1) == null)
				parent.dataHandler.getFunds().add(new Funds(cur1.toUpperCase(), 0, 0));
			
			if(parent.dataHandler.getMarginFunds(cur1) == null)
				parent.dataHandler.getMarginFunds().add(new Funds(cur1.toUpperCase(), 0, 0));
			
			if(parent.dataHandler.getFunds(cur1).getAmountAvailable() > 0.01)
				parent.dataHandler.macd_direction.put(cur1 + cur2, 1);
			
			if(parent.dataHandler.getMarginFunds(cur1).getAmount() > 0.01) {
				parent.dataHandler.macd_direction.put(cur1 + cur2, 0);
				parent.dataHandler.short_positions.put(cur1 + cur2, parent.dataHandler.getMarginFunds(cur1).getAmount());
			}
			
			if(Configuration.TEST) {
				cal.add(Calendar.DATE, - Configuration.NUMBER_OF_DAYS_BACKLOAD);
				cal.set(Calendar.HOUR, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				startTime = cal.getTimeInMillis();
			} else
				cal.add(Calendar.MILLISECOND, - Configuration.MACD_TIME_PERIOD * (Configuration.MACD_EMA_2 * 2));
			
			List<Double> closingPrices = new ArrayList<Double>();
			List<Double> volumes = new ArrayList<Double>();
			List<Double> volumesShort = new ArrayList<Double>();
			List<Double> volumesSell = new ArrayList<Double>();
			List<Double> volumesShortSell = new ArrayList<Double>();
			// Get enough data for the MACD, according to the MACD strategy
			startTime = cal.getTimeInMillis();
			
			int seqInit = 0;
			long interval = startTime;
			long intervalShort = startTime;
			JSONArray jArray = new JSONArray();	
			
			int arraySize = -1;
			
			if(Configuration.TEST)
				arraySize = 50000;
			else
				arraySize = 1000;
			
			do {
				
				if(!Configuration.TEST) {
					jArray = parent.restHandler_btf.getPriceIntervals(cur1, cur2, startTime);
				}
				else {
					jArray = parent.dbHandler.getPriceIntervals(cur1, cur2, startTime, arraySize);
				}
				
				double currentVolume = 0;
				double currentShortVolume = 0;
				double currentVolumeSell = 0;
				double currentShortVolumeSell = 0;
				
				for(Object obj: jArray) {
					
					double currentPrice = -1;
					
					if(!Configuration.TEST) {
						JSONArray jObj = (JSONArray) obj;
						startTime = jObj.getLong(1);
						currentPrice = jObj.getDouble(3);
						double amount = jObj.getDouble(4);
						
						if(amount > 0) {
							currentVolume += amount;
							currentShortVolume += amount;
						} else {
							currentVolumeSell += amount;
							currentShortVolumeSell += amount;
						}
					}
					else {
						JSONObject jObj = (JSONObject) obj;
						startTime = (Long) jObj.get("timestamp");
						currentPrice =  (Double) jObj.get("price");
						
						double amount = (Double) jObj.get("amount");
						
						if(amount > 0) {
							currentVolume += amount;
							currentShortVolume += amount;
						} else {
							currentVolumeSell += amount;
							currentShortVolumeSell += amount;
						}
					}
					
					parent.dataHandler.macd_current_timestamp.put(cur1 + cur2, startTime);
					
					if(startTime > intervalShort) {
						intervalShort = intervalShort + (Configuration.MACD_TIME_PERIOD / Configuration.NUMBER_OF_TICKS_IN_PERIOD);
						volumesShort.add(currentShortVolume);
						currentShortVolume = 0;

						volumesShortSell.add(currentShortVolumeSell);
						currentShortVolumeSell = 0;
					}
					
					if(startTime > interval) {
						interval += Configuration.MACD_TIME_PERIOD;
						closingPrices.add(currentPrice);
						volumes.add(currentVolume);
						currentVolume = 0;
						
						volumesSell.add(currentVolumeSell);
						currentVolumeSell = 0;
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
				
				if(close < 0)
					System.out.println("close zero");
				
				parent.dataHandler.historyMACD_prices.get(cur1 + cur2).add(close);
			}				
			
			for(double volume: volumes) {
				parent.dataHandler.volume_long_term.get(cur1 + cur2).add(Math.abs(volume));
				
				if(parent.dataHandler.volume_long_term.get(cur1 + cur2).size() > Configuration.VOLUME_LONG_TERM_LENGTH)
					parent.dataHandler.volume_long_term.get(cur1 + cur2).remove(0);
			}
			
			for(double volume: volumesShort) {
				parent.dataHandler.volume_short_term.get(cur1 + cur2).add(Math.abs(volume));
				
				if(parent.dataHandler.volume_short_term.get(cur1 + cur2).size() > Configuration.VOLUME_SHORT_TERM_LENGTH)
					parent.dataHandler.volume_short_term.get(cur1 + cur2).remove(0);
			}
			
			for(double volume: volumesSell) {
				parent.dataHandler.volume_long_term_sell.get(cur1 + cur2).add(Math.abs(volume));
				
				if(parent.dataHandler.volume_long_term_sell.get(cur1 + cur2).size() > Configuration.VOLUME_LONG_TERM_LENGTH)
					parent.dataHandler.volume_long_term_sell.get(cur1 + cur2).remove(0);
			}
			
			for(double volume: volumesShortSell) {
				parent.dataHandler.volume_short_term_sell.get(cur1 + cur2).add(Math.abs(volume));
				
				if(parent.dataHandler.volume_short_term_sell.get(cur1 + cur2).size() > Configuration.VOLUME_SHORT_TERM_LENGTH)
					parent.dataHandler.volume_short_term_sell.get(cur1 + cur2).remove(0);
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
		long endDate = -1;
		
		while(keepRunning) {
			d = new Date();
			
			if(!Configuration.TEST) {
				parent.dataHandler.loadFunds();
				parent.dataHandler.loadShortPositions();
			}
			
			if(Configuration.TEST) {
				keepRunning = false;
				for(String k: parent.dataHandler.testFinished.keySet()) {
					if(!parent.dataHandler.testFinished.get(k))
						keepRunning = true;
				}
			}
			
			boolean newTick = seq % Configuration.NUMBER_OF_TICKS_IN_PERIOD == 0;		
			
			for(String currency: Configuration.CURRENCIES) {
				String cur1 = currency.split("/")[1];
				String cur2 = currency.split("/")[2];
				
				if(parent.dataHandler.testFinished.get(cur1 + cur2))
					continue;
				
				if(parent.dataHandler.getFunds(cur1) == null)
					parent.dataHandler.getFunds().add(new Funds(cur1.toUpperCase(), 0, 0));
				
				if(parent.dataHandler.getMarginFunds(cur1) == null)
					parent.dataHandler.getMarginFunds().add(new Funds(cur1.toUpperCase(), 0, 0));
				
				long timestamp = d.getTime();
				
				if(Configuration.TEST) {
					timestamp = parent.dataHandler.macd_current_timestamp.get(cur1 + cur2);
					if(timestamp > endDate)
						endDate = timestamp;
				}
				
				double price = -1;
				JSONArray jArray = new JSONArray();
				double currentVolume = 0;
				double currentShortVolume = 0;
				double currentVolumeSell = 0;
				double currentShortVolumeSell = 0;
				
				if(!Configuration.TEST) {
					jArray = parent.restHandler_btf.getRecentTrades(timestamp - Configuration.MACD_TIME_PERIOD, timestamp, cur1, cur2);
				}
				else {
					jArray = parent.dbHandler.getPriceIntervals(timestamp - Configuration.MACD_TIME_PERIOD, timestamp, cur1, cur2);
				}
				
				for(Object obj: jArray) {
					
					if(!Configuration.TEST) {
						JSONArray jObj = (JSONArray) obj;
						startTime = jObj.getLong(1);
						price = jObj.getDouble(3);
						double amount = jObj.getDouble(4);
						
						if(amount > 0) {
							currentVolume += amount;
							currentShortVolume += amount;
						} else {
							currentVolumeSell += amount;
							currentShortVolumeSell += amount;
						}
					}
					else {
						JSONObject jObj = (JSONObject) obj;
						startTime = (Long) jObj.get("timestamp");
						price =  (Double) jObj.get("price");
						
						double amount = (Double) jObj.get("amount");
						
						if(amount > 0) {
							currentVolume += amount;
							currentShortVolume += amount;
						} else {
							currentVolumeSell += amount;
							currentShortVolumeSell += amount;
						}
					}
						
				}
				
				if(price < 0)
					price = parent.dataHandler.historyMACD_prices.get(cur1 + cur2)
						.get(parent.dataHandler.historyMACD_prices.get(cur1 + cur2).size() - 1);
				
				parent.dataHandler.current_volume.put(cur1 + cur2, currentVolume);
				parent.dataHandler.current_volume_sell.put(cur1 + cur2, currentVolumeSell);
				
				parent.dataHandler.historyMACD_prices.get(cur1 + cur2).add(price);
				
				parent.dataHandler.volume_short_term.get(cur1 + cur2).add(currentShortVolume);
				if(parent.dataHandler.volume_short_term.get(cur1 + cur2).size() > Configuration.VOLUME_SHORT_TERM_LENGTH)
					parent.dataHandler.volume_short_term.get(cur1 + cur2).remove(0);
				currentShortVolume = 0;
				
				parent.dataHandler.volume_short_term_sell.get(cur1 + cur2).add(currentShortVolumeSell);
				if(parent.dataHandler.volume_short_term_sell.get(cur1 + cur2).size() > Configuration.VOLUME_SHORT_TERM_LENGTH)
					parent.dataHandler.volume_short_term_sell.get(cur1 + cur2).remove(0);
				currentShortVolumeSell = 0;
				
				if(newTick) {
					parent.dataHandler.volume_long_term.get(cur1 + cur2).add(currentVolume);
					if(parent.dataHandler.volume_long_term.get(cur1 + cur2).size() > Configuration.VOLUME_LONG_TERM_LENGTH)
						parent.dataHandler.volume_long_term.get(cur1 + cur2).remove(0);
					currentVolume = 0;
					
					parent.dataHandler.volume_long_term_sell.get(cur1 + cur2).add(currentVolumeSell);
					if(parent.dataHandler.volume_long_term_sell.get(cur1 + cur2).size() > Configuration.VOLUME_LONG_TERM_LENGTH)
						parent.dataHandler.volume_long_term_sell.get(cur1 + cur2).remove(0);
					currentVolumeSell = 0;
				}

				calculateEMAs(cur1, cur2, newTick);
				//calculateRSI(cur1, cur2, newTick);
				
				if(Configuration.TEST) {
					parent.dataHandler.macd_current_timestamp.put(cur1 + cur2,
							parent.dataHandler.macd_current_timestamp.get(cur1 + cur2) + (Configuration.MACD_TIME_PERIOD / 5));
					
					if(parent.dataHandler.macd_current_timestamp.get(cur1 + cur2) > startDate) {
						System.out.println("last date: " + parent.dataHandler.macd_current_timestamp.get(cur1 + cur2));
						parent.dataHandler.testFinished.put(cur1 + cur2, true);
					}
					
				}
				
				if(newTick) {
					parent.dataHandler.historyMACD_prices.get(cur1 + cur2).remove(0);
				}
				else {
					parent.dataHandler.historyMACD_prices.get(cur1 + cur2).remove(parent.dataHandler.historyMACD_prices.get(cur1 + cur2).size() - 1);
				}
			}			
			
			System.out.println("Last trade: " + toDate(endDate));
			
			long sleepTime = (Configuration.MACD_TIME_PERIOD / Configuration.NUMBER_OF_TICKS_IN_PERIOD ) - (new Date().getTime() - d.getTime());
			
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
		
		if(Configuration.TEST && Configuration.TEST_DETAILED_REPORT) {
			for(String key: parent.dataHandler.reports.keySet()) {
				String message = parent.dataHandler.reports.get(key);
				
				System.out.println(message.length());
				
				parent.mailService.sendMail("Test Treport: MACD / " + key, message);
			}
			
			for(String key: parent.dataHandler.reportsShort.keySet()) {
				String message = parent.dataHandler.reportsShort.get(key);
				
				parent.mailService.sendMail("Test STreport: MACD / " + key, message);
			}
		}
		
		String curConfigs = "";
		for(String cur: parent.currencies) {
			
			String cur1 = cur.split("/")[1];
			String cur2 = cur.split("/")[2];
			
			curConfigs += cur1 + cur2 + ":\n";
			curConfigs += "limit buy; " + parent.dataHandler.macd_config_limit_buy.get(cur1 + cur2) + "\n";
			curConfigs += "limit sell: " + parent.dataHandler.macd_config_limit_sell.get(cur1 + cur2) + "\n";
			curConfigs += "gain treshold buy: " + parent.dataHandler.macd_config_hist_gain_treshold_buy.get(cur1 + cur2) + "\n";
			curConfigs += "gain treshold sell: " + parent.dataHandler.macd_config_hist_gain_treshold_sell.get(cur1 + cur2) + "\n";
			curConfigs += "scope: " + parent.dataHandler.macd_config_limit_scope.get(cur1 + cur2) + "\n";
			curConfigs += "stop-loss long: " + parent.dataHandler.macd_stop_loss_limit.get(cur1 + cur2) + "\n";
			curConfigs += "stop-loss short: " + parent.dataHandler.macd_stop_loss_limit_short.get(cur1 + cur2) + "\n";
		}
		
		parent.mailService.sendMail("Test ttreport", "Results: " + parent.dataHandler.totalResults + "\nTotal trades: " + 
				parent.dataHandler.totalTrades +
				"\nMACD1: " + Configuration.MACD_EMA_1 + ", MACD2: " + Configuration.MACD_EMA_2 + ", Signal: " + Configuration.MACD_SIGNAL_LINE +
				"\nMACD_PERIOD_TIME: " + Configuration.MACD_TIME_PERIOD +
				"\nNumber of ticks in period: " + Configuration.NUMBER_OF_TICKS_IN_PERIOD +
				"\nStop-loss trading halt period: " + Configuration.TRADING_HALT_PERIOD +
				"\n" + curConfigs);
		
		parent.mailService.sendMail("Test ttsreport", "Results: " + parent.dataHandler.totalResultsShort + "\nTotal shorts: " + 
				parent.dataHandler.totalTradesShort +
				"\nMACD1: " + Configuration.MACD_EMA_1 + ", MACD2: " + Configuration.MACD_EMA_2 + ", Signal: " + Configuration.MACD_SIGNAL_LINE +
				"\nMACD_PERIOD_TIME: " + Configuration.MACD_TIME_PERIOD +
				"\nNumber of ticks in period: " + Configuration.NUMBER_OF_TICKS_IN_PERIOD +
				"\nStop-loss trading halt period: " + Configuration.TRADING_HALT_PERIOD +
				"\n" + curConfigs);
		
		parent.dataHandler.totalResults = 0;
		parent.dataHandler.totalResultsShort = 0;
		
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
		List<Double> volumeLongTerm = parent.dataHandler.volume_long_term.get(cur1 + cur2);
		List<Double> volumeShortTerm = parent.dataHandler.volume_short_term.get(cur1 + cur2);
		List<Double> volumeLongTermSell = parent.dataHandler.volume_long_term_sell.get(cur1 + cur2);
		List<Double> volumeShortTermSell = parent.dataHandler.volume_short_term_sell.get(cur1 + cur2);
		
		long tradingHaltDate = new Date().getTime();
		
		if(Configuration.TEST)
			tradingHaltDate = parent.dataHandler.macd_current_timestamp.get(cur1 + cur2);
		
		boolean tradingHaltShort = tradingHaltDate - Configuration.TRADING_HALT_PERIOD < parent.dataHandler.tradingHaltShort.get(cur1 + cur2);
		boolean tradingHaltLong =  tradingHaltDate - Configuration.TRADING_HALT_PERIOD < parent.dataHandler.tradingHaltLong.get(cur1 + cur2);
		boolean inertia = parent.dataHandler.inertia.get(cur1 + cur2);
		
		double price = prices.get(prices.size()-1);
		
		double volume = 0;
		double volumeSell = 0;
		
		for(double v: volumeLongTerm) {
			volume += v;
		}
		
		for(double v: volumeLongTermSell) {
			volumeSell += v;
		}
		
		volume = volume / volumeLongTerm.size();
		volumeSell = volumeSell / volumeLongTermSell.size();
		
		double volumeShort = volumeShortTerm.stream().mapToDouble(Double::doubleValue).sum();
		volumeShort = volumeShort / volumeShortTerm.size();
		volumeShort = volumeShort * Configuration.NUMBER_OF_TICKS_IN_PERIOD;
		
		double volumeShortSell = volumeShortTermSell.stream().mapToDouble(Double::doubleValue).sum();
		volumeShortSell = volumeShortSell / volumeShortTermSell.size();
		volumeShortSell = volumeShortSell * Configuration.NUMBER_OF_TICKS_IN_PERIOD;
		
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
		if(limits.size() > parent.dataHandler.macd_config_limit_scope.get(cur1 + cur2))
			limits.remove(0);
		
		double limitSell = (double) Collections.max(limits) / (double) parent.dataHandler.macd_config_limit_sell.get(cur1 + cur2);
		double limitBuy = (double) Collections.max(limits) / (double) parent.dataHandler.macd_config_limit_buy.get(cur1 + cur2);
		
		if(limitBuy == 0)
			System.out.println("hehe");
		
		if(price > parent.dataHandler.macd_highest_price.get(cur1 + cur2))
			parent.dataHandler.macd_highest_price.put(cur1 + cur2, price);

		if(price < parent.dataHandler.macd_lowest_price.get(cur1 + cur2))
			parent.dataHandler.macd_lowest_price.put(cur1 + cur2, price);
		
		int direction = parent.dataHandler.macd_direction.get(cur1 + cur2);

		System.out.println(toDate(parent.dataHandler.macd_current_timestamp.get(cur1 + cur2)));
		
		double changeDir = limits.get(limits.size()-2) - limits.get(limits.size()-1);
		double change  = Math.abs(changeDir);
		
		if(direction == 1 && change / limitSell > parent.dataHandler.macd_config_hist_gain_treshold_sell.get(cur1 + cur2))
			limitSell = 0;
		
		if(direction == 0 && change / limitBuy > parent.dataHandler.macd_config_hist_gain_treshold_buy.get(cur1 + cur2))
			limitBuy = 0;
		
		double currentVolume = parent.dataHandler.current_volume.get(cur1 + cur2);
		double currentVolumeSell = parent.dataHandler.current_volume_sell.get(cur1 + cur2);
		
		// Stop-loss
		if(direction == 1) {
			double gain = price / Double.parseDouble(parent.dataHandler.buyPrices.get(cur1 + cur2 + "MACD"));
			double gainL = price / parent.dataHandler.macd_highest_price.get(cur1 + cur2);
			gain -= 0.004;
			gainL -= 0.004;
			
			if(gainL < parent.dataHandler.macd_stop_loss_limit.get(cur1 + cur2) || gain > Configuration.ROI_GOAL) {
				parent.logger.logCustom("Stop-loss sell signal at " + price + "\nnewTick: " + newTick + "\nlimit: " + limitSell, "macd\\" + cur1 + cur2 + "macd.txt");
				
				double amount = parent.dataHandler.getFunds(cur1.toUpperCase()).getAmountAvailable();
				
				if(price < 0)
					System.out.println("stop loss zero price");
				
				if(!Configuration.TEST) {
					parent.restHandler_btf.placeMarketOrder(cur1, cur2, "sell", "exchange market", amount, price);
					parent.dataHandler.last_sell.put(cur1 + cur2, new Date().getTime());
				}
				
				if(Configuration.TEST) {
					parent.dataHandler.last_sell.put(cur1 + cur2, parent.dataHandler.macd_current_timestamp.get(cur1 + cur2));
				}
				
				parent.dataHandler.sellPrices.put(cur1 + cur2 + "MACD", Double.toString(price));
				parent.dataHandler.sellChanges.put(cur1 + cur2 + "MACD", Double.toString(change));
				
				parent.dataHandler.macd_funds.put(cur1 + cur2,  
						parent.dataHandler.macd_funds.get(cur1 + cur2) + (1000 * gain) - 1000);
				
				parent.dataHandler.totalResults = parent.dataHandler.totalResults + (1000 * gain) - 1000;
				
				parent.logger.logCustom("New funds: " + parent.dataHandler.macd_funds.get(cur1 + cur2), "macd\\" + cur1 + cur2 + "macd.txt");
				
				String mailMessage = 
						"Stop-loss\nBought at " + parent.dataHandler.buyPrices.get(cur1 + cur2 + "MACD") + " - " +
						toDate(parent.dataHandler.last_buy.get(cur1 + cur2)) +
						"\nHistogram: " + parent.dataHandler.last_buy_histogram.get(cur1 + cur2) + ", limit: " + parent.dataHandler.last_buy_limit.get(cur1 + cur2) +
						
						"\nhighest: " + parent.dataHandler.macd_highest_price.get(cur1 + cur2) +
						"\nCurrent volume: " + parent.dataHandler.buy_volume.get(cur1 + cur2) +
						"\nLong term: " + parent.dataHandler.buy_volume_long_term.get(cur1 + cur2) +
						
						"\nSold at " + price + " - " + 
						(Configuration.TEST ? toDate(parent.dataHandler.macd_current_timestamp.get(cur1 + cur2)) : toDate(new Date().getTime())) +
						"\nHistogram: " + (MACD.get(MACD.size()-1) - signal.get(signal.size()-1)) + ", limit: " + limitSell +
						"\nCurrent volume: " + (currentVolume + currentVolumeSell) +
						"\nvolume long term: " + (Math.abs(volume - volumeSell)/2) + 
						
						"\nGain: " + gain + "\n\nNew funds: " + parent.dataHandler.macd_funds.get(cur1 + cur2) + 
						"\n\nNew total funds: " + parent.dataHandler.totalResults +
						"\nnewTick: " + newTick;
				if(!Configuration.TEST) {
					parent.mailService.sendMail("Trade report: MACD / " + cur1 + cur2, mailMessage);
					parent.dataHandler.tradingHaltLong.put(cur1 + cur2, new Date().getTime());
				}
				else
					parent.dataHandler.reports.put(cur1 + cur2,
							parent.dataHandler.reports.get(cur1 + cur2) + "\n----------------\n" + mailMessage);
				
				parent.dataHandler.totalTrades++;
				
				parent.dataHandler.macd_direction.put(cur1 + cur2, -1);
				parent.dataHandler.macd_stoploss_long.put(cur1 + cur2, true);
				parent.dataHandler.tradingHaltLong.put(cur1 + cur2, parent.dataHandler.macd_current_timestamp.get(cur1 + cur2));
			}
		}
		
		if(direction == 0 && Configuration.MARGIN_ENABLED && parent.dataHandler.short_positions.get(cur1 + cur2) != null) {
			double shortPrice = parent.dataHandler.short_positions_price.get(cur1 + cur2);
			double shortAmount = parent.dataHandler.short_positions.get(cur1 + cur2);
			double gain = shortPrice / price;
			gain -= 0.004;
			
			if(gain < parent.dataHandler.macd_stop_loss_limit_short.get(cur1 + cur2) || gain > Configuration.ROI_GOAL ) {

				if(parent.dataHandler.short_positions.get(cur1 + cur2) != null) {
					
					if(!Configuration.TEST)
						parent.restHandler_btf.placeMarketOrder(cur1, cur2, "buy", "market", shortAmount, price);
					
					parent.dataHandler.short_positions.remove(cur1 + cur2);
					parent.dataHandler.short_positions_price.remove(cur1 + cur2);
					
					parent.dataHandler.macd_funds_short.put(cur1 + cur2,  
							parent.dataHandler.macd_funds_short.get(cur1 + cur2) + (1000 * gain) - 1000);
					
					parent.dataHandler.totalResultsShort = parent.dataHandler.totalResultsShort + (1000 * gain) - 1000;
					
					parent.logger.logCustom("New funds: " + parent.dataHandler.macd_funds_short.get(cur1 + cur2), "macd\\" + cur1 + cur2 + "macdshort.txt");
					
					String mailMessage = 
							"Stop-loss" + 
							"\nClosed short position at " + price + " - " + toDate(parent.dataHandler.macd_current_timestamp.get(cur1 + cur2)) +
							"\nSold at " + shortPrice + " - " + toDate(parent.dataHandler.last_sell.get(cur1 + cur2)) +
							"\nGain: " + gain + "\n\nNew Margin funds: " + parent.dataHandler.macd_funds_short.get(cur1 + cur2) + 
							"\n\nNew total margin results: " + parent.dataHandler.totalResultsShort +
							"\nnewTick: " + newTick;
					if(!Configuration.TEST) {
						parent.mailService.sendMail("Short trade report: MACD / " + cur1 + cur2, mailMessage);
						parent.dataHandler.tradingHaltShort.put(cur1 + cur2, new Date().getTime());
					}
					else
						parent.dataHandler.reportsShort.put(cur1 + cur2,
								parent.dataHandler.reportsShort.get(cur1 + cur2) + "\n----------------\n" + mailMessage);
					
					parent.dataHandler.totalTradesShort++;
				}
				
				parent.dataHandler.macd_direction.put(cur1 + cur2, -1);
				parent.dataHandler.macd_stoploss_short.put(cur1 + cur2, true);
				parent.dataHandler.tradingHaltShort.put(cur1 + cur2, parent.dataHandler.macd_current_timestamp.get(cur1 + cur2));
			}

		}
		
		boolean volumeboost = (volumeShort + volumeShortSell) / (Math.abs(volume - volumeSell)/2) > Configuration.VOLUME_TRESHOLD;
		
		if(
		(!inertia && (!tradingHaltShort && MACD.get(MACD.size()-1) - signal.get(signal.size()-1) < - limitSell && (direction == 1 || direction == -1))) ||
		(inertia && (!tradingHaltShort && MACD.get(MACD.size()-1) - signal.get(signal.size()-1) > 0 && MACD.get(MACD.size()-1) + Math.abs(limitSell) < MACD.get(MACD.size()-2) && (direction == 1 || direction == -1)))) {
			if(direction == 1 && !parent.dataHandler.macd_stoploss_long.get(cur1 + cur2)) {
				parent.logger.logCustom("Sell signal at " + price + "\nnewTick: " + newTick + "\nlimit: " + limitSell, "macd\\" + cur1 + cur2 + "macd.txt");

				double amount = parent.dataHandler.getFunds(cur1.toUpperCase()).getAmountAvailable();
				if(price < 0)
					System.out.println("zero price");
				
				if(!Configuration.TEST) {
					parent.restHandler_btf.placeMarketOrder(cur1, cur2, "sell", "exchange market", amount, price);
					
					if(Configuration.MARGIN_ENABLED  && !parent.dataHandler.macd_stoploss_short.get(cur1 + cur2)) {
						double shortAmount = Configuration.BASE_INVESTING_AMOUNT / price;
						parent.restHandler_btf.placeMarketOrder(cur1, cur2, "sell", "market", shortAmount, price);
						parent.dataHandler.short_positions.put(cur1 + cur2, shortAmount);
						parent.dataHandler.short_positions_price.put(cur1 + cur2, price);
						parent.dataHandler.last_sell.put(cur1 + cur2, new Date().getTime());
					}
				}
				
				if(Configuration.TEST) {
					
					if(Configuration.MARGIN_ENABLED  && !parent.dataHandler.macd_stoploss_short.get(cur1 + cur2)) {
						double shortAmount = Configuration.BASE_INVESTING_AMOUNT / price;
						parent.dataHandler.short_positions.put(cur1 + cur2, shortAmount);
						parent.dataHandler.short_positions_price.put(cur1 + cur2, price);
						parent.dataHandler.last_sell.put(cur1 + cur2, parent.dataHandler.macd_current_timestamp.get(cur1 + cur2));
					}
				}
					
				parent.dataHandler.macd_lowest_price.put(cur1 + cur2, price);
				parent.dataHandler.sellPrices.put(cur1 + cur2 + "MACD", Double.toString(price));
				parent.dataHandler.sellChanges.put(cur1 + cur2 + "MACD", Double.toString(change));
				double gain = price / Double.parseDouble(parent.dataHandler.buyPrices.get(cur1 + cur2 + "MACD"));
				gain -= 0.004;
				
				parent.dataHandler.macd_funds.put(cur1 + cur2,  
						parent.dataHandler.macd_funds.get(cur1 + cur2) + (1000 * gain) - 1000);
				
				parent.dataHandler.totalResults = parent.dataHandler.totalResults + (1000 * gain) - 1000;
				
				parent.logger.logCustom("New funds: " + parent.dataHandler.macd_funds.get(cur1 + cur2), "macd\\" + cur1 + cur2 + "macd.txt");
				
				String mailMessage = 
						"Bought at " + parent.dataHandler.buyPrices.get(cur1 + cur2 + "MACD") + " - " +
						toDate(parent.dataHandler.last_buy.get(cur1 + cur2)) +
						"\nChange: " + parent.dataHandler.buyChanges.get(cur1 + cur2 + "MACD") +
						"\nHistogram: " + parent.dataHandler.last_buy_histogram.get(cur1 + cur2) + ", limit: " + parent.dataHandler.last_buy_limit.get(cur1 + cur2) +
						
						"\nCurrent volume: " + parent.dataHandler.buy_volume.get(cur1 + cur2) +
						"\nLong term: " + parent.dataHandler.buy_volume_long_term.get(cur1 + cur2) +
						
						"\nSold at " + price + " - " + 
						(Configuration.TEST ? toDate(parent.dataHandler.macd_current_timestamp.get(cur1 + cur2)) : toDate(new Date().getTime())) +
						"\nChange: " + change +
						"\nHistogram: " + (MACD.get(MACD.size()-1) - signal.get(signal.size()-1)) + ", limit: " + limitSell +
						
						"\nGain: " + gain + "\n\nNew funds: " + parent.dataHandler.macd_funds.get(cur1 + cur2) + 
						"\n\nNew total funds: " + parent.dataHandler.totalResults +
						"\nnewTick: " + newTick + 
						"\nCurrent volume: " + (currentVolume + currentVolumeSell) +
						"\nvolume long term: " + (Math.abs(volume - volumeSell)/2) +
						"\nStop-loss: " + parent.dataHandler.macd_stoploss_long.get(cur1 + cur2);
				if(!Configuration.TEST)
					parent.mailService.sendMail("Trade report: MACD / " + cur1 + cur2, mailMessage);
				else {
					parent.dataHandler.reports.put(cur1 + cur2,
							parent.dataHandler.reports.get(cur1 + cur2) + "\n----------------\n" + mailMessage);
				}
				parent.dataHandler.totalTrades++;
			}
			else
				System.out.println("Setting MACD trend for " + cur1 + cur2 + " to Down.");

			parent.dataHandler.macd_direction.put(cur1 + cur2, 0);
			parent.dataHandler.macd_stoploss_long.put(cur1 + cur2, false);
		}
		else if((inertia && volumeboost && (volumeShort + volumeShortSell) > 0 && changeDir > 0 && (direction == 0 || direction == -1)) ||
				((!inertia && volumeboost && (volumeShort + volumeShortSell) > 0 && (!tradingHaltLong && MACD.get(MACD.size()-1) - signal.get(signal.size()-1) > limitBuy &&  (direction == 0 || direction == -1))) ||
				(inertia && (!tradingHaltLong && MACD.get(MACD.size()-1) - signal.get(signal.size()-1) < 0 && MACD.get(MACD.size()-1) - limitBuy > MACD.get(MACD.size()-2) && (direction == 0 || direction == -1))))) {
			
			if(!parent.dataHandler.macd_stoploss_long.get(cur1 + cur2)) {
				parent.logger.logCustom("Buy signal at " + price + "\nnewTick: " + newTick + "\nlimit: " + limitBuy, "macd\\" + cur1 + cur2 + "macd.txt");
	
				double amount = Configuration.BASE_INVESTING_AMOUNT / price;
				
				if(!Configuration.TEST) {
					parent.restHandler_btf.placeMarketOrder(cur1, cur2, "buy", "exchange market", amount, price);
					parent.dataHandler.getFunds(cur1.toUpperCase()).setAmountAvailable(amount);
				}
					
				if(Configuration.MARGIN_ENABLED) {
					if(parent.dataHandler.short_positions.get(cur1 + cur2) != null) {
						double shortAmount = parent.dataHandler.short_positions.get(cur1 + cur2);
						double shortPrice = parent.dataHandler.short_positions_price.get(cur1 + cur2);
						
						if(!Configuration.TEST)
							parent.restHandler_btf.placeMarketOrder(cur1, cur2, "buy", "market", shortAmount, price);
						
						parent.dataHandler.short_positions.remove(cur1 + cur2);
						parent.dataHandler.short_positions_price.remove(cur1 + cur2);
						
						double gain = shortPrice / price;
						gain -= 0.004;
						
						parent.dataHandler.macd_funds_short.put(cur1 + cur2,  
								parent.dataHandler.macd_funds_short.get(cur1 + cur2) + (1000 * gain) - 1000);
						
						parent.dataHandler.totalResultsShort = parent.dataHandler.totalResultsShort + (1000 * gain) - 1000;
						
						parent.logger.logCustom("New funds: " + parent.dataHandler.macd_funds_short.get(cur1 + cur2), "macd\\" + cur1 + cur2 + "macdshort.txt");
						
						String mailMessage = 
								"Closed short position at " + price + " - " + toDate(parent.dataHandler.macd_current_timestamp.get(cur1 + cur2)) +
								"\nChange: " + change +
								"\nSold at " + shortPrice + " - " + toDate(parent.dataHandler.last_sell.get(cur1 + cur2)) +
								"\nChange: " + parent.dataHandler.sellChanges.get(cur1 + cur2 + "MACD") +
								"\nGain: " + gain + "\n\nNew Margin funds: " + parent.dataHandler.macd_funds_short.get(cur1 + cur2) + 
								"\n\nNew total margin results: " + parent.dataHandler.totalResultsShort +
								"\nnewTick: " + newTick +
								"\nCurrent volume: " + (currentVolume + currentVolumeSell) +
								"\nvolume long term: " + (Math.abs(volume - volumeSell)/2);
						if(!Configuration.TEST)
							parent.mailService.sendMail("Short trade report: MACD / " + cur1 + cur2, mailMessage);
						else {
							if(volumeboost)
								parent.dataHandler.reportsShort.put(cur1 + cur2,
									parent.dataHandler.reportsShort.get(cur1 + cur2) + "\n----------------\n" + mailMessage);
						}
						parent.dataHandler.totalTradesShort++;
						
					}
				}
				
				parent.dataHandler.buyPrices.put(cur1 + cur2 + "MACD", Double.toString(price));
				parent.dataHandler.buyChanges.put(cur1 + cur2 + "MACD", Double.toString(change));
				parent.dataHandler.buy_volume.put(cur1 + cur2, (volumeShort + volumeShortSell));
				parent.dataHandler.buy_volume_long_term.put(cur1 + cur2, (Math.abs(volume - volumeSell)/2));
				
				if(Configuration.TEST)
					parent.dataHandler.last_buy.put(cur1 + cur2, parent.dataHandler.macd_current_timestamp.get(cur1 + cur2));
				else
					parent.dataHandler.last_buy.put(cur1 + cur2, new Date().getTime());
				
				parent.dataHandler.macd_highest_price.put(cur1 + cur2, price);
				parent.dataHandler.last_buy_histogram.put(cur1 + cur2, MACD.get(MACD.size()-1) - signal.get(signal.size()-1));
				parent.dataHandler.last_buy_limit.put(cur1 + cur2, limitBuy);
				parent.dataHandler.macd_direction.put(cur1 + cur2, 1);
			}

			parent.dataHandler.macd_stoploss_short.put(cur1 + cur2, false);
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
		
		double limitStart = 0;
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
		
		
		for(int i = EMA1.size() - 1; i > 0; i--) {
			double ema1 = EMA1.get(i);
			double ema2 = EMA2.get(i);
			
			if(ema1 - ema2 > limitStart)
				limitStart = ema1 - ema2;
		}
		
		// WTF
		limits.add(limitStart);
		
//		System.out.println("EMA1: " + EMA1);
//		System.out.println("EMA2: " + EMA2);
//		System.out.println("MACD: " + MACD);
//		System.out.println("Signal line: " + signal);
		
	}
	
	private String toDate(long date)  {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date);
		
		return new Date(c.getTimeInMillis()).toLocaleString();
	}
}