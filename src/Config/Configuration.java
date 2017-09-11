package Config;

import java.util.Arrays;
import java.util.List;

public class Configuration {
	
	//public static String MODE = "loadHistory";			// Stage 1 - poll historic data from exchange
	//public static String MODE = "generateData";				// Stage 2 - generate data points from historic data
	public static String MODE = "training";				// Stage 3 - Train AI
	//public static String MODE = "pluton";				// Stage 4 - let's roll
	
	public static long INTERVAL_TICK_GEN = 1000 * 60 * 5; // Five minute intervals
	//public static long INTERVAL_TICK_GEN = 1000 * 60 * 15; // Fifteen minute intervals
	//public static long INTERVAL_TICK_GEN = 1000 * 60 * 1440; // Day minute intervals
	
	public static int NUMBER_OF_DAYS_TRAINING = 30;
	public static int NUMBER_OF_DAYS_BACKLOAD = 30;
	public static int NUMBER_OF_TRADES_PER_BATCH = 1000;
	public static int NUMBER_OF_API_CALLS_MINUTE = 15;
	
	public static double JUMP_LIMIT = 1.008;
	public static double JUMP_LIMIT_VOL = 5;
	
	// Training
	public static int NN_LATENCY = 5;
	
	//public static String DATABASE_URL = "192.168.0.11";
	public static String DATABASE_URL = "localhost";
	public static String DATABASE_NAME = "pluton";
	public static String DATABASE_USER = "pluton";
	public static String DATABASE_PW = "pluton";
	
	public static String EXCHANGE = "cex";

	public static String DEBUG_LOG_FILEPATH = "debug.log";
	public static String TRADE_LOG_FILEPATH = "trade.log";
	public static String DATE_FORMAT = "HH:mm:ss";
	
	public static int FLOOR_CEILING_DEPTH = 20;
	public static int FLOOR_CEILING_SIZE = 1500;
	public static int FLOOR_CEILING_INTERVAL_SIZE = 20000;
	public static int FLOOR_CEILING_SIG_LIMIT = 600; // How small orders will be ignored when considering walls
	public static double WALL_TARGET_GAP_LIMIT = 0.8; // How small difference between wall and interval target before acting
	public static double WALLS_GAP_LIMIT = 0.5; // How small gap between floor and ceiling before acting
	
	public static double STOP_LOSS_LIMIT = 0.5; // How big percentage loss before dropping
	public static double ROI_GOAL = 0.8; // How many percentage return on investment to expect per trade
	
	public static double BID_ASK_SIG_LIMIT = 0.15; // How much variation in price before altering bid/ask

	public static int MISSION_INTERVAL = 4000;
	public static int SCANNER_INTERVAL = 3500;
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
	//public static List<String> CURRENCIES = Arrays.asList("BTF/LTC/USD", "BTF/XMR/USD");
	public static List<String> CURRENCIES = Arrays.asList("BTF/IOT/USD");
	//public static List<String> CURRENCIES = Arrays.asList("BTF/XMR/USD");
	//public static List<String> CURRENCIES = Arrays.asList("BTF/LTC/USD");
	//public static List<String> CURRENCIES = Arrays.asList("BTF/OMG/USD");
	
	// 4. Finished training.
	// None
}
