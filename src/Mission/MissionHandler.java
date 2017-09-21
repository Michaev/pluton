package Mission;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Config.Configuration;
import Data.Wall;
import Data.WallInterval;
import Engine.Pluton;
import Output.Logger;

public class MissionHandler {
	
	// Will analyze the conceptualized data from the data handler and consider any possible opportunities, as well as manage any active missions.
	
	private boolean verbose;
	Thread missionPlannerThread;
	boolean keepRunning = true;
	private List<Mission> missions;
	private List<Mission> missionQueue;
	Pluton parent;
	
	public MissionHandler(boolean verbose, Pluton parent) {
		this.verbose = verbose;
		this.parent = parent;
		missions = new ArrayList<Mission>();
		missionQueue = new ArrayList<Mission>();
		
	}
	
	private void considerWalls() {
		for(String currency: parent.currencies) {
			
			String cur1 = currency.split("/")[1];
			String cur2 = currency.split("/")[2];
			
			double amountOrders = parent.dataHandler.getFunds(cur1).getAmount() - parent.dataHandler.getFunds(cur1).getAmountAvailable();
			
			if(amountOrders > 0) 
				continue;

			double amountAvailable = parent.dataHandler.getFunds(cur1).getAmountAvailable();
			
			if(amountAvailable > 0)  {
				double sellPrice = considerWallsReturnAskPrice(cur1, cur2);
				double floorPrice = parent.dataHandler.getWallInterval(cur1, cur2, "F").getHighestPrice();
				
				getMissions().add(new Mission(cur1, cur2, "WALL", 2, amountAvailable, sellPrice, floorPrice, this));
				
				parent.dataHandler.getFunds(cur1).setAmountAvailable(0);
				continue;
			}

			// Abort if wall mission already commenced.
			if(getMission(cur1, cur2, "WALL", 1) != null || getMission(cur1, cur2, "WALL", 2) != null)
				continue;
			
			double bidPrice = considerWallsReturnBidPrice(cur1, cur2);
			
			if(bidPrice > 0)
				getMissions().add(new Mission(cur1, cur2, "WALL", 1,  -1, bidPrice, -1, this));
			
			// Testing 
			String openOrders = parent.restHandler_cex.getOpenOrders(cur1, cur2);
			String tradeHistory = parent.restHandler_cex.getOwnTradeHistory(cur1, cur2);
			
		}
	}
	

	public double considerWallsReturnBidPrice(String cur1, String cur2) {
		// Start floor analysis
		
		System.out.println(cur1 + "/" + cur2 + 
				": Considering walls");
		
		Wall floor = parent.dataHandler.getWall(cur1, cur2, "F");
		WallInterval floorInterval = parent.dataHandler.getWallInterval(cur1, cur2, "F");
		
		if(floor == null || floorInterval == null) {
			// walls not initialized.
			System.out.println("(considerWalls(" + cur1 + ", " + cur2 + "): floors not initialized yet");
			return -2;
		}
		
		double floorPrice = floor.getPrice();
		double floorIntervalEndPrice = floorInterval.getEndPrice();
		double floorIntervalSigPrice = floorInterval.getfirstSignificant();
		
		//double floorTarget = (floorIntervalEndPrice + floorIntervalSigPrice) / 2; // Testing if bidding above significant order is better
		double floorTarget = floorIntervalSigPrice;
		double floorGap = ((Math.abs(floorTarget / floorPrice) - 1) * 100);
		
		System.out.println(cur1 + "/" + cur2 + ": Floor target set at " + floorTarget);
		System.out.println(cur1 + "/" + cur2 + ": Distance from floor to target " + floorGap);
		
		// Start ceiling analysis
		
		Wall ceiling = parent.dataHandler.getWall(cur1, cur2, "C");
		WallInterval ceilingInterval = parent.dataHandler.getWallInterval(cur1, cur2, "C");
		
		if(ceiling == null || ceilingInterval == null) {
			// walls not initialized.
			System.out.println("(considerWalls(" + cur1 + ", " + cur2 + "): ceiling not initialized yet");
			return -2;
		}
		
		double ceilingPrice = ceiling.getPrice();
		double ceilingIntervalEndPrice = ceilingInterval.getEndPrice();
		double ceilingIntervalSigPrice = ceilingInterval.getfirstSignificant();
		
		double ceilingTarget = (ceilingIntervalEndPrice + ceilingIntervalSigPrice) / 2;
		double ceilingGap = (Math.abs((ceilingTarget / ceilingPrice) - 1) * 100);
		
		System.out.println(cur1 + "/" + cur2 + ": Ceiling target set at " + ceilingTarget);
		System.out.println(cur1 + "/" + cur2 + ": Distance from ceiling to target " + ceilingGap);
		
		// Comparing distance between floor target and first significant sell order
		System.out.println("Gap between walls: " + ((ceilingIntervalSigPrice / floorTarget) - 1) * 100);
		
		if(((ceilingIntervalSigPrice / floorTarget) - 1) * 100  >= Configuration.WALLS_GAP_LIMIT) {
			
			parent.logger.logDebug(cur1 + "/" + cur2 + ": Found wall gap bigger than limit.");
			parent.logger.logDebug(cur1 + "/" + cur2 + ": Floor gap: " + floorGap + ", ceiling gap: " + ceilingGap);
			
			if(floorGap < Configuration.WALL_TARGET_GAP_LIMIT) {
				parent.logger.logDebug(cur1 + "/" + cur2 + ": Walls looking solid. Initiate mission.");
				
				return floorTarget + parent.dataHandler.getTickSize(cur1, cur2).getTickSize();
			} else {
				parent.logger.logDebug(cur1 + "/" + cur2 + ": Not solid enough ceiling or floor. Aborting.");
			}
		}
		
		return -3;
	}
	
