package Config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
	
//	public static String MODE = "loadHistory";				// Stage 1a - poll historic data from exchange
	//public static String MODE = "generateData";				// Stage 2 - generate data points from historic data
	//public static String MODE = "simulation";					// Stage 3a - Simulate strategy
	public static String MODE = "simulation_MACD";			// Stage 3b - Simulate MACD strategy
	//public static String MODE = "news";
	//public static String MODE = "pluton";						// Stage 4 

	//public static String MODE = "test_mail";					// Test mail

	//public static long INTERVAL_TICK_GEN = (long) (1000 * 60 * 0.25); // Fifteen second intervals
	//public static long INTERVAL_TICK_GEN = (long) (1000 * 60 * 0.5); // Thirty second intervals
	//public static long INTERVAL_TICK_GEN = 1000 * 60 * 1; // One minute intervals
	//public static long INTERVAL_TICK_GEN = 1000 * 60 * 5; // Five minute intervals
	public static long INTERVAL_TICK_GEN = 1000 * 60 * 15; // Fifteen minute intervals
	//public static long INTERVAL_TICK_GEN = 1000 * 60 * 1440; // Day minute intervals

	public static String toMail = "semtex789@gmail.com";
	public static String fromMail = "pluton726@gmail.com";
	public static String mail = "!1Pluton67";
	
	public static int NUMBER_OF_DAYS_TRAINING = 30;
	public static int NUMBER_OF_DAYS_BACKLOAD = 5;
	public static int NUMBER_OF_TRADES_PER_BATCH = 1000;
	public static int NUMBER_OF_API_CALLS_MINUTE = 15;
	public static boolean VERBOSE = true;
	public static boolean TEST = true;
	public static boolean TEST_DETAILED_REPORT = true;
	public static boolean MARGIN_ENABLED = true;
	public static boolean MACD_ENABLED = false;
	public static boolean RS_ENABLED = true;

	public static int VOLUME_SHORT_TERM_LENGTH = 3;
	public static int VOLUME_LONG_TERM_LENGTH = 30;
	public static int VOLUME_TRESHOLD = 10;
	
	public static double JUMP_LIMIT = 1.003;
	public static double JUMP_LIMIT_VOL = 5;
	public static double STOP_LOSS_LIMIT = 0.97; 	// How big percentage loss before dropping
	public static double STOP_LOSS_LIMIT_SHORT = 0.96; // tested
	public static double ROI_GOAL = 1.05; 			// How many percentage return on investment to expect per trade
	public static double SLIPPAGE_LIMIT = 1.004;
	
	public static double SUPPORT_TRESHOLD = 1.001;
	public static int SR_LENGTH = 80;
	public static int TEST_TICKS = 300;
	public static double MAX_RS_ANGLE = 0.03; // percent per hour
	public static double RS_SENSITIVITY = 0.003;
	public static long TICK_POLISH = 1800000;
	
	public static int MACD_EMA_1 = 12;
	public static int MACD_EMA_2 = 26;
	public static int MACD_SIGNAL_LINE = 6;
	//public static int MACD_TIME_PERIOD = 15 * 60000; // 15 minute periods
	public static int MACD_TIME_PERIOD = 30 * 60000; // 30 minute periods
//	public static int MACD_TIME_PERIOD = 60 * 60000; // 60 minute periods
	public static int MACD_LIMIT_BUY = 6;
	public static int MACD_LIMIT_SELL = 8;
	public static int MACD_LIMIT_SCOPE = 200;
	public static double MACD_HISTOGRAM_GAIN_TRESHOLD_BUY = 6;
	public static double MACD_HISTOGRAM_GAIN_TRESHOLD_SELL = 2;
	public static int NUMBER_OF_TICKS_IN_PERIOD = 5;
	public static long TRADING_HALT_PERIOD = 0 * 60 * 60 * 1000; // No trading halt
//	public static long TRADING_HALT_PERIOD = 3 * 60 * 60 * 1000; // 3 hour trading halt
//	public static long TRADING_HALT_PERIOD = 6 * 60 * 60 * 1000; // 6 hour trading halt
//	public static long TRADING_HALT_PERIOD = 9 * 60 * 60 * 1000; // 9 hour trading halt
//	public static long TRADING_HALT_PERIOD = 12 * 60 * 60 * 1000; // 12 hour trading halt
	
	public static int RSI1 = 14;
	public static int RSISTOCH1 = 14;
	public static int RSISTOCH2 = 3;
	
	public static Map<String, CurrencyConfigs> currencyConfig = new HashMap<String, CurrencyConfigs>();
	
	// Testing
