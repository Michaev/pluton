package Orders;
import java.util.Date;

public class Order {

	public String cur1;
	public String cur2;

	private boolean verbose;
	
	private Offer[] sellEntries;
	private Offer[] buyEntries;
	
	public Date updated;
	
	public Order(String cur1, String cur2, boolean verbose) {
		this.cur1 = cur1;
		this.cur2 = cur2;
		this.verbose = verbose;
		
		sellEntries = new Offer[0];
		buyEntries = new Offer[0];
	}
	
	public void setOrder(String order) {
		if(verbose) System.out.println("Set order for " + cur1 + "/" + cur2 + ": " + order);
		
		updated = new Date();
		
		String asks = order.split("asks\":\\[")[1];
		asks = asks.split("\\]\\],\"sell_total")[0];
		asks = asks.substring(1, asks.length());
		
		String buys = order.split("bids\":\\[")[1];
		buys = buys.split("\\]\\],\"buy_total")[0];
		buys = buys.substring(1, buys.length());
		
		String[] sellEntriesString = asks.split("\\],\\[");
		String[] buyEntriesString = buys.split("\\],\\[");
		
		clearAsks();

		for(String sellEntry: sellEntriesString) {
			addAsk(Double.parseDouble(sellEntry.split(",")[0]), Double.parseDouble(sellEntry.split(",")[1]));
		}
		
//		System.out.println("20 biggest sell entries;");
//		for(int  i = 0; i < sellEntries.length && i < 20; i++) {
//			System.out.println(sellEntries[i].getPrice() + ", " + sellEntries[i].getAmount() + ", " + sellEntries[i].getTotal());
//		}
		
		
		clearBids();

		for(String buyEntry: buyEntriesString) {
			addBid(Double.parseDouble(buyEntry.split(",")[0]), Double.parseDouble(buyEntry.split(",")[1]));
		}
		
//		System.out.println("20 biggest buy entries;");
//		for(int  i = 0; i < buyEntries.length && i < 20; i++) {
//			System.out.println(buyEntries[i].getPrice() + ", " + buyEntries[i].getAmount() + ", " + buyEntries[i].getTotal());
//		}
		
	}
	
	private void clearAsks() {
		this.sellEntries = new Offer[0];
	}
	
	private void clearBids() {
		this.buyEntries = new Offer[0];
	}
	
	private void addAsk(double price, double amount) {
		
		Offer[] temp = new Offer[sellEntries.length+1];
		
		for(int i = 0; i < sellEntries.length; i++) {
			temp[i] = sellEntries[i];
		}
		
		temp[sellEntries.length] = new Offer(price, amount);
		sellEntries = temp;
	}
	
	private void addBid(double price, double amount) {
		
		Offer[] temp = new Offer[buyEntries.length+1];
		
		for(int i = 0; i < buyEntries.length; i++) {
			temp[i] = buyEntries[i];
		}
		
		temp[buyEntries.length] = new Offer(price, amount);
		buyEntries = temp;
	}
	
	public Offer[] getSellEntries() {
		return this.sellEntries;
	}
	
	public Offer[] getBuyEntries() {
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
