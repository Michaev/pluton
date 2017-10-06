package Config;

import Engine.Pluton;

public class ConfigLoader {

	Pluton parent;
	
	public ConfigLoader(Pluton parent) {
		this.parent = parent;
	}
	
	public void fillConfigValues() {
		
		// Included - inertia
		parent.dataHandler.macd_config_limit_buy.put("EOSUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("EOSUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("EOSUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("EOSUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("EOSUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("EOSUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("EOSUSD", 0.975);
		parent.dataHandler.inertia.put("EOSUSD", true);
		
		// Included - inertia
		parent.dataHandler.macd_config_limit_buy.put("XMRUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("XMRUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("XMRUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("XMRUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("XMRUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("XMRUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("XMRUSD", 0.975);
		parent.dataHandler.inertia.put("XMRUSD", true);
		
		// Needs tuning
		parent.dataHandler.macd_config_limit_buy.put("IOTUSD", 5);
		parent.dataHandler.macd_config_limit_sell.put("IOTUSD", 20);
		parent.dataHandler.macd_config_limit_scope.put("IOTUSD", 50);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("IOTUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("IOTUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("IOTUSD", 0.97);
		parent.dataHandler.macd_stop_loss_limit_short.put("IOTUSD", 0.97);
		parent.dataHandler.inertia.put("IOTUSD", false);
		
		// Included - inertia
		parent.dataHandler.macd_config_limit_buy.put("OMGUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("OMGUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("OMGUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("OMGUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("OMGUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("OMGUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("OMGUSD", 0.975);
		parent.dataHandler.inertia.put("OMGUSD", false);
		
		// Needs tuning
		parent.dataHandler.macd_config_limit_buy.put("LTCUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("LTCUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("LTCUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("LTCUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("LTCUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("LTCUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("LTCUSD", 0.975);
		parent.dataHandler.inertia.put("LTCUSD", true);

		// Needs tuning
		parent.dataHandler.macd_config_limit_buy.put("SANUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("SANUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("SANUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("SANUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("SANUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("SANUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("SANUSD", 0.975);
		parent.dataHandler.inertia.put("SANUSD", true);
		
		// Needs tuning
		parent.dataHandler.macd_config_limit_buy.put("DSHUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("DSHUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("DSHUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("DSHUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("DSHUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("DSHUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("DSHUSD", 0.975);
		parent.dataHandler.inertia.put("DSHUSD", true);
		
		// Needs tuning
		parent.dataHandler.macd_config_limit_buy.put("BCHUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("BCHUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("BCHUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("BCHUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("BCHUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("BCHUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("BCHUSD", 0.975);
		parent.dataHandler.inertia.put("BCHUSD", true);
		
		// Needs tuning
		parent.dataHandler.macd_config_limit_buy.put("BTCUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("BTCUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("BTCUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("BTCUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("BTCUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("BTCUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("BTCUSD", 0.975);
		parent.dataHandler.inertia.put("BTCUSD", false);
		
		// Included - inertia
		parent.dataHandler.macd_config_limit_buy.put("NEOUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("NEOUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("NEOUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("NEOUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("NEOUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("NEOUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("NEOUSD", 0.975);
		parent.dataHandler.inertia.put("NEOUSD", true);
		
		// Needs tuning
		parent.dataHandler.macd_config_limit_buy.put("ETPUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("ETPUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("ETPUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("ETPUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("ETPUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("ETPUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("ETPUSD", 0.975);
		parent.dataHandler.inertia.put("ETPUSD", false);
		
		// Included - inertia
		parent.dataHandler.macd_config_limit_buy.put("ZECUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("ZECUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("ZECUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("ZECUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("ZECUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("ZECUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("ZECUSD", 0.975);
		parent.dataHandler.inertia.put("ZECUSD", true);
		
		// Included - inertia
		parent.dataHandler.macd_config_limit_buy.put("ETCUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("ETCUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("ETCUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("ETCUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("ETCUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("ETCUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("ETCUSD", 0.975);
		parent.dataHandler.inertia.put("ETCUSD", true);
		
		// Included - inertia
		parent.dataHandler.macd_config_limit_buy.put("ETHUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("ETHUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("ETHUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("ETHUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("ETHUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("ETHUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("ETHUSD", 0.975);
		parent.dataHandler.inertia.put("ETHUSD", true);

		// Included - inertia
		parent.dataHandler.macd_config_limit_buy.put("XRPUSD", 8);
		parent.dataHandler.macd_config_limit_sell.put("XRPUSD", 7);
		parent.dataHandler.macd_config_limit_scope.put("XRPUSD", 200);
		parent.dataHandler.macd_config_hist_gain_treshold_buy.put("XRPUSD", 3.0);
		parent.dataHandler.macd_config_hist_gain_treshold_sell.put("XRPUSD", 2.0);
		parent.dataHandler.macd_stop_loss_limit.put("XRPUSD", 0.975);
		parent.dataHandler.macd_stop_loss_limit_short.put("XRPUSD", 0.975);
		parent.dataHandler.inertia.put("XRPUSD", true);
		
	}
	
}
