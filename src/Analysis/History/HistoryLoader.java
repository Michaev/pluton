package Analysis.History;

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

public class HistoryLoader {
	
	Pluton parent;
	long startTime;
	
	public HistoryLoader(Pluton parent) {
		this.parent = parent;
		this.startTime = new Date().getTime();
	}
	
	private  String[] shiftArray(String[] history, String newRow, int size) {
		
		String[] tempArray = new String[size];
		
		for(int i = 1; i < history.length; i++) {
			tempArray[i-1] = history[i];
		}
		
		tempArray[tempArray.length-1] = newRow;
		
		return tempArray;
	}
	
	public void findJumpsV2(String currency) {
		String cur1 = currency.split("/")[1];
		String cur2 = currency.split("/")[2];
		
		List<String> rows = parent.dbHandler.getDataPoints(cur1, cur2, Configuration.INTERVAL_TICK_GEN);
		
		long start;
		long stop;
		double gain;
		double volume;
		
		int state = 0; 
		double buyPrice = -1;
		double peakPrice = -1;
		long buyTimestamp = -1;
		int countGrowing = 0;
		
		int trades = 0;
		
		/*
		 * 0: Looking for opportunities 
		 * 1: Trying to buy
		 * 2: Bought, waiting to sell
		 * 3: Trying to sell
		 */

		String[] history = new String[8];
		
		List<String> gains = new ArrayList<String>();
		

		double avgVolume = parent.dbHandler.get24HVolume(cur1, cur2); // Simplified avg volume
		avgVolume /= (24 * 60 * 60 * 1000);
		avgVolume *= Configuration.INTERVAL_TICK_GEN;
		
		int count = 0;
		int countD = 0;
		int countC = 0;
		
		for(String row : rows) {
			start = Long.parseLong(row.split(",")[0]);
			stop = Long.parseLong(row.split(",")[1]);
			gain = Double.parseDouble(row.split(",")[2]);
			volume = Double.parseDouble(row.split(",")[3]);

			if(avgVolume == -1) {
				//System.out.println("Not enough volume data. " + start);
				continue;
			}
			
			double volumeRatio = volume / avgVolume;
			
			if(gain > Configuration.JUMP_LIMIT && volumeRatio > Configuration.JUMP_LIMIT_VOL)
				countGrowing++;
			else
				countGrowing = 0;
			
//			if(countGrowing > 3) {
//				if(state != 2) {
//					state = 2; // Simulation, jumping to state 2
//				
//					buyPrice = parent.dbHandler.getCurrentPrice(cur1, cur2, start + Configuration.INTERVAL_TICK_GEN, 1);
//					peakPrice = buyPrice;
//					buyTimestamp = start + Configuration.INTERVAL_TICK_GEN;
//					parent.logger.logTrade("Buying " + cur1 + "/" + cur2 + " at " + parent.timestampToDate(start + Configuration.INTERVAL_TICK_GEN) + " for " + buyPrice + " (consecutive growth)");
//				}
//			}
			
			if(gain > Configuration.JUMP_LIMIT && volumeRatio > Configuration.JUMP_LIMIT_VOL) {
				System.out.println("Found jump above limit: " + gain + " (" + volumeRatio + ") at " + parent.timestampToDate(start));

				List<String> zoomRows = parent.dbHandler.getDataPoints(cur1, cur2, Configuration.INTERVAL_TICK_GEN, start-(5*Configuration.INTERVAL_TICK_GEN), start + Configuration.INTERVAL_TICK_GEN*15);
				
				if(state != 2) {
					state = 2; // Simulation, jumping to state 2
				
					buyPrice = parent.dbHandler.getCurrentPrice(cur1, cur2, start + Configuration.INTERVAL_TICK_GEN, 1);
					peakPrice = buyPrice;
					buyTimestamp = start + Configuration.INTERVAL_TICK_GEN;
					parent.logger.logTrade("Buying " + cur1 + "/" + cur2 + " at " + parent.timestampToDate(start + Configuration.INTERVAL_TICK_GEN) + " for " + buyPrice + " (gain and volume higher than treshold, " + gain + ", " + volumeRatio);
					trades++;
				}
				
				
				for(String zoomRow: zoomRows) {
					long zstart = Long.parseLong(zoomRow.split(",")[0]);
					double zgain = Double.parseDouble(zoomRow.split(",")[2]);
					double zvolume = Double.parseDouble(zoomRow.split(",")[3]);
					
					double zVolumeRatio = zvolume / (avgVolume / 5);
					
					System.out.println("Micro trade at " + zstart + " " + parent.timestampToDate(zstart) + " (" + zVolumeRatio + "): " + zgain);
					
				}
				System.out.println("End microtrade ----------------------");
				System.out.println();
			}
			
			if(state == 2) {
				double currentPrice = parent.dbHandler.getCurrentPrice(cur1, cur2, start, -1);
				if(currentPrice > peakPrice)
					peakPrice = currentPrice;
				
				double lossFromPeak = currentPrice / peakPrice;
				double gainFromStart = currentPrice / buyPrice;
				
				if(lossFromPeak <= Configuration.STOP_LOSS_LIMIT && start > buyTimestamp) {
					state = 3;
					parent.logger.logTrade(cur1 + "/" + cur2 + ": Price dropped " + lossFromPeak + " at " + parent.timestampToDate(start) + ". Selling out. (price dropped below treshold)");
				}
				
				if(gainFromStart >= Configuration.ROI_GOAL && start > buyTimestamp) {
					state = 3;
					parent.logger.logTrade(cur1 + "/" + cur2 + ": Price increased " + lossFromPeak + " at " + parent.timestampToDate(start) + ". Selling out. (price hit ROI goal)");
				}
			}
			
			if(state == 3) {
				double price = parent.dbHandler.getCurrentPrice(cur1, cur2, start + Configuration.INTERVAL_TICK_GEN, 1);
				double mPrice = parent.dbHandler.getLowestPrice(cur1, cur2, start, start + (Configuration.INTERVAL_TICK_GEN/2));
				
				if(mPrice / peakPrice <= Configuration.STOP_LOSS_LIMIT && mPrice > price) {
					price = parent.dbHandler.getCurrentPrice(cur1, cur2, start + (Configuration.INTERVAL_TICK_GEN/2), 1);
					parent.logger.logTrade("Selling " + cur1 + "/" + cur2 + " at " + parent.timestampToDate(start + (Configuration.INTERVAL_TICK_GEN/2)) + " for " + price + " (intercepted)");
				} else {
					parent.logger.logTrade("Selling " + cur1 + "/" + cur2 + " at " + parent.timestampToDate(start + Configuration.INTERVAL_TICK_GEN) + " for " + price);
				}
				
				trades++;
				double tradeGain = (price / buyPrice) - 0.003;
				parent.logger.logTrade("Total gain: " + tradeGain);
				
				gains.add("" + tradeGain);
				
				parent.funds.setAmountAvailable(parent.funds.getAmountAvailable() + ((1000 * tradeGain) - 1000));
				parent.logger.logTrade("New funds: " + parent.funds.getAmountAvailable());
				state = 0;
			}
			
			if(gain <= Configuration.STOP_LOSS_LIMIT && false) {
				
				if(state == 2) {
					double price = parent.dbHandler.getCurrentPrice(cur1, cur2, start + Configuration.INTERVAL_TICK_GEN, 1);
					parent.logger.logTrade("Selling " + cur1 + "/" + cur2 + " at " + parent.timestampToDate(start + Configuration.INTERVAL_TICK_GEN) + " for " + price);
					parent.logger.logTrade("Total gain: " + price / buyPrice);
					
					parent.funds.setAmountAvailable(parent.funds.getAmountAvailable() + (1000 * (price / buyPrice) - 1000));
					parent.logger.logTrade("New funds: " + parent.funds.getAmountAvailable());
					
					state = 0;
				}
			}
		}

		parent.logger.logTrade("Gain limit: " + Configuration.JUMP_LIMIT);
		parent.logger.logTrade("Gain limit vol: " + Configuration.JUMP_LIMIT_VOL);
		parent.logger.logTrade("Total trades: " + trades);

		parent.logger.logCustom("Gain limit: " + Configuration.JUMP_LIMIT, "configs_" + cur1 + "_" + cur2 + ".txt");
		parent.logger.logCustom("Gain limit vol: " + Configuration.JUMP_LIMIT_VOL, "configs_" + cur1 + "_" + cur2 + ".txt");
		parent.logger.logCustom("New funds: " + parent.funds.getAmountAvailable(), "configs_" + cur1 + "_" + cur2 + ".txt");
		parent.logger.logCustom("Total trades: " + trades, "configs_" + cur1 + "_" + cur2 + ".txt");
		parent.logger.logCustom("", "configs_" + cur1 + "_" + cur2 + ".txt");
		
		System.out.println();
		System.out.println("List of gains:");
		gains.sort(Comparator.comparing(Double::parseDouble));
		for(String g: gains) 
			parent.logger.logTrade(g);
	}
	
