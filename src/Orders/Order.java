package Orders;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Data.DataHandler;

public class Order {
	
	DataHandler parent;

	public String cur1;
	public String cur2;

	private boolean verbose;
	
	private List<Offer> sellEntries;
	private List<Offer> buyEntries;
	
	public Date updated;
	
	public Order(String cur1, String cur2, DataHandler parent, boolean verbose) {
		this.cur1 = cur1;
		this.cur2 = cur2;
		this.parent = parent;
		this.verbose = verbose;
		
		sellEntries = new ArrayList<Offer>();
		buyEntries = new ArrayList<Offer>();
	}
	
	public void setOrder(String order) {
		if(verbose) System.out.println("Set order for " + cur1 + "/" + cur2 + ": " + order);
		
		updated = new Date();
		
		clearAsks();
		clearBids();
		
		for(String o: order.split("\\],\\[")) {
			double price = Double.parseDouble(o.split(",")[0]);
			double count = Double.parseDouble(o.split(",")[1]);
			double amount = Double.parseDouble(o.split(",")[2]);
			
			if(amount > 0 && (parent.buyPrices.get(cur1 + cur2) == null || price != Double.parseDouble(parent.buyPrices.get(cur1 + cur2))) || count > 1) {
				this.buyEntries.add(new Offer(price, amount));
			}
			if(amount < 0 && (parent.sellPrices.get(cur1 + cur2) == null  || price != Double.parseDouble(parent.sellPrices.get(cur1 + cur2))) || count > 1) {
				this.sellEntries.add(new Offer(price, -amount));
			}
		}
		
	}
	
	private void clearAsks() {
		this.sellEntries.clear();
	}
	
	private void clearBids() {
		this.buyEntries.clear();
	}
	
	public List<Offer> getSellEntries() {
		return this.sellEntries;
	}
	
	public List<Offer> getBuyEntries() {
		return this.buyEntries;
	}
	
	public String getCur1() {
		return cur1;
	}

	public void setCur1(String cur1) {
		this.cur1 = cur1;
	}

	public String getCur2() {
		return cur2;
	}

	public void setCur2(String cur2) {
		this.cur2 = cur2;
	}

}
