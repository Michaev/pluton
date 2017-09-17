package Scanning;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import Config.Configuration;
import Data.Datapoint;
import Engine.Pluton;
import jdk.nashorn.internal.runtime.regexp.joni.Config;

public class Scanner {

	Pluton parent;
	Thread scanThread;
	int status = 0;
	boolean keepRunning = true;

	public Scanner(Pluton parent) {
		this.parent = parent;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return this.status;
	}

	public void start() {

		scanThread = new Thread() {
			public void run() {

				System.out.println("Scanner thread running");

				// parent.dataHandler.loadTickSizes();
				
				while (keepRunning) {

					// Logic
					Date start = new Date();
					
					parent.dataHandler.loadFunds();

//					for (String currency : parent.currencies) {
//						String currencyData = parent.restHandler_cex.getPublicOrders(currency.split("/")[0],
//								currency.split("/")[1]);
//						parent.dataHandler.setOrders(currency.split("/")[1], currency.split("/")[2], currencyData);
//					}
					
					JSONArray pOrders = parent.restHandler_btf.getPrivateOrders();
					
					for (String currency : parent.currencies) {
						
						String cur1 = currency.split("/")[1];
						String cur2 = currency.split("/")[2];
						
						String orders = parent.restHandler_btf.getPublicOrders(currency.split("/")[1],
								currency.split("/")[2]);
						
						orders = orders.substring(2, orders.length()-2);
						
						parent.dataHandler.setOrders(currency.split("/")[1], currency.split("/")[2], orders);
						
						if(Integer.parseInt(parent.dataHandler.states.get(cur1 + cur2)) == 1) {
							boolean foundOrder = false;
							String oPrice = "";
							String orderId = "";
							
							for(int i = 0; i < pOrders.length(); i++) {
								JSONObject jOrder = pOrders.getJSONObject(i);
								System.out.println(jOrder);
								
								String symbol = jOrder.getString("symbol").toUpperCase();
								orderId = Long.toString(jOrder.getLong("id"));
								oPrice = Double.toString(jOrder.getDouble("price"));
								
								if(symbol.equals(cur1 + cur2) && orderId.equals(parent.dataHandler.activeOrders.get(cur1 + cur2)))
									foundOrder = true;
								
							}
							
							if(!foundOrder) {
								parent.dataHandler.states.put(cur1 + cur2, "" + 2);
								parent.dataHandler.activeOrders.remove(cur1 + cur2);
							}
							else {
								double buyPrice = parent.dataHandler.getBuyPrice(cur1, cur2) + Double.parseDouble(parent.dataHandler.minTicks.get(cur1 + cur2));
								double amount = Configuration.BASE_INVESTING_AMOUNT / buyPrice;
								long new_orderId = parent.restHandler_btf.replaceOrder(Long.parseLong(orderId), cur1, cur2, "buy", amount, buyPrice);
								parent.dataHandler.activeOrders.remove(cur1 + cur2);
								parent.dataHandler.activeOrders.put(cur1 + cur2, "" + new_orderId);
								
								continue;
							}
						}
						
						double avgVolume = Double.parseDouble(parent.dataHandler.volume24h.get(cur1 + cur2));
						avgVolume /= (24 * 60 * 60 * 1000);
						avgVolume *= Configuration.INTERVAL_TICK_GEN;
						
						String trades = parent.restHandler_btf.getRecentTrades((start.getTime() - Configuration.SCANNER_INTERVAL), start.getTime(), cur1,
								cur2).toString();
						
						if(trades.length() < 4) {
							Datapoint prevDatapoint = parent.dataHandler.getPreviousDataPoint(cur1, cur2);
							if(prevDatapoint != null) {
								prevDatapoint.setStart(start.getTime());
								prevDatapoint.setStop(start.getTime() + Configuration.SCANNER_INTERVAL);
								parent.dataHandler.datapoints.add(prevDatapoint);
							}
							continue;
						}
						
						trades = trades.substring(2, trades.length()-2);
						String[] trade =  trades.split("\\],\\[");
						
						double gain = 1;
						double close = -1;
						double open = -1;
						double volume = 0;
						long dpStart = start.getTime();
						long dpStop = dpStart + Configuration.SCANNER_INTERVAL;
						
						for(int i = 0; i < trade.length; i++) {
							String t = trade[i];
							double amount = Math.abs(Double.parseDouble(t.split(",")[2]));
							double price = Double.parseDouble(t.split(",")[3]);
							volume += amount * price;
							
							if(i == trade.length -1)
								open = price;
							
							if(i == 0)
								close = price;
						}

						if(parent.dataHandler.getPreviousDataPoint(cur1, cur2) == null || parent.dataHandler.getPreviousDataPoint(cur1, cur2).getVolume() == 0) { 
							gain = close / open;
						} else {
							gain = close / parent.dataHandler.getPreviousDataPoint(cur1, cur2).getClose();
						}
						
						parent.dataHandler.datapoints.add(new Datapoint(cur1, cur2, open, close, dpStart, dpStop, gain, volume));
						
						double volumeRatio = volume / avgVolume;
						
						if(gain > Configuration.JUMP_LIMIT && volumeRatio > Configuration.JUMP_LIMIT_VOL) {
							System.out.println("Found jump above limit: " + gain + " (" + volumeRatio + ") at " + parent.timestampToDate(start.getTime()));

							if(Integer.parseInt(parent.dataHandler.states.get(cur1 + cur2)) == 0) {
							
								double buyPrice = parent.dataHandler.getBuyPrice(cur1, cur2) + Double.parseDouble(parent.dataHandler.minTicks.get(cur1 + cur2));
								parent.dataHandler.buyPrices.put(cur1 + cur2, "" + buyPrice);
								parent.dataHandler.peakPrices.put(cur1 + cur2, "" + buyPrice);
								parent.dataHandler.buyTimestamps.put(cur1 + cur2, "" + (new Date().getTime()));
								parent.logger.logTrade("Trying to buy " + cur1 + "/" + cur2 + " at " + parent.timestampToDate(start.getTime() + Configuration.INTERVAL_TICK_GEN) + " for " + buyPrice + " (gain and volume higher than treshold, " + gain + ", " + volumeRatio);
								
								double amount = Configuration.BASE_INVESTING_AMOUNT / buyPrice;
								long orderId = parent.restHandler_btf.placeOrder(cur1, cur2, "buy", amount, buyPrice);
								
								parent.dataHandler.states.put(cur1 + cur2, "" + 1);
								parent.dataHandler.activeOrders.put(cur1 + cur2, "" + orderId);
								continue;
							}
						}
						
						if(Integer.parseInt(parent.dataHandler.states.get(cur1 + cur2)) == 2) {
							
							double peakPrice = Double.parseDouble(parent.dataHandler.peakPrices.get(cur1 + cur2));
							double buyPrice = Double.parseDouble(parent.dataHandler.buyPrices.get(cur1 + cur2));
							
							// TODO: continue
							
							double currentPrice = parent.dbHandler.getCurrentPrice(cur1, cur2, start.getTime(), -1);
							
							if(currentPrice > peakPrice)
								parent.dataHandler.peakPrices.put(cur1 + cur2, "" + currentPrice);
							
							double lossFromPeak = currentPrice / peakPrice;
							double gainFromStart = currentPrice / buyPrice;
							
							if(lossFromPeak <= Configuration.STOP_LOSS_LIMIT && start.getTime() > Long.parseLong(parent.dataHandler.buyTimestamps.get(cur1 + cur2))) {
								parent.dataHandler.states.put(cur1 + cur2, "" + 3);
								parent.logger.logTrade(cur1 + "/" + cur2 + ": Price dropped " + lossFromPeak + " at " + parent.timestampToDate(start.getTime()) + ". Selling out. (price dropped below treshold)");
							}
							
							if(gainFromStart >= Configuration.ROI_GOAL && start.getTime() > Long.parseLong(parent.dataHandler.buyTimestamps.get(cur1 + cur2))) {
								parent.dataHandler.states.put(cur1 + cur2, "" + 3);
								parent.logger.logTrade(cur1 + "/" + cur2 + ": Price increased " + lossFromPeak + " at " + parent.timestampToDate(start.getTime()) + ". Selling out. (price hit ROI goal)");
							}
						}
						
						if(Integer.parseInt(parent.dataHandler.states.get(cur1 + cur2)) == 3) {
							double price = parent.dataHandler.getSellPrice(cur1, cur2);
							parent.logger.logTrade("Selling " + cur1 + "/" + cur2 + " at " + parent.timestampToDate(start.getTime()) + " for " + price);
							
							double tradeGain = (price / Double.parseDouble(parent.dataHandler.buyPrices.get(cur1 + cur2))) - 0.003;
							parent.logger.logTrade("Total gain: " + tradeGain);
							
							parent.funds.setAmountAvailable(parent.funds.getAmountAvailable() + ((1000 * tradeGain) - 1000));
							parent.logger.logTrade("New funds: " + parent.funds.getAmountAvailable());
							parent.dataHandler.states.put(cur1 + cur2, "" + 0);
						}
					}

					//
					// // BTC/USD
					// parent.auth.upNonce();
					// String btc =
					// parent.restHandler.postData("https://cex.io/api/order_book/BTC/USD/",
					// new String[] { parent.auth.getApi(),
					// parent.auth.getSignature(), "" + parent.auth.getNonce()
					// });
					// parent.dataHandler.setOrders("BTC", "USD", btc);
					//
					// //System.out.println(btc);
					//
					// // BCH/USD
					// parent.auth.upNonce();
					// String bch =
					// parent.restHandler.postData("https://cex.io/api/order_book/BCH/USD/",
					// new String[] { parent.auth.getApi(),
					// parent.auth.getSignature(), "" + parent.auth.getNonce()
					// });
					// parent.dataHandler.setOrders("BCH", "USD", bch);
					//
					// //System.out.println(bch);
					//
					// // ETH/USD
					// parent.auth.upNonce();
					// String eth =
					// parent.restHandler.postData("https://cex.io/api/order_book/ETH/USD/",
					// new String[] { parent.auth.getApi(),
					// parent.auth.getSignature(), "" + parent.auth.getNonce()
					// });
					// parent.dataHandler.setOrders("ETH", "USD", eth);

					// System.out.println(eth);

					Date stop = new Date();
					
					try {
						if(Configuration.SCANNER_INTERVAL - (stop.getTime() - start.getTime()) > 0) // Mainly for debugging
							Thread.sleep(Configuration.SCANNER_INTERVAL - (stop.getTime() - start.getTime()));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					System.out.println("--------------");
				}

				System.out.println("Scanner thread quitting");
			}
		};
		scanThread.start();
	}
}