	public void findJumps(String currency) {
		String cur1 = currency.split("/")[1];
		String cur2 = currency.split("/")[2];
		
		List<String> rows = parent.dbHandler.getDataPoints(cur1, cur2, Configuration.INTERVAL_TICK_GEN);
		
		long start;
		long stop;
		double gain;
		double volume;
		int consecutive = 0;
		int consecutiveDuck = 0;
		
		boolean checkForTurning = false;
		boolean checkForTurning2 = false;
		int falsePositivesTurning = 0;
		int correctTurning = 0;
		int falsePositivesTurning2 = 0;
		int correctTurning2 = 0;
		
		double turningFactor = 1;
		
		String[] history = new String[8];
		

		double avgVolume = parent.dbHandler.get24HVolume(cur1, cur2); // Simplified avg volume
		avgVolume /= (24 * 60 * 60 * 1000);
		avgVolume *= Configuration.INTERVAL_TICK_GEN;
		
		int count = 0;
		int countD = 0;
		int countC = 0;
		
		for(String row : rows) {
			start = Long.parseLong(row.split(",")[0]);
			stop = Long.parseLong(row.split(",")[1]);
			gain = Double.parseDouble(row.split(",")[2]);
			volume = Double.parseDouble(row.split(",")[3]);

			//double avgVolume = parent.dbHandler.getAverageVolume(cur1, cur2, start);
			
			if(avgVolume == -1) {
				//System.out.println("Not enough volume data. " + start);
				continue;
			}
			
//			if(checkForTurning2) {
//				if(isDescending(row))
//					falsePositivesTurning2++;
//				else {
//					correctTurning2++;
//					turningFactor *= gain;
//					
//					System.out.println("Turn successful. Gain: " + turningFactor);
//				}
//				checkForTurning2 = false;
//			}
			
//			if(checkForTurning) {
//				if(isDescending(row)) {
//					falsePositivesTurning++;
//					checkForTurning2 = false;
//				}
//				else {
//					correctTurning++;
//					checkForTurning2 = true;
//					turningFactor = gain;
//				}
//				checkForTurning = false;
//			}
			
			double volumeRatio = volume / avgVolume;
			
			if(consecutive > 0) {
				System.out.println("Tick after jump: " + gain + " at vol " + volumeRatio + "(" + parent.timestampToDate(start) + ")");
				System.out.println();
			}
			
			if(consecutiveDuck > 0) {
				System.out.println("Tick after duck: " + gain + " at vol " + volumeRatio + "(" + parent.timestampToDate(start) + ")");
				System.out.println();
			}
			
			if(gain > Configuration.JUMP_LIMIT && volumeRatio > Configuration.JUMP_LIMIT_VOL) {
				consecutive++;
				System.out.println("Found jump above limit: " + gain + " (" + volumeRatio + ") at " + parent.timestampToDate(start));
//				System.out.println(history[0]);
//				System.out.println(history[1]);
//				System.out.println(history[2]);
//				System.out.println(history[3]);
//				System.out.println(history[4]);
//				System.out.println(history[5]);
//				System.out.println(history[6]);
//				System.out.println(history[7]);
//				System.out.println();
				
				List<String> zoomRows = parent.dbHandler.getDataPoints(cur1, cur2, 60000, start-1, start + Configuration.INTERVAL_TICK_GEN*3);
				
				for(String zoomRow: zoomRows) {
					long zstart = Long.parseLong(zoomRow.split(",")[0]);
					double zgain = Double.parseDouble(zoomRow.split(",")[2]);
					double zvolume = Double.parseDouble(zoomRow.split(",")[3]);
					
					double zVolumeRatio = zvolume / (avgVolume / 5);
					
					if(zstart == start || zstart == stop) System.out.println("-------");
					System.out.println("Micro trade at " + zstart + " " + parent.timestampToDate(zstart) + " (" + zVolumeRatio + "): " + zgain);
					if(zstart == start || zstart == stop) System.out.println("-------");
					
				}
				System.out.println("End microtrade ----------------------");
				System.out.println();
				
				
				count++;
				
				if(consecutive > 1) {
					System.out.println("Consecutive jump(" + consecutive + "): " + gain + " (" + volumeRatio + ") at " + parent.timestampToDate(start));
					countC++;
					
					if(volumeRatio > Configuration.JUMP_LIMIT_VOL * 2) {
						System.out.println("Still going up?");
					} else {
						System.out.println("Probably turning.");
					}
					
				}
				
//				if(previousDescending(history) > 1) {
//					System.out.println(previousDescending(history) + " desc. Turning point? : " + gain + " (" + volumeRatio + ") at " + parent.timestampToDate(start));
//					checkForTurning = true;
//				} 
			}
			else
				consecutive = 0;
			
			if(gain <= 2-(Configuration.JUMP_LIMIT) && volumeRatio > Configuration.JUMP_LIMIT_VOL) {
				consecutiveDuck++;
				System.out.println("Found duck above limit: " + gain + " (" + volumeRatio + ") at " + parent.timestampToDate(start));
				List<String> zoomRows = parent.dbHandler.getDataPoints(cur1, cur2, 60000, start-1, start + Configuration.INTERVAL_TICK_GEN*3);
				
				for(String zoomRow: zoomRows) {
					long zstart = Long.parseLong(zoomRow.split(",")[0]);
					double zgain = Double.parseDouble(zoomRow.split(",")[2]);
					double zvolume = Double.parseDouble(zoomRow.split(",")[3]);
					
					double zVolumeRatio = zvolume / (avgVolume / 5);
					
					if(zstart == start || zstart == stop) System.out.println("-------");
					System.out.println("Micro trade at " + zstart + " " + parent.timestampToDate(zstart) + " (" + zVolumeRatio + "): " + zgain);
					if(zstart == start || zstart == stop) System.out.println("-------");
					
				}
				System.out.println("End microtrade ----------------------");
				System.out.println();
				countD++;
			} else
				consecutiveDuck = 0;
			
			history = shiftArray(history, row, 8);
		}
		
		System.out.println("Total jumps: " + count);
		System.out.println("Total consecutive: " + countC);

		System.out.println("Total ducks: " + countD);
		
//		System.out.println("Total false positives: " + falsePositivesTurning);
//		System.out.println("Total correct turn predictions: " + correctTurning);
//
//		System.out.println("Total false positives2: " + falsePositivesTurning2);
//		System.out.println("Total correct turn predictions2: " + correctTurning2);
	}
	
