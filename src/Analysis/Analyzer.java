package Analysis;
import java.util.Date;

import Config.Configuration;
import Data.DataHandler;
import Data.PriceHistory;
import Engine.Pluton;
import Orders.Offer;
import Orders.Order;

public class Analyzer {
	
	// Will analyze the raw data and polish into concepts. Will not consider any buy/sell opportunities. 
	
	private boolean verbose;
	Pluton parent;
	Thread analyzerThread;
	boolean keepRunning = true;
	
	int analyzeFloorCeilingInterval;
	int analyzeCurrentPriceInterval;
	
	Date lastFloorCeilingAnalysis;
	Date lastCurrentPriceAnalysis;
	
	PriceHistory priceHistory;
	
	public Analyzer(boolean verbose, Pluton parent) {
		this.verbose = verbose;
		this.parent = parent;
		this.priceHistory = new PriceHistory();

		analyzeFloorCeilingInterval = Configuration.ANALYZE_FLOOR_CEILING_INTERVAL;
		analyzeCurrentPriceInterval = Configuration.ANALYZE_CURRENT_PRICE_INTERVAL;
	}
	
	private String getCurrency(Order order) {
		return order.getCur1() + "/" + order.getCur2();
	}
	
	private double[] getCeilingInterval(Order order, int depth, double size) {
		Offer[] sellEntries = order.getSellEntries();
		
		if(sellEntries.length < 1) {
			return null;
		}
		
		double temp = 0;
		double highestPrice = Double.MAX_VALUE; // Misleading variable name; referring to price of biggest sell order. 
		double highestTotal = 0;
		double firstNotIgnored = 0;
		double startPrice = sellEntries[0].getPrice();
		
		for(Offer sellEntry: sellEntries) {
			if(sellEntry.getTotal() > highestTotal) { highestTotal = sellEntry.getTotal(); highestPrice = sellEntry.getPrice(); }
			if(sellEntry.getTotal() > Configuration.FLOOR_CEILING_SIG_LIMIT && firstNotIgnored == 0) firstNotIgnored = sellEntry.getPrice();

			temp += sellEntry.getTotal();
			if(temp > size) {
				double endPrice = sellEntry.getPrice();
				System.out.println(getCurrency(order) + ": Found ceiling interval between " + startPrice + " and " + endPrice + 
						". TOT: " + temp + ". (" + highestPrice + ", " + highestTotal + "), First significant order: " + firstNotIgnored);
				return new double[] { startPrice, endPrice, highestPrice, firstNotIgnored };
			}
		}
		
		return null;
	}
	
	private double[] getFloorInterval(Order order, int depth, double size) {
		Offer[] buyEntries = order.getBuyEntries();
		
		if(buyEntries.length < 1) {
			return null;
		}
		
		double temp = 0;
		double highestPrice = 0;
		double highestTotal = 0;
		double firstNotIgnored = 0;
		double startPrice = buyEntries[0].getPrice();
		
		for(Offer buyEntry: buyEntries) {
			if(buyEntry.getTotal() > highestTotal) { highestTotal = buyEntry.getTotal(); highestPrice = buyEntry.getPrice(); }
			if(buyEntry.getTotal() > Configuration.FLOOR_CEILING_SIG_LIMIT && firstNotIgnored == 0) firstNotIgnored = buyEntry.getPrice();
			temp += buyEntry.getTotal();
			if(temp > size) {
				double endPrice = buyEntry.getPrice();
				System.out.println(getCurrency(order) + ": Found floor interval between " + startPrice + " and " + endPrice +
						". TOT: " + temp + ". (" + highestPrice + ", " + highestTotal + "), first significant order: " + firstNotIgnored);
				return new double[] { startPrice, endPrice, highestPrice, firstNotIgnored };
			}
		}
		
		return null;
	}
	
	private double[] getCeiling(Order order, int depth, double size) {
		Offer[] sellEntries = order.getSellEntries();
		
		for(Offer sellEntry: sellEntries) {
			if(sellEntry.getTotal() > size) {
				System.out.println(getCurrency(order) + ": Found ceiling selling " + sellEntry.getAmount() + " at " + sellEntry.getPrice() + ", TOT: " + sellEntry.getTotal());
				return new double[] { sellEntry.getPrice(), sellEntry.getTotal() };
			}
		}
		
		return null;
	}
	
