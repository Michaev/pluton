package Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import Engine.Pluton;
import Orders.Order;

public class DataHandler {

	Pluton parent;
	public List<Order> orders;
	public List<Wall> walls;
	public List<WallInterval> wallIntervals;
	public List<TickSize> tickSizes;
	
	public List<Funds> funds;
	
	public Map<String, String> volume24h;
	public Map<String, String> minTicks;
	public Map<String, String> states;
	public Map<String, String> peakPrices;
	public Map<String, String> buyTimestamps;
	public Map<String, String> buyPrices;
	public Map<String, String> sellPrices;
	public Map<String, String> activeOrders;
	
	public List<Datapoint> datapoints;
	
	private boolean verbose;
	
	public DataHandler(boolean verbose, Pluton parent) {
		this.verbose = verbose;
		this.parent = parent;
		
		this.volume24h = new HashMap<String, String>();
		this.datapoints = new ArrayList<Datapoint>();
		this.states = new HashMap<String, String>();
		this.peakPrices = new HashMap<String, String>();
		this.buyTimestamps = new HashMap<String, String>();
		this.buyPrices = new HashMap<String, String>();
		this.sellPrices = new HashMap<String, String>();
		this.activeOrders = new HashMap<String, String>();

		this.minTicks = new HashMap<String, String>();
		this.minTicks.put("BCHUSD", "" + 0.01);
		this.minTicks.put("IOTUSD", "" + 0.00001);
		
		orders = new ArrayList<Order>();
		
		for(String currency: parent.currencies) {
			orders.add(new Order(currency.split("/")[1], currency.split("/")[2], this, verbose));
			states.put(currency.split("/")[1] + currency.split("/")[2], "" + 0);
		}
		
		walls = new ArrayList<Wall>();
		wallIntervals = new ArrayList<WallInterval>();
		
		funds = new ArrayList<Funds>();
	}
	
	public Datapoint getPreviousDataPoint(String cur1, String cur2) {
		Datapoint latest = null;
		for(Datapoint dp: this.datapoints) {
			if(cur1.equals(dp.cur1) && cur2.equals(dp.cur2)) {
				latest = dp;
			}
		}
		
		return latest;
	}
	
	public void loadTickSizes() {
		setTickSizes(parent.restHandler_cex.getTickSizes());
	}
	
	public void loadFunds() {
		
		funds.clear();
		
		JSONArray jFunds = parent.restHandler_btf.getFunds();
		
		for(int i = 0; i < jFunds.length(); i++) {
			
			JSONObject jFund = jFunds.getJSONObject(i);
			String currency = jFund.getString("currency");
			double amount = jFund.getDouble("amount");
			double available = jFund.getDouble("available");
			String type = jFund.getString("type");
			
			if(type.equals("exchange"))
				funds.add(new Funds(currency, available, amount));
		}
	}
	
	public List<Funds> getFunds() {
		return this.funds;
	}
	
	public Funds getFunds(String currency) {
		for(Funds funds: getFunds()) {
			if(funds.currency.equals(currency))
				return funds;
		}
		
		return null;
	}
	
	public void setOrders(String cur1, String cur2, String order) {
		
		for(Order orderIterator: orders) {
			if(cur1.equals(orderIterator.cur1) && cur2.equals(orderIterator.cur2)) {
				orderIterator.setOrder(order);
			}
		}
	}
	
	public double getBuyPrice(String cur1, String cur2) {
		Order o = getOrder(cur1, cur2);
		return o.getBuyEntries().get(0).price;
	}
	
	public double getSellPrice(String cur1, String cur2) {
		Order o = getOrder(cur1, cur2);
		return o.getSellEntries().get(0).price;
	}
	
	public Order getOrder(String cur1, String cur2) {
		for(Order orderIterator: orders) {
			if(cur1.equals(orderIterator.cur1) && cur2.equals(orderIterator.cur2)) {
				return orderIterator;
			}
		}
		
		return null;
	}
	
	public void load24HVolume(List<String> currencies) {
		for(String currency: currencies) {
			String cur1 = currency.split("/")[1];
			String cur2 = currency.split("/")[2];
			
			String vol = parent.restHandler_btf.get24HVolume(cur1, cur2).toString();
			double lastPrice = Double.parseDouble(vol.split(",")[6]);
			double volumeSelf = Double.parseDouble(vol.split(",")[7]);
			double volume = lastPrice * volumeSelf;
			
			//this.volume24h.put(cur1 + "/" + cur2, "" + parent.dbHandler.get24HVolume(cur1, cur2));
			this.volume24h.put(cur1 + cur2, "" + volume);
			
			
		}
	}
	
