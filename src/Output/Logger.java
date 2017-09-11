package Output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Logger {

	BufferedWriter debugLog;
	BufferedWriter tradeLog;
	
	public Logger(String debugLogPath, String tradeLogPath) {
		try{
			FileWriter debugFileWriter = new FileWriter(debugLogPath, true);
			debugLog = new BufferedWriter(debugFileWriter);
		} catch (Exception e) {
		   System.out.println("Could not find debug log file path");
		   e.printStackTrace();
		}
		
		try{
			FileWriter tradeFileWriter = new FileWriter(tradeLogPath, true);
			tradeLog = new BufferedWriter(tradeFileWriter);
		} catch (Exception e) {
			   System.out.println("Could not find trade log file path");
			   e.printStackTrace();
		}
	}
	
	public void logDebug(String entry) {
		
		System.out.println("Logging to debug");
		
		try {
			Date d = new Date();
			
			debugLog.write(d.toString() + ": " + entry + "\n");
			debugLog.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void logTrade(String entry) {
		try {
			Date d = new Date();
			
			tradeLog.write(d.toString() + ": " + entry + "\n");
			tradeLog.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
