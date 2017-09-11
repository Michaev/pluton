package Data;

public class WallInterval {
	
	String type;
	String cur1;
	String cur2;
	
	double startPrice;
	double endPrice;
	double highestPrice;
	double firstSignificant;
	
	public WallInterval(String type, String cur1, String cur2, double startPrice, double endPrice, double highestPrice, double firstSignificant) {
		this.type = type;
		this.cur1 = cur1;
		this.cur2 = cur2;
		this.startPrice = startPrice;
		this.endPrice = endPrice;
		this.highestPrice = highestPrice;
		this.firstSignificant = firstSignificant;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public double getStartPrice() {
		return startPrice;
	}

	public void setStartPrice(double startPrice) {
		this.startPrice = startPrice;
	}

	public double getEndPrice() {
		return endPrice;
	}

	public void setEndPrice(double endPrice) {
		this.endPrice = endPrice;
	}

	public double getHighestPrice() {
		return highestPrice;
	}

	public void setHighestPrice(double highestPrice) {
		this.highestPrice = highestPrice;
	}

	public double getfirstSignificant() {
		return firstSignificant;
	}

	public void setfirstSignificant(double firstSignificant) {
		this.firstSignificant = firstSignificant;
	}
	
	
}
