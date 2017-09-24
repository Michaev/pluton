package Engine;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.encog.Encog;

import AI.NNTrainer;
import AI.NNTrainerTimeSeries;
import AI.SunSpotTimeseries;
import Analysis.Analyzer;
import Analysis.History.HistoryLoader;
import Config.Configuration;
import Config.CurrencyConfigs;
import DB.DBHandler;
import Data.DataHandler;
import Data.FileLoader;
import Data.Funds;
import MarketBuildTraining.MarketBuildTraining;
import MarketBuildTraining.MarketEvaluate;
import MarketBuildTraining.MarketPredict;
import MarketBuildTraining.MarketPrune;
import MarketBuildTraining.MarketTrain;
import Mission.MACDAgent;
import Mission.MissionHandler;
import Output.Logger;
import REST.Rest_BTF;
import REST.Rest_CEX;
import Scanning.Scanner;

public class Pluton {

	public boolean verbose = Configuration.VERBOSE;
	public Rest_CEX restHandler_cex;
	public Rest_BTF restHandler_btf;
	private Scanner scanner;
	public DataHandler dataHandler;
	public MissionHandler missionHandler;
	public DBHandler dbHandler;
	public Analyzer analyzer;
	public Logger logger;
	
	public List<String> currencies = new ArrayList<String>();
	
	public Pluton() {
		
		logger = new Logger(Configuration.DEBUG_LOG_FILEPATH, Configuration.TRADE_LOG_FILEPATH);

		for(String currency: Configuration.CURRENCIES) {
			currencies.add(currency);
		}
		
		dbHandler = new DBHandler();
		dataHandler = new DataHandler(verbose, this);
		restHandler_btf = new Rest_BTF(verbose);
		
		if(Configuration.MODE.equals("loadHistory")) {
			HistoryLoader historyLoader = new HistoryLoader(this);
			for(String currency: Configuration.CURRENCIES) {
				historyLoader.load(currency);
			}
			
			logger.logDebug("Finished loading history");
			System.exit(0);
		}
		
		if(Configuration.MODE.equals("generateData")) {

			dataHandler.load24HVolume(Configuration.CURRENCIES);
			
			HistoryLoader historyLoader = new HistoryLoader(this);
			for(String currency: Configuration.CURRENCIES) {
				
				long[] startStop = dbHandler.getStartStop(currency);
				long start = startStop[0];
				long stop = startStop[1];
				
				Date startDate = new Date();
				startDate.setTime(start);
				Date stopDate = new Date();
				stopDate.setTime(stop);
				
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, - Configuration.NUMBER_OF_DAYS_TRAINING);

				// Only train on data that is n number of days old (configurable). If not enough data is available for n number of days, use as much as possible.
				if(stop < cal.getTimeInMillis())
					stop = cal.getTimeInMillis();

				historyLoader.generateTicks(start, stop, Configuration.INTERVAL_TICK_GEN, currency);
			}
			
			logger.logDebug("Finished loading history");
			System.exit(0);
		}
		
