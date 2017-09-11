package Data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import Config.Configuration;
import Engine.Pluton;

public class FileLoader {
	
	Pluton parent;
	
	public FileLoader(Pluton parent) {
		this.parent = parent;
	}
	
	public File loadValidateFile(String currency) {
		String cur1 = currency.split("/")[1];
		String cur2 = currency.split("/")[2];
		
		return new File("datapoints_" + cur1 + "_" + cur2 + "_validate.data");
	}

	public File loadFile(String currency) {
		String cur1 = currency.split("/")[1];
		String cur2 = currency.split("/")[2];
		
		List<String> rows = parent.dbHandler.getDataPoints(cur1, cur2, Configuration.INTERVAL_TICK_GEN);
		File file = new File("datapoints_" + cur1 + "_" + cur2 + ".data");
		
		try {
			FileWriter fw = new FileWriter(file);
			PrintWriter pw = new PrintWriter(fw);
			
			pw.println("timestamp,gain,volume");
			
			boolean first = true;
			
			int latency = Configuration.NN_LATENCY-1;
			int count = 0;
			String[] history = new String[latency];
			String currentPrint = "";
			
			for(String row: rows) {
				
				if(count < latency) {
					history[count] = row;
					count++;
				} else {
					
//					if(!first) {
//						pw.println("," + normalize(Double.parseDouble(row.split(",")[0])));
//						//currentPrint += "," + row.split(",")[0] + "\n";
//						//System.out.println("Add next gain: " + row.split(",")[0]);
//					}
					
//					for(int i = 0; i < history.length; i++) {
//						pw.print(history[i] + ",");
//						//currentPrint += "h: " + history[i] + ",";
//						//System.out.println("history: "  + history[i] + ",");
//					}
					
					pw.println(row);
					//currentPrint += "r: " +  row;
					//System.out.println("Current print: " + currentPrint);
	
					history = shiftArray(history, row, latency);
					
					first = false;
				}
			}
			//pw.print(",1.0");
			pw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return file;
	}
	
	private int normalize(double value) {
		value *= 10;
		
		return (int) value;
	}
	
	private  String[] shiftArray(String[] history, String newRow, int latency) {
		
		String[] tempArray = new String[latency];
		
		for(int i = 1; i < history.length; i++) {
			tempArray[i-1] = history[i];
			//System.out.println("Adding to history[" + (i-1) + "]: " + history[i]);
		}
		
		tempArray[tempArray.length-1] = newRow;
		//System.out.println("Adding to history[" + (tempArray.length-1) + "]: " + newRow);
		//System.out.println();
		
		return tempArray;
	}
}
