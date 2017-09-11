package Mission;

import java.util.Date;

import Config.Configuration;
import Data.Wall;
import Data.WallInterval;
import Orders.Order;
import Output.Logger;

public class Mission {
	
	String cur1;
	String cur2;
	String type;
	double price;
	double price2;
	int stage;
	long orderId;
	long timestamp;
	double amount;
	double amountOrig;
	
	boolean deleteFlag = false;
	
	MissionHandler parent;
	
	
	public Mission(String cur1, String cur2, String type, int stage, double amount, double origPrice, double price2, MissionHandler parent) {
		this.cur1 = cur1;
		this.cur2 = cur2;
		this.type = type;
		this.stage = stage;
		this.price = origPrice;
		this.price2 = price2;
		this.parent = parent;
		this.amount = amount;
		
		startMission(type, stage);

//		new Thread(){
//			public void run(){
//				
//				try {
//					Thread.sleep(30000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				parent.getMissions().clear();
//			}
//		}.start();
		
	}
	
	private void startStage1Wall(Date d) {
		amount = Configuration.BASE_INVESTING_AMOUNT / this.price;
		if(amount < parent.parent.dataHandler.getMinTickSize(cur1, cur2)) {
			parent.endMission(cur1, cur2, "WALL", stage);
			return;
		}
		
		this.orderId = parent.parent.restHandler_cex.placeOrder(cur1, cur2, "BUY", amount, this.price);
		if(orderId < 0) {
			parent.endMission(cur1, cur2, "WALL", stage);
			return;
		}
		
		this.timestamp = new Date().getTime();
		
		parent.parent.logger.logTrade(d.toGMTString() + ":" + cur1 + "/" + cur2 + ": " + this.type +
				" mission trying to buy " + amount + " at " + this.price + ". " + orderId);
	}
	
	private void startStage2Wall(Date d) {
		System.out.println(getStage());
		
		this.orderId = parent.parent.restHandler_cex.placeOrder(cur1, cur2, "SELL", amount, this.price);
		
		if(orderId < 0) {
			parent.endMission(cur1, cur2, "WALL", stage);
			return;
		}
		
		this.timestamp = new Date().getTime();
		
		parent.parent.logger.logTrade(d.toGMTString() + ":" + cur1 + "/" + cur2 + ": " + this.type +
				" mission trying to sell " + amount + " at " + this.price + ". " + orderId);
	}
	
	private void startMission(String type, int stage) {
		
		Date d = new Date();
		if(type.equals("WALL") && stage == 1)
			startStage1Wall(d);
		
		else if (type.equals("WALL") && stage == 2)
			startStage2Wall(d);
	}
	
	public void assessMission() {
		if(parent.parent.verbose) System.out.println("Assess missions. No of missions: " + parent.getMissions().size());
		
		if(getType().equals("WALL")) {
			// Wall mission
			
			if(getStage() == 1) {
				// Stage 1 wall mission - buy order already out
				assessStage1Wall();
			}
			
			if(getStage() == 2) {
				// Stage 2 wall mission - buy order executed
				assessStage2Wall();
			}
		}
	}
	
	private void assessStage1Wall() {

		double newBidPrice = parent.considerWallsReturnBidPrice(cur1, cur2);
		double amountRemaining = parent.parent.restHandler_cex.getOrderRemaning(orderId);

		double amountTraded = amount - amountRemaining;
		amountTraded = (double) Math.round(amountTraded * 10000d) / 10000d;
		newBidPrice = (double) Math.round(newBidPrice * 10000d) / 10000d;
		
		if(newBidPrice < 0) {
			
			if(amountTraded > 0) {
				double ceilingPrice = parent.considerWallsReturnAskPrice(cur1, cur2);
				parent.queueMission(new Mission(cur1, cur2, "WALL", 2, amountTraded, ceilingPrice, newBidPrice, parent));
			} 
			
			parent.parent.restHandler_cex.cancelOrder(orderId);
			parent.endMission(cur1, cur2, type, 1);
			return;
			
		} else {
			
			if(amountTraded > 0) {
				amount = amountRemaining;
				double ceilingPrice = parent.considerWallsReturnAskPrice(cur1, cur2);
				parent.queueMission(new Mission(cur1, cur2, "WALL", 2, amountTraded, ceilingPrice, newBidPrice, parent));
			} 
			
			if(newBidPrice != this.price) {
				System.out.println("Replace from assessstage1wall");
				this.orderId = parent.parent.restHandler_cex.replaceOrder(cur1, cur2, "BUY", amount, newBidPrice, orderId);
				
				if(orderId < 0) {
					parent.endMission(cur1, cur2, type, 1);
					return;
				}
				
				System.out.println("Readjusting price from " + this.price + " to " + newBidPrice);
				this.price = newBidPrice;
			}
		}
		
		if (amountRemaining == 0) {
			parent.endMission(cur1, cur2, type, 1);
			return;
		}
		
	}
	
	private void assessStage2Wall() {
		double newSellPrice = parent.considerWallsReturnAskPrice(cur1, cur2);
		double amountRemaining = parent.parent.restHandler_cex.getOrderRemaning(orderId);

		double amountTraded = amount - amountRemaining;
		amountTraded = (double) Math.round(amountTraded * 10000d) / 10000d;
		newSellPrice = (double) Math.round(newSellPrice * 10000d) / 10000d;
		
		if(newSellPrice == -1) { // verify
			System.out.println("replace from assessstage2wall");
			parent.parent.restHandler_cex.replaceOrder(cur1, cur2, "SELL", amountRemaining, price2 * 0.8, orderId); // Drop it like it's hot
			parent.endMission(cur1, cur2, type, 2);
			return;
			
		} else {
			
			if(amountTraded > 0) {
				amount = amountRemaining;
			} 
			
			if(newSellPrice != this.price) {
				this.orderId = parent.parent.restHandler_cex.replaceOrder(cur1, cur2, "SELL", amount, newSellPrice, orderId);
				System.out.println("Readjusting price from " + this.price + " to " + newSellPrice);
				this.price = newSellPrice;
			}
		}
		
		if (amountRemaining == 0) {
			parent.endMission(cur1, cur2, type, 2);
			return;
		}
	}
	
	public String getCur1() {
		return this.cur1;
	}
	
	public void setCur1(String cur1) {
		this.cur1 = cur1;
	}
	
	public String getCur2() {
		return this.cur2;
	}
	
	public void setCur2(String cur2) {
		this.cur2 = cur2;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public int getStage() {
		return this.stage;
	}
	
	public void setStage(int stage) {
		this.stage = stage;
	}
	
	public double getPrice() {
		return this.price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	
	public boolean getDeleteFlag() {
		return this.deleteFlag;
	}
}
