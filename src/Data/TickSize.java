package Data;

public class TickSize {

	String cur1;
	String cur2;
	double minSymbol1;
	double minSymbol2;
	double tickSize;
	
	public TickSize(String cur1, String cur2, double minSymbol1, double minSymbol2, double tickSize) {
		this.cur1 = cur1;
		this.cur2 = cur2;
		this.minSymbol1 = minSymbol1;
		this.minSymbol2 = minSymbol2;
		this.tickSize = tickSize;
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

	public double getMinSymbol1() {
		return minSymbol1;
	}

	public void setMinSymbol1(double minSymbol1) {
		this.minSymbol1 = minSymbol1;
	}

	public double getMinSymbol2() {
		return minSymbol2;
	}

	public void setMinSymbol2(double minSymbol2) {
		this.minSymbol2 = minSymbol2;
	}

	public double getTickSize() {
		return tickSize;
	}

	public void setTickSize(double tickSize) {
		this.tickSize = tickSize;
	}
	
}
