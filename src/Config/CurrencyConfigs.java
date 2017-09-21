package Config;

public class CurrencyConfigs {

	double jump_limit;
	double jump_limit_vol;
	double stop_loss_limit;
	double roi_goal; 		
	double slippage_limit;
	
	long interval_tick_gen;
	
	public CurrencyConfigs(double[] args, long interval_tick_gen) {
		
		jump_limit = args[0];
		jump_limit_vol = args[1];
		stop_loss_limit = args[2];
		roi_goal = args[3]; 		
		slippage_limit = args[4];
		
		this.interval_tick_gen = interval_tick_gen;
	}
	
	public long getInterval_tick_gen() {
		return interval_tick_gen;
	}

	public void setInterval_tick_gen(long interval_tick_gen) {
		this.interval_tick_gen = interval_tick_gen;
	}

	public double getJump_limit() {
		return jump_limit;
	}

	public void setJump_limit(double jump_limit) {
		this.jump_limit = jump_limit;
	}

	public double getJump_limit_vol() {
		return jump_limit_vol;
	}

	public void setJump_limit_vol(double jump_limit_vol) {
		this.jump_limit_vol = jump_limit_vol;
	}

	public double getStop_loss_limit() {
		return stop_loss_limit;
	}

	public void setStop_loss_limit(double stop_loss_limit) {
		this.stop_loss_limit = stop_loss_limit;
	}

	public double getRoi_goal() {
		return roi_goal;
	}

	public void setRoi_goal(double roi_goal) {
		this.roi_goal = roi_goal;
	}

	public double getSlippage_limit() {
		return slippage_limit;
	}

	public void setSlippage_limit(double slippage_limit) {
		this.slippage_limit = slippage_limit;
	}
	
	
}
