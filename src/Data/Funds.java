package Data;

public class Funds {
	
	String currency;
	double amountAvailable;
	double amount;

	public Funds(String currency, double amountAvailable, double amount) {
		this.currency = currency;
		this.amountAvailable = amountAvailable;
		this.amount = amount;
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

	public double getamount() {
		return amount;
	}

	public void setamount(double amount) {
		this.amount = amount;
	}
	
	
}