	private int previousDescending(String[] history) {
		int count = 0;
		
		for(int i = history.length-1; i >= 0; i--) {
			if(isDescending(history[i]))
				count++;
			else return count;
		}
		
		return count;
	}
	
	private boolean isDescending(String row) {
		return Double.parseDouble(row.split(",")[2]) < 1;
	}
	
	public void load(String currency) {
		String exchange = currency.split("/")[0];
		String cur1 = currency.split("/")[1];
		String cur2 = currency.split("/")[2];
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -Configuration.NUMBER_OF_DAYS_BACKLOAD);
		
		long timestamp = cal.getTime().getTime(); 				// v2
		//long timestamp = cal.getTime().getTime();  / 1000 	// Bfx API v1 receives timestamp parameters in seconds
		
		long lastLoad = -1;

		parent.dbHandler.createTable(cur1, cur2);
		
		do {
			System.out.println("Time since last load: " + (new Date().getTime() - lastLoad));
			lastLoad = new Date().getTime();
			
			parent.logger.logDebug("Loading trades starting at timestamp " + timestamp);
			timestamp = loadHistoryArray(timestamp, cur1, cur2);
			
			long sleepTime = new Date().getTime() - lastLoad - (60000 / Configuration.NUMBER_OF_API_CALLS_MINUTE);
			if(sleepTime < 0) {
				try {
					System.out.println("Sleeping " + -sleepTime + "ms to avoid API block");
					Thread.sleep(-sleepTime);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		} while(timestamp != -1);
		
		parent.dbHandler.calculateAvgVolume(cal.getTime().getTime(), new Date().getTime(), cur1, cur2);
		
		parent.logger.logDebug("Exiting HistoryLoader");
	}
	
	private long loadHistoryArray(long timestamp, String cur1, String cur2) {
		
		System.out.println("loadHistoryArray starting at " + parent.timestampToDate(timestamp));
		
		JSONArray jsonArray = parent.restHandler_btf.getTrades(timestamp, cur1, cur2);
		
		long latestTimestamp = parent.dbHandler.insertTrade(jsonArray, cur1, cur2);
		
		if(latestTimestamp > startTime)  {
			latestTimestamp = -1;
		}
		
		if(latestTimestamp == -2) {
			latestTimestamp = timestamp + (3600 * 1000); // Increase timestamp by one hour manually to avoid exit on empty json returns.
		}
			
		return latestTimestamp;
	}
	
	public void generateTicks(long start, long stop, long intervals, String currency) {
		
		System.out.println("Generating ticks with interval: " + intervals);
		
		String exchange = currency.split("/")[0];
		String cur1 = currency.split("/")[1];
		String cur2 = currency.split("/")[2];
		
		double open = -1;
		double close = -1;
		double gain = -1;
		double low = -1;
		double high = -1;
		double volume = -1;

		long currentStamp = start;
		long currentStop = start + intervals;
		Object[] data = new Object[7];
		
		parent.dbHandler.createDataPointsTable(cur1, cur2, intervals);
		
		do {
			data = parent.dbHandler.getDataPoint(cur1, cur2, currentStamp, intervals);
			
			if(data[2] == null) {
				open = 1;
				close = 1;
				gain = 1;
				low = 1;
				high = 1;
				volume = 0;
			} else {
				open = (double)data[2];
				close = (double)data[3];
				gain = close / open;
				
				low = (double) data[4] / close;
				high = (double) data[5] / close;
				
				// Implement dynamically. Set to five minutes for now.
//				volume = (double) data[6] / 
//						(Double.parseDouble(parent.dataHandler.getVolume24h().get(cur1 + "/" + cur2)) / 24.0 / 12.0); 
				
				volume = (double) data[6];
			}

			parent.dbHandler.insertDataPoint(cur1, cur2, currentStamp, currentStop, gain, low, high, volume, intervals);
			
			currentStamp += intervals;
			currentStop += intervals;
		} while (currentStop <= stop);
	}
}