	private double[] getFloor(Order order, int depth, double size) {
		Offer[] buyEntries = order.getBuyEntries();
		
		for(Offer buyEntry: buyEntries) {
			if(buyEntry.getTotal() > size) {
				System.out.println(getCurrency(order) + ": Found floor buying " + buyEntry.getAmount() + " at " + buyEntry.getPrice() + ", TOT: " + buyEntry.getTotal());
				return new double[] { buyEntry.getPrice(), buyEntry.getTotal() };
			}
		}
		
		return null;
	}
	
	private void analyzeCeilingFloor(Order order) {
		
		double[] floor = getFloor(order, Configuration.FLOOR_CEILING_DEPTH, Configuration.FLOOR_CEILING_SIZE);
		double[] floorInterval = getFloorInterval(order, Configuration.FLOOR_CEILING_DEPTH, Configuration.FLOOR_CEILING_SIZE);
		
		double[] ceiling = getCeiling(order, Configuration.FLOOR_CEILING_DEPTH, Configuration.FLOOR_CEILING_SIZE);
		double[] ceilingInterval = getCeilingInterval(order, Configuration.FLOOR_CEILING_DEPTH, Configuration.FLOOR_CEILING_SIZE);
		
		parent.dataHandler.clearWalls(order.cur1, order.cur2);
		
		if(floor != null)
			this.parent.dataHandler.addFloor(floor[0], floor[1], order.cur1, order.cur2);
		
		if(floorInterval != null)
			this.parent.dataHandler.addFloorInterval(floorInterval[0], floorInterval[1], floorInterval[2], floorInterval[3], order.cur1, order.cur2);

		if(ceiling != null)
			this.parent.dataHandler.addCeiling(ceiling[0], ceiling[1], order.cur1, order.cur2);
		
		if(ceilingInterval != null)
			this.parent.dataHandler.addCeilingInterval(ceilingInterval[0], ceilingInterval[1], ceilingInterval[2], ceilingInterval[3], order.cur1, order.cur2);

		System.out.println(getCurrency(order) + ": Analyzed ceiling/floor");
		System.out.println("------------------");
	}
	
	private void analyzeCurrentPrice(Order order) {
		
	}
	
	private void analyzeOrder(Order order) {
		if(verbose) System.out.println("Analyzing " + order.cur1 + "/" + order.cur2 + ".");
		
		if(lastFloorCeilingAnalysis == null || new Date().getTime() - lastFloorCeilingAnalysis.getTime() < Configuration.ANALYZE_FLOOR_CEILING_INTERVAL * 1000) {
			analyzeCeilingFloor(order);
		}
		
		if(lastCurrentPriceAnalysis == null || new Date().getTime() - lastCurrentPriceAnalysis.getTime() < Configuration.ANALYZE_CURRENT_PRICE_INTERVAL * 1000) {
			analyzeCurrentPrice(order);
		}
		
		
	}
	
	public void start() {
		
		analyzerThread = new Thread(){
			public void run(){
				
				System.out.println("Analyzer thread running");
				
				while(keepRunning) {

//					Order btcOrder = parent.dataHandler.getOrder("BTC", "USD");
//					Order ethOrder = parent.dataHandler.getOrder("ETH", "USD");
//					Order bchOrder = parent.dataHandler.getOrder("BCH", "USD");
//					
//					analyzeOrder(btcOrder);
//					analyzeOrder(bchOrder);
//					analyzeOrder(ethOrder);
					
					for(Order order: parent.dataHandler.getOrders()) {
						analyzeOrder(order);
					}
					
					lastFloorCeilingAnalysis = new Date();
					
					try {
						Thread.sleep(Configuration.SCANNER_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					System.out.println("-----End analyzer thread cycle---------");
				}
				
				System.out.println("Analyzer thread quitting");
			}
		};
	  analyzerThread.start();
	}
}