		if(Configuration.MODE.equals("simulation")) {
			for(String currency: Configuration.CURRENCIES) {
				//FileLoader fileLoader = new FileLoader(this);
				
				//File file = fileLoader.loadFile(currency);
				//NNTrainerTimeSeries trainer = new NNTrainerTimeSeries(this, fileLoader.loadValidateFile(currency));
				//trainer.trainNetwork(file);
				
				//SunSpotTimeseries ts = new SunSpotTimeseries();
				//ts.run(new String[] { "C:\\Users\\Michael\\workspace\\Pluton II" });
				
//				File dataDir = new File("encog-market");
//				MarketBuildTraining.generate(dataDir); // Generate
//				MarketTrain.train(dataDir); // Train
//				MarketEvaluate.evaluate(dataDir); // Evaluate
//				MarketPrune.incremental(dataDir); // Prune
//				Encog.getInstance().shutdown();
				HistoryLoader historyLoader = new HistoryLoader(this);
				
				List<String> jumpLims = new ArrayList<String>();
			//	jumpLims.add("" + Configuration.JUMP_LIMIT);
				
				// Benchmarking
//				jumpLims.add("1.003");
//				jumpLims.add("1.004");
//				jumpLims.add("1.005");
//				jumpLims.add("1.006");
//				jumpLims.add("1.007");
				jumpLims.add("1.008");
				jumpLims.add("1.009");
				jumpLims.add("1.01");
				jumpLims.add("1.011");
				jumpLims.add("1.012");
				jumpLims.add("1.015");
				jumpLims.add("1.02");
				
				List<String> jumpLimVols = new ArrayList<String>();
			//	jumpLimVols.add("" + Configuration.JUMP_LIMIT_VOL);
				
				// Benchmarking
//				jumpLimVols.add("5");
//				jumpLimVols.add("6");
//				jumpLimVols.add("7");
//				jumpLimVols.add("8");
//				jumpLimVols.add("9");
//				jumpLimVols.add("10");
//				jumpLimVols.add("11");
				jumpLimVols.add("12");
				jumpLimVols.add("13");
				jumpLimVols.add("14");
				jumpLimVols.add("18");
				jumpLimVols.add("22");

				for(String jumpLim: jumpLims) {
					for(String jumpLimVol: jumpLimVols) {
						Configuration.JUMP_LIMIT = Double.parseDouble(jumpLim);
						Configuration.JUMP_LIMIT_VOL = Double.parseDouble(jumpLimVol);
						dataHandler.getFunds().add(new Funds("USD", 1000, 1000));
						historyLoader.findJumpsV2(currency);
					}
				}
				
			}
			
			logger.logDebug("Finished training");
			System.exit(0);
		}
		
		if(Configuration.MODE.equals("simulation_MACD")) {
			MACDAgent macdAgent = new MACDAgent(this);
			macdAgent.start();
			
			logger.logDebug("Finished loading MACD history");
			System.exit(0);
		}
		

		//restHandler_cex = new Rest_CEX(verbose);
		
		//restHandler.getData("https://cex.io/api/currency_limits");
		
		//auth.upNonce();
		//restHandler.postData("https://cex.io/api/balance/", new String[] { auth.getApi(), auth.getSignature(), "" + auth.getNonce() });
		
		//auth.upNonce();
		//restHandler.postData("https://cex.io/api/order_book/BTC/USD/", new String[] { auth.getApi(), auth.getSignature(), "" + auth.getNonce() });

		Configuration.currencyConfig.put("BCHUSD", new CurrencyConfigs(new double[] {1.003, 2, 0.992, 1.04, 1.004} , 30000));
		Configuration.currencyConfig.put("DSHUSD", new CurrencyConfigs(new double[] {1.009, 2, 0.992, 1.04, 1.004}, 30000));
		Configuration.currencyConfig.put("IOTUSD", new CurrencyConfigs(new double[] {1.009, 2, 0.992, 1.04, 1.004}, 30000));
		Configuration.currencyConfig.put("XMRUSD", new CurrencyConfigs(new double[] {1.009, 1.5, 0.992, 1.04, 1.004}, 30000));
		Configuration.currencyConfig.put("SANUSD", new CurrencyConfigs(new double[] {1.015, 2, 0.992, 1.04, 1.004}, 300000));
		Configuration.currencyConfig.put("OMGUSD", new CurrencyConfigs(new double[] {1.008, 2, 0.992, 1.04, 1.004}, 30000));

		Configuration.currencyConfig.put("ETHUSD", new CurrencyConfigs(new double[] {1.008, 2, 0.992, 1.04, 1.004}, 30000));
		Configuration.currencyConfig.put("LTCUSD", new CurrencyConfigs(new double[] {1.007, 2, 0.992, 1.04, 1.004}, 30000));

		dataHandler.load24HVolume(currencies);
		scanner = new Scanner(this);
		scanner.start();
		
//		analyzer = new Analyzer(verbose, this);
//		analyzer.start();
//
//		missionHandler = new MissionHandler(verbose, this);
//		missionHandler.start();
	
	}
	
	public String timestampToDate(long timestamp) {
		Date d = new Date();
		d.setTime(timestamp);
		
		return d.toLocaleString();
	}
	
	
}
