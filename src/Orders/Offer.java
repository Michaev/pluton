package Orders;

public class Offer {

	public double price;
	public double amount;
	
	public double total;
	
	public Offer(double price, double amount) {
		this.price = price;
		this.amount = amount;
		
		this.total = price * amount;
	}
	
	public double getTotal() {
		return this.total;
	}
	
	public double getAmount() {
		return this.amount;
	}
	
	public double getPrice() {
		return this.price;
	}
}
