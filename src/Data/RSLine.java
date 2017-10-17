package Data;

import java.util.List;

public class RSLine {

	List<TopBottom> plots;
	double angle;
	
	public RSLine() {
	
	}
	
	public RSLine(List<TopBottom> plots, double angle) {
		this.plots = plots;
		this.angle = angle;
	}

	public List<TopBottom> getPlots() {
		return plots;
	}

	public void setPlots(List<TopBottom> plots) {
		this.plots = plots;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}
	
	public void add(TopBottom plot) {
		plots.add(plot);
	}
	
	public void removeLast() {
		plots.remove(plots.size() - 1);
	}
	
	public int size() {
		return plots.size();
	}
	
	public String toString() {
		return plots.toString();
	}
}
