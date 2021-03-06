package Scanning;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import Config.Configuration;
import Data.Datapoint;
import Data.Funds;
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

				JSONArray pOrders = parent.restHandler_btf.getPrivateOrders();
				parent.dataHandler.loadFunds();

				boolean ownsCurrency = false;
				boolean activeSellOrder = false;
				boolean activeBuyOrder = false;
				
				// Start init
				
				for (String currency : parent.currencies) {
					
					parent.dataHandler.loadFunds();
					
					String cur1 = currency.split("/")[1];
					String cur2 = currency.split("/")[2];
					
					ownsCurrency = false;
					activeSellOrder = false;
					activeBuyOrder = false;
					
					for(Funds f: parent.dataHandler.funds) {
						if(f.getCurrency().equals(cur1) && f.getAmount() >= 0.1)
							ownsCurrency = true;
					}
					
					String orderIdS = "";
					for(int i = 0; i < pOrders.length(); i++) {
						JSONObject jOrder = pOrders.getJSONObject(i);
						System.out.println(jOrder);
						
						String symbol = jOrder.getString("symbol").toUpperCase();
						orderIdS = Long.toString(jOrder.getLong("id"));
						String oPrice = Double.toString(jOrder.getDouble("price"));
						String type = jOrder.getString("side");
						
						if(symbol.equals(cur1 + cur2)) {
							parent.dataHandler.activeOrders.put(cur1 + cur2, orderIdS);
							
							if(type.equals("sell")) {
								activeSellOrder = true;
							}
							
							if(type.equals("buy")) {
								activeBuyOrder = true;
							}
						}
					}
					
					if(!ownsCurrency && activeBuyOrder) {
						parent.restHandler_btf.cancelOrder(Long.parseLong(parent.dataHandler.activeOrders.get(cur1 + cur2)));
						parent.dataHandler.states.put(cur1 + cur2, "" + 0);
					}
				
					if(ownsCurrency) {
						if(activeBuyOrder || activeSellOrder)
							parent.restHandler_btf.cancelOrder(Long.parseLong(parent.dataHandler.activeOrders.get(cur1 + cur2)));
						
						String orders = parent.restHandler_btf.getPublicOrders(currency.split("/")[1],
								currency.split("/")[2]);
						
						orders = orders.substring(2, orders.length()-2);
						
						parent.dataHandler.setOrders(currency.split("/")[1], currency.split("/")[2], orders);
						
						parent.dataHandler.states.put(cur1 + cur2, "" + 2);
						parent.dataHandler.peakPrices.put(cur1 + cur2, "" + parent.dataHandler.getBuyPrice(cur1, cur2));
						parent.dataHandler.buyPrices.put(cur1 + cur2, "" + parent.dataHandler.getBuyPrice(cur1, cur2));
					}
				}
				
				// End init

				long sequence = 0;
				while (keepRunning) {

					// Logic
					Date start = new Date();
					
					
					parent.dataHandler.loadFunds();

//					for (String currency : parent.currencies) {
//						String currencyData = parent.restHandler_cex.getPublicOrders(currency.split("/")[0],
//								currency.split("/")[1]);
//						parent.dataHandler.setOrders(currency.split("/")[1], currency.split("/")[2], currencyData);
//					}
					
					for (String currency : parent.currencies) {
						
						String cur1 = currency.split("/")[1];
						String cur2 = currency.split("/")[2];

						if((sequence * Configuration.SCANNER_INTERVAL) % Configuration.currencyConfig.get(cur1 + cur2).getInterval_tick_gen() != 0)
							continue;
						
						// parent.logger.logDebug("Running scanner on " + cur1 + "/" + cur2 + " at sequence " + sequence);
						
						String orders = parent.restHandler_btf.getPublicOrders(currency.split("/")[1],
								currency.split("/")[2]);
						
						orders = orders.substring(2, orders.length()-2);
						
						parent.dataHandler.setOrders(currency.split("/")[1], currency.split("/")[2], orders);
						
						if(Integer.parseInt(parent.dataHandler.states.get(cur1 + cur2)) == 1) {
						
							pOrders = parent.restHandler_btf.getPrivateOrders();
							
							String orderIdS = "";
							activeBuyOrder = false;
							activeSellOrder = false;
							for(int i = 0; i < pOrders.length(); i++) {
								JSONObject jOrder = pOrders.getJSONObject(i);
								System.out.println(jOrder);
								
								String symbol = jOrder.getString("symbol").toUpperCase();
								orderIdS = Long.toString(jOrder.getLong("id"));
								String type = jOrder.getString("side");
								
								if(symbol.equals(cur1 + cur2)) {
									parent.dataHandler.activeOrders.put(cur1 + cur2, orderIdS);
									
									if(type.equals("sell")) {
										activeSellOrder = true;
									}
									
									if(type.equals("buy")) {
										activeBuyOrder = true;
									}
								}
							}
							
							if(!activeBuyOrder) {
								if(parent.dataHandler.getFunds(cur1) != null && parent.dataHandler.getFunds(cur1).getAmountAvailable() > 0.01)
									parent.dataHandler.states.put(cur1 + cur2, "" + 2);
								else
									parent.dataHandler.states.put(cur1 + cur2, "" + 0);
								
								parent.dataHandler.activeOrders.remove(cur1 + cur2);
							}
							else {
								double buyPrice = parent.dataHandler.getBuyPrice(cur1, cur2) + Double.parseDouble(parent.dataHandler.minTicks.get(cur1 + cur2));
								double amount = Configuration.BASE_INVESTING_AMOUNT / buyPrice;
								
								if(buyPrice / Double.parseDouble(parent.dataHandler.buyPrices.get(cur1 + cur2)) >= Configuration.currencyConfig.get(cur1 + cur2).getSlippage_limit()) {
									// Price grew out of range, cancelling
									
									parent.dataHandler.activeOrders.remove(cur1 + cur2);
									parent.restHandler_btf.cancelOrder(Long.parseLong(orderIdS));
									parent.dataHandler.states.put(cur1 + cur2, "" + 0);
									
									continue;
								}
								
								long new_orderId = parent.restHandler_btf.replaceOrder(Long.parseLong(orderIdS), cur1, cur2, "buy", amount, buyPrice);
								//parent.dataHandler.activeOrders.remove(cur1 + cur2);
								parent.dataHandler.activeOrders.put(cur1 + cur2, "" + new_orderId);
								
								continue;
							}
						}
						
						double avgVolume = Double.parseDouble(parent.dataHandler.volume24h.get(cur1 + cur2));
						avgVolume /= (24 * 60 * 60 * 1000);
						avgVolume *= Configuration.currencyConfig.get(cur1 + cur2).getInterval_tick_gen();
						
						String trades = parent.restHandler_btf.getRecentTrades((start.getTime() - Configuration.currencyConfig.get(cur1 + cur2).getInterval_tick_gen()), start.getTime(), cur1,
								cur2).toString();
						
						if(trades.length() < 4) {
							Datapoint prevDatapoint = parent.dataHandler.getPreviousDataPoint(cur1, cur2);
							if(prevDatapoint != null) {
								prevDatapoint.setStart(start.getTime());
								prevDatapoint.setStop(start.getTime() + Configuration.currencyConfig.get(cur1 + cur2).getInterval_tick_gen());
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
						long dpStop = dpStart + Configuration.currencyConfig.get(cur1 + cur2).getInterval_tick_gen();
						
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
						
						if(gain > Configuration.currencyConfig.get(cur1 + cur2).getJump_limit() && volumeRatio > Configuration.currencyConfig.get(cur1 + cur2).getJump_limit_vol()) {
							System.out.println("Found jump above limit: " + gain + " (" + volumeRatio + ") at " + parent.timestampToDate(start.getTime()));

							if(Integer.parseInt(parent.dataHandler.states.get(cur1 + cur2)) == 0) {
							
								double buyPrice = parent.dataHandler.getBuyPrice(cur1, cur2) + Double.parseDouble(parent.dataHandler.minTicks.get(cur1 + cur2));
								parent.dataHandler.buyPrices.put(cur1 + cur2, "" + buyPrice);
								parent.dataHandler.peakPrices.put(cur1 + cur2, "" + buyPrice);
								parent.dataHandler.buyTimestamps.put(cur1 + cur2, "" + (new Date().getTime()));
								parent.logger.logTrade("Trying to buy " + cur1 + "/" + cur2 + " at " + parent.timestampToDate(start.getTime() + Configuration.currencyConfig.get(cur1 + cur2).getInterval_tick_gen()) + " for " + buyPrice + " (gain and volume higher than treshold, " + gain + ", " + volumeRatio);
								
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
							double currentPrice = parent.dataHandler.getBuyPrice(cur1, cur2);
							
							if(currentPrice > peakPrice)
								parent.dataHandler.peakPrices.put(cur1 + cur2, "" + currentPrice);
							
							double lossFromPeak = currentPrice / peakPrice;
							double gainFromStart = currentPrice / buyPrice;
							boolean sell = false;
							
							if(lossFromPeak <= Configuration.currencyConfig.get(cur1 + cur2).getStop_loss_limit() && start.getTime() > Long.parseLong(parent.dataHandler.buyTimestamps.get(cur1 + cur2))) {
								parent.logger.logTrade(cur1 + "/" + cur2 + ": Price dropped " + lossFromPeak + " at " + parent.timestampToDate(start.getTime()) + ". Selling out. (price dropped below treshold)");
								sell = true;
							}
							
							if(gainFromStart >= Configuration.currencyConfig.get(cur1 + cur2).getRoi_goal() && start.getTime() > Long.parseLong(parent.dataHandler.buyTimestamps.get(cur1 + cur2))) {
								parent.logger.logTrade(cur1 + "/" + cur2 + ": Price increased " + lossFromPeak + " at " + parent.timestampToDate(start.getTime()) + ". Selling out. (price hit ROI goal)");
								sell = true;
							}
							
							if(sell) {
								double sellPrice = parent.dataHandler.getSellPrice(cur1, cur2) - Double.parseDouble(parent.dataHandler.minTicks.get(cur1 + cur2));
								parent.logger.logTrade("Selling " + cur1 + "/" + cur2 + " at " + parent.timestampToDate(start.getTime()) + " for " + sellPrice);
								long orderId = parent.restHandler_btf.placeOrder(cur1, cur2, "sell", parent.dataHandler.getFunds(cur1).getAmountAvailable(), sellPrice);
								parent.dataHandler.states.put(cur1 + cur2, "" + 3);
								parent.dataHandler.activeOrders.put(cur1 + cur2, Long.toString(orderId));
								parent.dataHandler.sellPrices.put(cur1 + cur2, Double.toString(sellPrice));
							}
						}
						
						if(Integer.parseInt(parent.dataHandler.states.get(cur1 + cur2)) == 3) {
							
							pOrders = parent.restHandler_btf.getPrivateOrders();
							
							String orderIdS = "";
							activeBuyOrder = false;
							activeSellOrder = false;
							double sellPrice = 0;
							double executionPrice = 0;
							double amount = 0;
							
							for(int i = 0; i < pOrders.length(); i++) {
								JSONObject jOrder = pOrders.getJSONObject(i);
								System.out.println(jOrder);
								
								String symbol = jOrder.getString("symbol").toUpperCase();
								orderIdS = Long.toString(jOrder.getLong("id"));
								String type = jOrder.getString("side");
								
								if(symbol.equals(cur1 + cur2)) {
									parent.dataHandler.activeOrders.put(cur1 + cur2, orderIdS);
									
									if(type.equals("sell")) {
										activeSellOrder = true;
									}
									
									if(type.equals("buy")) {
										activeBuyOrder = true;
									}
								}
							}
							
							if(!activeSellOrder) {
								if(parent.dataHandler.getFunds(cur1) == null || parent.dataHandler.getFunds(cur1).getAmountAvailable() <= 0.01)
									parent.dataHandler.states.put(cur1 + cur2, "" + 0);
								else
									parent.dataHandler.states.put(cur1 + cur2, "" + 2);
								
								executionPrice = parent.restHandler_btf.getExecutionPrice(Long.parseLong(parent.dataHandler.activeOrders.get(cur1 + cur2)));
								parent.dataHandler.activeOrders.remove(cur1 + cur2);
								
								double tradeGain = (executionPrice / Double.parseDouble(parent.dataHandler.buyPrices.get(cur1 + cur2))) - 0.003;
								parent.logger.logTrade("Total gain: " + tradeGain);
								parent.dataHandler.states.put(cur1 + cur2, "" + 0);
							}
							else {
								sellPrice = parent.dataHandler.getSellPrice(cur1, cur2) - Double.parseDouble(parent.dataHandler.minTicks.get(cur1 + cur2));
								amount = parent.dataHandler.getFunds(cur1).getAmount();
								
								if(Double.parseDouble(parent.dataHandler.sellPrices.get(cur1 + cur2)) / sellPrice >= Configuration.currencyConfig.get(cur1 + cur2).getSlippage_limit()) {
									// Price grew out of range, dumping on market
									
									double dumpPrice = parent.dataHandler.getBuyPrice(cur1, cur2);
									long new_orderId = parent.restHandler_btf.replaceOrder(Long.parseLong(orderIdS), cur1, cur2, "sell", amount, dumpPrice);
									executionPrice = parent.restHandler_btf.getExecutionPrice(new_orderId);
									parent.dataHandler.activeOrders.put(cur1 + cur2, Long.toString(new_orderId));
									parent.dataHandler.states.put(cur1 + cur2, "" + 0);
									parent.logger.logTrade("Dumping " + cur1 + "/" + cur2 + " at " + parent.timestampToDate(start.getTime()) + " for " + dumpPrice);
									
									double tradeGain = (executionPrice / Double.parseDouble(parent.dataHandler.buyPrices.get(cur1 + cur2))) - 0.003;
									parent.logger.logTrade("Total gain: " + tradeGain);
									parent.dataHandler.states.put(cur1 + cur2, "" + 0);
									continue;
								}
								
								long new_orderId = parent.restHandler_btf.replaceOrder(Long.parseLong(orderIdS), cur1, cur2, "sell", amount, sellPrice);
								//parent.dataHandler.activeOrders.remove(cur1 + cur2);
								parent.dataHandler.activeOrders.put(cur1 + cur2, "" + new_orderId);
								
								continue;
							}
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
					sequence++;
				}

				System.out.println("Scanner thread quitting");
			}
		};
		scanThread.start();
	}
}