	public double considerWallsReturnAskPrice(String cur1, String cur2) {
		// Start floor analysis
		
		System.out.println(cur1 + "/" + cur2 + 
				": Considering walls");
		
		// Start ceiling analysis
		
		Wall ceiling = parent.dataHandler.getWall(cur1, cur2, "C");
		WallInterval ceilingInterval = parent.dataHandler.getWallInterval(cur1, cur2, "C");
		
		if(ceiling == null || ceilingInterval == null) {
			// walls not initialized.
			System.out.println("(considerWalls(" + cur1 + ", " + cur2 + "): ceiling not initialized yet");
			return -2;
		}
		
		double ceilingPrice = ceiling.getPrice();
		double ceilingIntervalEndPrice = ceilingInterval.getEndPrice();
		double ceilingIntervalSigPrice = ceilingInterval.getfirstSignificant();
		
		// double ceilingTarget = (ceilingIntervalEndPrice + ceilingIntervalSigPrice) / 2; // Testing to see if outselling nearest significant order is better
		double ceilingTarget = ceilingIntervalSigPrice;
		double ceilingGap = (Math.abs((ceilingTarget / ceilingPrice) - 1) * 100);
		
		System.out.println(cur1 + "/" + cur2 + ": Ceiling target set at " + ceilingTarget);
		System.out.println(cur1 + "/" + cur2 + ": Distance from ceiling to target " + ceilingGap);
		
		return ceilingTarget - parent.dataHandler.getMinTickSize(cur1, cur2);
	}
	
	private void considerPriceMovement() {
		
	}
	
	public void queueMission(Mission m) {
		missionQueue.add(m);
	}
	
	
	public void endMission(String cur1, String cur2, String type, int stage) {
		for(Mission m: getMissions()) {
			if(m.getCur1().equals(cur1) && m.getCur2().equals(cur2) && m.getType().equals(type) && m.getStage() == stage) 
				m.setDeleteFlag(true);
		}
	}
	
	private void setMissions(List<Mission> missions) {
		this.missions = missions;
	}
	
	private Mission getMission(String cur1, String cur2, String type, int stage) {
		for(Mission m: getMissions()) {
			if(m.getCur1().equals(cur1) && m.getCur2().equals(cur2) && m.getType().equals(type) && m.getStage() == stage) 
				return m;
		}
		
		return null;
	}
	
	public List<Mission> getMissions() {
		return this.missions;
	}
	
	public List<Mission> getMissionQueue() {
		return this.missionQueue;
	}
	
	private void assessMissions() {
		for(Mission mission: getMissions()) {
			if(!mission.getDeleteFlag())
				mission.assessMission();
		}
		
		
		// Delete flagged missions
		List<Mission> tempMissions = new ArrayList<Mission>();
		for(Mission m: getMissions()) {
			if(!m.getDeleteFlag())
				tempMissions.add(m);
		}
		setMissions(tempMissions);
		
		// Add queued missions
		for(Mission m: getMissionQueue()) {
			getMissions().add(m);
		}
	}
	
	public void start() {
		
		missionPlannerThread = new Thread(){
			public void run(){
				
				System.out.println("Mission planner thread running");
				
				while(keepRunning) {

					try {
						Thread.sleep(Configuration.MISSION_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					// Consider if any new missions will commence
					if(Configuration.ACT_ON_WALLS) considerWalls();
					if(Configuration.ACT_ON_PRICE_HISTORY) considerPriceMovement();
					
					assessMissions();
					
					System.out.println("-----End Mission planner thread cycle---------");
				}
				
				System.out.println("Mission planner thread quitting");
			}
		};
	  missionPlannerThread.start();
	
	
	}
}
