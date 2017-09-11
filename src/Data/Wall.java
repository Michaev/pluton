package Data;

public class Wall {
	
	String type;
	String cur1;
	String cur2;
	double price;
	double total;
	
	public Wall(String type, String cur1, String cur2, double price, double total) {
		this.type = type;
		this.cur1 = cur1;
		this.cur2 = cur2;
		this.price = price;
		this.total = total;
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

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}
	
	
}