	public List<Order> getOrders() {
		return orders;
	}
	
	public void addCeiling(double ceilingPrice, double ceilingTotal, String cur1, String cur2) {
		walls.add(new Wall("C", cur1, cur2, ceilingPrice, ceilingTotal));
	}
	
	public void addFloor(double floorPrice, double floorTotal, String cur1, String cur2) {
		walls.add(new Wall("F", cur1, cur2, floorPrice, floorTotal));
	}
	
	public void addCeilingInterval(double startPrice, double endPrice, double lowest, double firstNotIgnored, String cur1, String cur2)  {
		wallIntervals.add(new WallInterval("C", cur1, cur2, startPrice, endPrice, lowest, firstNotIgnored));
	}
	
	public void addFloorInterval(double startPrice, double endPrice, double highest, double firstNotIgnored, String cur1, String cur2)  {
		wallIntervals.add(new WallInterval("F", cur1, cur2, startPrice, endPrice, highest, firstNotIgnored));
	}
	
	public List<Wall> getWalls() {
		return this.walls;
	}
	
	public List<WallInterval> getWallIntervals() {
		return this.wallIntervals;
	}
	
	public Wall getWall(String cur1, String cur2, String type) {
		for(Wall wall: getWalls()) {
			if(wall.cur1.equals(cur1) && wall.cur2.equals(cur2) && wall.type.equals(type))
				return wall;
		}
		
		return null;
	}
	
	public WallInterval getWallInterval(String cur1, String cur2, String type) {
		for(WallInterval wall: getWallIntervals()) {
			if(wall.cur1.equals(cur1) && wall.cur2.equals(cur2) && wall.type.equals(type))
				return wall;
		}
		
		return null;
	}
	
	public List<Wall> getWalls(String cur1, String cur2) {
		List<Wall> tempWalls = new ArrayList<Wall>();
		
		for(Wall wall: getWalls()) {
			if(wall.cur1.equals(cur1) && wall.cur2.equals(cur2))
				tempWalls.add(wall);
		}
		
		return tempWalls;
	}
	
	public List<WallInterval> getWallIntervals(String cur1, String cur2) {
		List<WallInterval> tempWalls = new ArrayList<WallInterval>();
		
		for(WallInterval wall: getWallIntervals()) {
			if(wall.cur1.equals(cur1) && wall.cur2.equals(cur2))
				tempWalls.add(wall);
		}
		
		return tempWalls;
	}
	
	public void clearWalls(String cur1, String cur2) {
		List<Wall> tempWalls = new ArrayList<Wall>();
		
		for(Wall wall: getWalls()) {
			if(!wall.cur1.equals(cur1) || !wall.cur2.equals(cur2))
				tempWalls.add(wall);
		}
		
		this.walls = tempWalls;
		
		List<WallInterval> tempWallIntervals = new ArrayList<WallInterval>();
		
		for(WallInterval wall: getWallIntervals()) {
			if(!wall.cur1.equals(cur1) || !wall.cur2.equals(cur2))
				tempWallIntervals.add(wall);
		}
		
		this.wallIntervals = tempWallIntervals;
	}
	
	public void setTickSizes(List<TickSize> tickSizes) {
		this.tickSizes = tickSizes;
	}
	
	public void setFunds(List<Funds> funds) {
		this.funds = funds;
	}
	
	public TickSize getTickSize(String cur1, String cur2) {
		
		if(getTickSizes() == null) 
			return null;
		
		for(TickSize tickSize: getTickSizes()) {
			if(tickSize.getCur1().equals(cur1) && tickSize.getCur2().equals(cur2))
				return tickSize;
		}
		
		return null;
	}
	
	public double getMinTickSize(String cur1, String cur2) {
		return getTickSize(cur1, cur2).tickSize;
	}
	
	public double getMinSymbol1(String cur1, String cur2) {
		return getTickSize(cur1, cur2).minSymbol1;
	}
	
	public double getMinSymbol2(String cur1, String cur2) {
		return getTickSize(cur1, cur2).minSymbol2;
	}
	
	public List<TickSize> getTickSizes() {
		return this.tickSizes;
	}

	public Map<String, String> getVolume24h() {
		return volume24h;
	}

	public void setVolume24h(Map<String, String> volume24h) {
		this.volume24h = volume24h;
	}
	
	
}
