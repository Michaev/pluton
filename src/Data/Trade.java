package Data;

public class Trade {

	long tid;
	long timestamp;
	String cur1;
	String cur2;
	double amount;
	double price;
	
	public Trade(long tid, long timestamp, String cur1, String cur2, double amount, double price) {
		this.tid = tid;
		this.timestamp = timestamp;
		this.cur1 = cur1;
		this.cur2 = cur2;
		this.amount = amount;
		this.price = price;
	}

	public long getTid() {
		return tid;
	}

	public void setTid(long tid) {
		this.tid = tid;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
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

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	
}
