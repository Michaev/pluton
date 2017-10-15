package Data;

import java.util.Date;

// Resistance / support
public class RS {
	Double price;
	long timestamp;
	
	public RS() {
		
	}
	
	public RS(double price, long timestamp) {
		this.price = price;
		this.timestamp = timestamp;
	}
	
	public String toString() {
		Date d = new Date();
		d.setTime(timestamp);
		
		return price + " at " + d.toLocaleString() + "\n";
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
}
