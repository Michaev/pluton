package Data;

public class Datapoint {

	long start;
	long stop;
	double gain;
	double volume;
	double close;
	double open;
	
	public String cur1;
	public String cur2;
	
	public Datapoint(String cur1, String cur2, double open, double close, long start, long stop, double gain, double volume) {
		this.cur1 = cur1;
		this.cur2 = cur2;
		
		this.close = close;
		this.open = open;
		this.start = start;
		this.stop = stop;
		this.gain = gain;
		this.volume = volume;
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

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getStop() {
		return stop;
	}

	public void setStop(long stop) {
		this.stop = stop;
	}

	public double getGain() {
		return gain;
	}

	public void setGain(double gain) {
		this.gain = gain;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}
	
	
}
