package Data;

public class Funds {
	
	String currency;
	double amountAvailable;
	double amountOrder;

	public Funds(String currency, double amountAvailable, double amountOrder) {
		this.currency = currency;
		this.amountAvailable = amountAvailable;
		this.amountOrder = amountOrder;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public double getAmountAvailable() {
		return amountAvailable;
	}

	public void setAmountAvailable(double amountAvailable) {
		this.amountAvailable = amountAvailable;
	}

	public double getAmountOrder() {
		return amountOrder;
	}

	public void setAmountOrder(double amountOrder) {
		this.amountOrder = amountOrder;
	}
	
	
}
