package DB;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import jdk.nashorn.internal.runtime.regexp.joni.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import Config.Configuration;

public class DBHandler {
	
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://" + Configuration.DATABASE_URL + "/" + Configuration.DATABASE_NAME;
	
	static final String USER = Configuration.DATABASE_USER;
	static final String PASS = Configuration.DATABASE_PW;
	
	Connection conn;
	Statement stm;
	
	public DBHandler() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			Properties properties = new Properties();
			properties.setProperty("user", USER);
			properties.setProperty("password", PASS);
			properties.setProperty("useSSL", "false");

			conn = DriverManager.getConnection(DB_URL, properties);
			conn.setAutoCommit(false);
			stm = conn.createStatement();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createMACDTable(String cur1, String cur2) {
		try {
			String sql = "drop table if exists history_MACD_" + cur1 + "_" + cur2;
			stm.executeUpdate(sql);
			
			sql = " create table if not exists history_MACD_" + cur1 + "_" + cur2 + " (timestamp bigint, price double)";
			System.out.println("Creating MACD table for " + cur1 + ", " + cur2);
			stm.executeUpdate(sql);
			conn.commit();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void createDataPointsTable(String cur1, String cur2, long intervals) {
		try {
			String sql = "drop table if exists datapoints_" + cur1 + "_" + cur2 + "_" + intervals;
			stm.executeUpdate(sql);
			
			sql = " create table if not exists datapoints_" + cur1 + "_" + cur2 + "_" + intervals + " (start bigint, stop bigint, gain double, low double, high double, volume double)";
			System.out.println("Creating data points table for " + cur1 + ", " + cur2);
			stm.executeUpdate(sql);
			conn.commit();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public long insertTrade(JSONArray jsonArray, String cur1, String cur2) {

		long latestTimestamp = -2;
		long earliestTimestamp = Long.MAX_VALUE;
		int count = 0;
		
		try {
			String sql = "INSERT IGNORE INTO history_" + cur1 + "_" + cur2 + " values (?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			
			System.out.println("Insert array: " + jsonArray.length());
			
			for(int i = 0; i < jsonArray.length(); i++) {
				JSONArray trade = jsonArray.getJSONArray(i);
				
				double amount = trade.getDouble(2);
				double price = trade.getDouble(3);
				long tid = trade.getLong(0);
				long currentTimestamp = trade.getLong(1);
				
				ps.setLong(1, tid);
				ps.setLong(2, currentTimestamp);
				ps.setDouble(3, amount);
				ps.setDouble(4, price);
				ps.addBatch();
				
				count = i;
				
				if(currentTimestamp > latestTimestamp)
					latestTimestamp = currentTimestamp;
				
				if(currentTimestamp < earliestTimestamp)
					earliestTimestamp = currentTimestamp;
				
			}
			
			System.out.println("Executing batch. Latest stamp: " + latestTimestamp);
			ps.executeBatch();
			conn.commit();
			System.out.println("Batch executed and committed.");
			
		} catch (BatchUpdateException bue) {
			bue.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		System.out.println("Trades committed: " + count);
		System.out.println("Earliest trade: " + earliestTimestamp);
		System.out.println("Latest trade: " + latestTimestamp);
		
		if(earliestTimestamp == latestTimestamp) // If only one trade returned - avoid infinite loop by adding one hour manually
			latestTimestamp += (3600 * 1000);
		
		return latestTimestamp;

	}
	
	public void createTable(String cur1, String cur2) {
		try {
			String sql = " create table if not exists history_" + cur1 + "_" + cur2 + " (tid int, timestamp bigint, amount double, price double)";
			stm.executeUpdate(sql);
			sql = "create index history_" + cur1 + "_" + cur2 + "_timestamp on history_" + cur1 + "_" + cur2 + " (timestamp)";
			stm.executeUpdate(sql);
			sql = "alter table history_" + cur1 + "_" + cur2 + " add primary key(tid)";
			stm.executeUpdate(sql);
			conn.commit();
			
		} catch (BatchUpdateException bue) {
			bue.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public double getLowestPrice(String cur1, String cur2, long start, long stop) {

		ResultSet res;
		double price = -1;
		
		try {
			String sql = "";
			sql = "select price from history_" + cur1 + "_" + cur2 + " where timestamp > " + start + " and timestamp <= " + stop + " limit 1";
			
			res = stm.executeQuery(sql);
			while(res.next()) {
				price = res.getDouble(1);
				return price;
			}		
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return price;

	}
	
	public double getCurrentPrice(String cur1, String cur2, long timestamp, int type) {

		ResultSet res;
		double price = -1;
		
		try {
			String sql = "";

			// Getting bid, offer or just last price?
			if(type == 0)
				sql = "select price from history_" + cur1 + "_" + cur2 + " where timestamp > " + timestamp + " and amount > 0 limit 1";

			if(type == 1)
				sql = "select price from history_" + cur1 + "_" + cur2 + " where timestamp > " + timestamp + " and amount < 0 limit 1";
			
			if(type == -1)
				sql = "select price from history_" + cur1 + "_" + cur2 + " where timestamp > " + timestamp + " limit 1";
			
			res = stm.executeQuery(sql);
			while(res.next()) {
				price = res.getDouble(1);
				return price;
			}		
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return price;

	}
	
	public void calculateAvgVolume(long startTimestamp, long endTimestamp, String cur1, String cur2) {

		double numDays = (endTimestamp - startTimestamp + 0.0) / (1000 * 60 * 60 * 24);
		
		try {
			String sql = " insert into average_volume (cur1, cur2, volume_self_24h, volume_usd_24h) values ('"
					+ cur1 + "', '" + cur2 + "', "
					+ " (select SUM(ABS(amount))/" + numDays + " from history_" + cur1 + "_" + cur2 + " where timestamp >= " + startTimestamp + " and timestamp < " + endTimestamp + "),"
					+ " (select SUM(ABS(amount) * price)/" + numDays + "  from history_" + cur1 + "_" + cur2 + " where timestamp >= " + startTimestamp + " and timestamp < " + endTimestamp + "))";
		
			System.out.println("Average volume calc SQL: " + sql);
			
			stm.executeUpdate(sql);
			conn.commit();
			
		} catch (BatchUpdateException bue) {
			bue.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
//	public boolean insertTrade(long tid, long timestamp, String cur1, String cur2, double amount, double price) {
//		try {
//			String sql = "INSERT INTO history values (?, ?, '?', '?', ?, ?)";
//			PreparedStatement ps = conn.prepareStatement(sql);
//			
//			System.out.println(sql);
//			stm.executeUpdate(sql);
//			return true;
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
//
//	}
	
	public double getAverageVolume(String cur1, String cur2, long stop) {
		ResultSet res;
		double realVolume = -1;
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(stop);
		cal.add(Calendar.DATE, -3);
		double start = cal.getTimeInMillis();
		
		try {
			String sql = "select sum(abs(amount) * price), MIN(timestamp) from history_" + cur1 + "_" + cur2 + " where timestamp > " + start + " and timestamp <= " + stop;
			
			res = stm.executeQuery(sql);
			while(res.next()) {
				
				if(res.getDouble(2) > start + (1000 * 60 * 60 * 3))
					return -1;
				
				realVolume = res.getDouble(1);
				
				realVolume *= (Configuration.INTERVAL_TICK_GEN / 60000);
				realVolume /= (3 * 24 * 60); 
			}		
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return realVolume;
	}
	
	public List<String> getDataPoints(String cur1, String cur2, long intervals) {
		
		ResultSet res;
		List<String> rows = new ArrayList<String>();
		
		try {
			String sql = "select round(start, 4), round(stop, 4), round(gain, 4), round(volume, 4) from datapoints_" + cur1 + "_" + cur2 + "_" + intervals;
			
			res = stm.executeQuery(sql);
			while(res.next()) {
				rows.add(res.getLong(1) + "," + res.getLong(2) + "," + res.getDouble(3) + "," + res.getDouble(4));
			}		
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return rows;
	}
	
	public List<String> getDataPoints(String cur1, String cur2, long intervals, long start, long stop) {
		
		ResultSet res;
		List<String> rows = new ArrayList<String>();
		
		try {
			String sql = "select round(start, 4), round(stop, 4), round(gain, 4), round(volume, 4) from datapoints_" + cur1 + "_" + cur2 + "_" + intervals 
					+ " where start > " + start + " and start <= " + stop;
			
			res = stm.executeQuery(sql);
			while(res.next()) {
				rows.add(res.getLong(1) + "," + res.getLong(2) + "," + res.getDouble(3) + "," + res.getDouble(4));
			}		
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return rows;
	}
	
	public long[] getStartStop(String currency) {
		String cur1 = currency.split("/")[1];
		String cur2 = currency.split("/")[2];
		
		ResultSet res;
		long start = -1;
		long stop = -1;
		
		try {
			String sql = "select MIN(timestamp) as START, MAX(timestamp) as STOP from history_" + cur1 + "_" + cur2;
			res = stm.executeQuery(sql);
			
			while(res.next()) {
				System.out.println("Get start");
				start = res.getLong("START");
				stop = res.getLong("STOP");
			}		
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return new long[] { start, stop };
		
	}
	
	public double get24HVolume(String cur1, String cur2) {
		
		ResultSet res;
		double volume = -1;
		
		try {
			String sql = "select volume_usd_24h as VOLUME from average_volume where cur1 = '" + cur1 + "' and cur2 = '" + cur2 + "'";
			res = stm.executeQuery(sql);
			
			while(res.next()) {
				System.out.println("Prefetch volume");
				volume = res.getDouble("VOLUME");
			}		
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return volume;
	}
	
	public void insertDataPoint(String cur1, String cur2, long start, long stop, double gain, double low, double high, double volume, long intervals) {
	
		try {
			String sql = "INSERT INTO datapoints_" + cur1 + "_" + cur2 + "_" + intervals + " (start, stop, gain, low, high, volume) values ("
					+ start + ", " + stop + ", " + gain + ", " + low + ", " + high + ", " + volume + ")";
		
			//System.out.println("Insert data points update :" + sql);
			stm.executeUpdate(sql);
			conn.commit();
			
		} catch (BatchUpdateException bue) {
			bue.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public void addMACDData(String cur1, String cur2, long timestamp, double price) {
		
		try {
			String sql = "INSERT INTO history_MACD_" + cur1 + "_" + cur2 + " (timestamp, price) values ("
					+ timestamp + ", " + price + ")";
		
			//System.out.println("Insert data points update :" + sql);
			stm.executeUpdate(sql);
			conn.commit();
			
		} catch (BatchUpdateException bue) {
			bue.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public List<String> getRawTrades(String cur1, String cur2, long timestamp, long interval) {
		long stop = timestamp + interval;
		
		List<String> data = new ArrayList<String>();
		
		ResultSet res;
		
		try {
			String sql = "select timestamp, amount, price from history_" + cur1 + "_" + cur2 + " where timestamp >= " + timestamp
					+ " and timestamp < " + stop;
			res = stm.executeQuery(sql);
			
			while(res.next()) {
				data.add(res.getLong("TIMESTAMP") + "," + res.getDouble("AMOUNT") + "," + res.getDouble("PRICE"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return data;
	}
	
	public JSONArray getPriceIntervals(String cur1, String cur2, long start, int arraySize) {

		ResultSet res;
		JSONArray jArray = new JSONArray();
		
		
		try {
			String sql = "select tid, timestamp, amount, price from history_" + cur1 + "_" + cur2 + " where timestamp > " + start + " order by timestamp asc limit " + arraySize;
			
			System.out.println(sql);
			res = stm.executeQuery(sql);
			
			while(res.next()) {
				JSONObject jObj = new JSONObject();
				jObj.put("tid", res.getLong(1));
				jObj.put("timestamp", res.getLong(2));
				jObj.put("amount", res.getDouble(3));
				jObj.put("price", res.getDouble(4));
				
				jArray.put(jObj);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return jArray;
	}
	
	public JSONArray getPriceIntervals(long start, long stop, String cur1, String cur2) {

		ResultSet res;
		JSONArray jArray = new JSONArray();
		
		
		try {
			String sql = "select tid, timestamp, amount, price from history_" + cur1 + "_" + cur2 + " where timestamp > " + start + 
					" and timestamp < " + stop + " order by timestamp asc";
			
			System.out.println(sql);
			res = stm.executeQuery(sql);
			
			while(res.next()) {
				JSONObject jObj = new JSONObject();
				jObj.put("tid", res.getLong(1));
				jObj.put("timestamp", res.getLong(2));
				jObj.put("amount", res.getDouble(3));
				jObj.put("price", res.getDouble(4));
				
				jArray.put(jObj);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return jArray;
	}
	
	public double getLastPrice(String cur1, String cur2, long end) {

		ResultSet res;
		
		try {
			String sql = "select price from history_" + cur1 + "_" + cur2 + " where timestamp < " + end + " order by timestamp desc limit 1";
			
			System.out.println(sql);
			
			res = stm.executeQuery(sql);
			
			while(res.next()) {
				return res.getDouble(1);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public Object[] getDataPoint(String cur1, String cur2, long currentStamp, long intervals) {
		long stop = currentStamp + intervals;
		
		Object[] data = new Object[7];
		data[0] = currentStamp;
		data[1] = stop;
		
		ResultSet res;
		
		try {
			String sql = "select price as OPEN from history_" + cur1 + "_" + cur2
					+ " where timestamp > " + currentStamp + " and timestamp <= " + stop + " order by timestamp limit 1";
			res = stm.executeQuery(sql);
			
			while(res.next()) {
				//System.out.println("Get open");

				data[2] = res.getDouble("OPEN");
				
				//System.out.println("data[2]: " + data[2]);
			}
			
			sql = "select price as CLOSE from history_" + cur1 + "_" + cur2
					+ " where timestamp > " + currentStamp + " and timestamp <= " + stop + " order by timestamp desc limit 1";
			res = stm.executeQuery(sql);
			
			while(res.next()) {
				//System.out.println("Get close");

				data[3] = res.getDouble("CLOSE");
			}	
			
			sql = "select MIN(price) as LOW from history_" + cur1 + "_" + cur2
					+ " where timestamp > " + currentStamp + " and timestamp <= " + stop;
			res = stm.executeQuery(sql);
			
			while(res.next()) {
				//System.out.println("Get low");

				data[4] = res.getDouble("LOW");
			}	
			
			sql = "select MAX(price) as HIGH from history_" + cur1 + "_" + cur2
					+ " where timestamp > " + currentStamp + " and timestamp <= " + stop;
			res = stm.executeQuery(sql);
			
			while(res.next()) {
				//System.out.println("Get high");

				data[5] = res.getDouble("HIGH");
			}	
			
			
			sql = "select SUM(ABS(amount) * price) as VOLUME from history_" + cur1 + "_" + cur2
					+ " where timestamp > " + currentStamp + " and timestamp <= " + stop;
			res = stm.executeQuery(sql);
			
			while(res.next()) {
				data[6] = res.getDouble("VOLUME");

				//System.out.println("Get volume: " + data[6]);
			}	

			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return data;
	}
}