//	public static double JUMP_LIMIT = 1.001;		
//	public static double JUMP_LIMIT_VOL = 0.001;
//	public static double STOP_LOSS_LIMIT = 0.995;
//	public static double ROI_GOAL = 1.005;
//	public static double SLIPPAGE_LIMIT = 1.004;
	
	// Training
	public static int NN_LATENCY = 5;
	
	//public static String DATABASE_URL = "192.168.0.11";
	public static String DATABASE_URL = "localhost";
	public static String DATABASE_NAME = "pluton";
	public static String DATABASE_USER = "pluton";
	public static String DATABASE_PW = "pluton";
	
	public static String EXCHANGE = "cex";
	public static int API_TIMEOUT_RETRY = 3000;

	public static String DEBUG_LOG_FILEPATH = "debug.log";
	public static String TRADE_LOG_FILEPATH = "trade.log";
	public static String DATE_FORMAT = "HH:mm:ss";
	
	public static int FLOOR_CEILING_DEPTH = 20;
	public static int FLOOR_CEILING_SIZE = 1500;
	public static int FLOOR_CEILING_INTERVAL_SIZE = 20000;
	public static int FLOOR_CEILING_SIG_LIMIT = 600; // How small orders will be ignored when considering walls
	public static double WALL_TARGET_GAP_LIMIT = 0.8; // How small difference between wall and interval target before acting
	public static double WALLS_GAP_LIMIT = 0.5; // How small gap between floor and ceiling before acting
	
	public static double BID_ASK_SIG_LIMIT = 0.15; // How much variation in price before altering bid/ask

	public static int MISSION_INTERVAL = 4000;
	public static int SCANNER_INTERVAL = 30000;
	public static int ANALYZE_CURRENT_PRICE_INTERVAL = 4000;
	public static int ANALYZE_PRICE_HISTORY_DEPTH = 30;
	public static int ANALYZE_FLOOR_CEILING_INTERVAL = 4000;
	
	public static boolean ACT_ON_WALLS = true;
	public static boolean ACT_ON_PRICE_HISTORY = false;
	
	public static double STARTING_FUNDS = 1000;
	public static double BASE_INVESTING_AMOUNT = 10;

	// 1. Get historic data
	//public static List<String> CURRENCIES = Arrays.asList("BTF/BTC/USD", "BTF/ETH/USD", "BTF/ZEC/USD", "BTF/EOS/USD", "BTF/BCH/USD", "BTF/RRT/USD", "BTF/XRP/USD", "BTF/SAN/USD");
	//public static List<String> CURRENCIES = Arrays.asList("BTF/IOT/USD");
	
	// 2. Generate data points
	// None
	
	// 3. Training.
	//public static List<String> CURRENCIES = Arrays.asList("BTF/XMR/USD", "BTF/IOT/USD", "BTF/LTC/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/BTC/USD");
	public static List<String> CURRENCIES = Arrays.asList("BTF/ETH/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/BCH/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/NEO/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/DSH/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/ETC/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/SAN/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/IOT/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/ZEC/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/XMR/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/LTC/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/OMG/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/EOS/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/ETP/USD");
//	public static List<String> CURRENCIES = Arrays.asList("BTF/XRP/USD");
	
	//public static List<String> CURRENCIES = Arrays.asList("BTF/BTC/USD", "BTF/BCH/USD", "BTF/ETH/USD", "BTF/LTC/USD");

	//public static List<String> CURRENCIES = Arrays.asList("BTF/BCH/USD", "BTF/BTC/USD");
	

	//public static List<String> CURRENCIES = Arrays.asList("BTF/LTC/USD", "BTF/ETH/USD");

//	public static List<String> CURRENCIES = Arrays.asList(
//			"BTF/XMR/USD",
//			"BTF/OMG/USD",
//			"BTF/IOT/USD",
//			"BTF/SAN/USD",
//			"BTF/DSH/USD", 
//			"BTF/BCH/USD", 
//			"BTF/BTC/USD",
//			"BTF/LTC/USD", 
//			"BTF/NEO/USD",
//			"BTF/EOS/USD",
//			"BTF/ETH/USD", 
//			"BTF/ETP/USD", 
//			"BTF/ETC/USD", 
//			"BTF/XRP/USD",
//			"BTF/ZEC/USD");

	
	// 4. Finished training.

	//public static List<String> CURRENCIES = Arrays.asList("BTF/BCH/USD", "BTF/IOT/USD"); // Use for real testing
	// None
	
	public static List<String> NEWS_SOURCES = Arrays.asList(
			"hacker-news|top", 
			"the-washington-post|top",
			"the-wall-street-journal|top",
			"the-telegraph|top",
			"the-new-york-times|top",
			"the-next-web|latest",
			"the-guardian-uk|top",
			"the-economist|top",
			"techcrunch|top",
			"techradar|top",
			"reuters|top", 
			"recode|top",
			"reddit-r-all|top", 
			"new-scientist|top",
			"mirror|top", 
			"independent|top",
			"fortune|top", 
			"focus|top",
			"financial-times|top",
			"daily-mail|top",
			"cnn|top",
			"cnbc|top",
			"buzzfeed|top",
			"business-insider-uk|top",
			"business-insider|top",
			"breitbart-news|top",
			"bloomberg|top",
			"bbc-news|top",
			"ars-technica|top",
			"al-jazeera-english|top",
			"abc-news-au|top");
}
