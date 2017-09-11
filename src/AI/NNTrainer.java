package AI;

import java.io.File;
import java.util.Arrays;

import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import org.encog.util.simple.EncogUtility;

import Engine.Pluton;

public class NNTrainer {
	
	Pluton parent;

	public NNTrainer(Pluton parent) {
		
		this.parent = parent;
	}
	
	public void trainNetwork(File file) {
		
		VersatileDataSource dataSource = new CSVDataSource(file, false, CSVFormat.DECIMAL_POINT);
		
		VersatileMLDataSet data = new VersatileMLDataSet(dataSource);
//		data.defineSourceColumn("gain1", 0, ColumnType.continuous);
//		data.defineSourceColumn("low1", 1, ColumnType.continuous);
//		data.defineSourceColumn("high1", 2, ColumnType.continuous);
//		data.defineSourceColumn("volume1", 3, ColumnType.continuous);
//		
//		data.defineSourceColumn("gain2", 4, ColumnType.continuous);
//		data.defineSourceColumn("low2", 5, ColumnType.continuous);
//		data.defineSourceColumn("high2", 6, ColumnType.continuous);
//		data.defineSourceColumn("volume2", 7, ColumnType.continuous);
//		
//		data.defineSourceColumn("gain3", 8, ColumnType.continuous);
//		data.defineSourceColumn("low3", 9, ColumnType.continuous);
//		data.defineSourceColumn("high3", 10, ColumnType.continuous);
//		data.defineSourceColumn("volume3", 11, ColumnType.continuous);
//		
//		data.defineSourceColumn("gain4", 12, ColumnType.continuous);
//		data.defineSourceColumn("low4", 13, ColumnType.continuous);
//		data.defineSourceColumn("high4", 14, ColumnType.continuous);
//		data.defineSourceColumn("volume4", 15, ColumnType.continuous);
//		
//		data.defineSourceColumn("gain5", 16, ColumnType.continuous);
//		data.defineSourceColumn("low5", 17, ColumnType.continuous);
//		data.defineSourceColumn("high5", 18, ColumnType.continuous);
//		data.defineSourceColumn("volume5", 19, ColumnType.continuous);
//		
//		ColumnDefinition outputColumn = data.defineSourceColumn("nextClose", 20, ColumnType.continuous);
		
		
		data.defineSourceColumn("gain1", 0, ColumnType.continuous);
		//data.defineSourceColumn("volume1", 1, ColumnType.continuous);
		
		data.defineSourceColumn("gain2", 1, ColumnType.continuous);
		//data.defineSourceColumn("volume2", 3, ColumnType.continuous);
		
		data.defineSourceColumn("gain3", 2, ColumnType.continuous);
		//data.defineSourceColumn("volume3", 5, ColumnType.continuous);
		
		data.defineSourceColumn("gain4", 3, ColumnType.continuous);
		//data.defineSourceColumn("volume4", 7, ColumnType.continuous);
		
		data.defineSourceColumn("gain5", 4, ColumnType.continuous);
		//data.defineSourceColumn("volume5", 9, ColumnType.continuous);

		ColumnDefinition outputColumn = data.defineSourceColumn("nextClose", 5, ColumnType.continuous);
		
		data.analyze();
		data.defineSingleOutputOthersInput(outputColumn);
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		model.setReport(new ConsoleStatusReportable());
		
		data.normalize();
		model.holdBackValidation(0.3, true, 1001);
		model.selectTrainingType(data);
		
		MLRegression bestMethod = (MLRegression) model.crossvalidate(5, true);
		
		System.out.println( "Training error: " + EncogUtility.calculateRegressionError(bestMethod, model.getTrainingDataset()));
		System.out.println( "Validation error: " + EncogUtility.calculateRegressionError(bestMethod, model.getValidationDataset()));
		
		NormalizationHelper helper = data.getNormHelper();
		System.out.println(helper.toString());
		
		System.out.println("Final model: " + bestMethod);
		
		ReadCSV csv = new ReadCSV(file, false, CSVFormat.DECIMAL_POINT);
		//String[] line = new String[21];
		String[] line = new String[5];
		MLData input = helper.allocateInputVector();
		
		while(csv.next()) {
			StringBuilder result = new StringBuilder();
			
			line[0] = csv.get(0);
			line[1] = csv.get(1);
			line[2] = csv.get(2);
			line[3] = csv.get(3);

			line[4] = csv.get(4);
//			line[5] = csv.get(5);
//			line[6] = csv.get(6);
//			line[7] = csv.get(7);
//
//			line[8] = csv.get(8);
//			line[9] = csv.get(9);
//			line[10] = csv.get(10);
//			line[11] = csv.get(11);
//
//			line[12] = csv.get(12);
//			line[13] = csv.get(13);
//			line[14] = csv.get(14);
//			line[15] = csv.get(15);
//
//			line[16] = csv.get(16);
//			line[17] = csv.get(17);
//			line[18] = csv.get(18);
//			line[19] = csv.get(19);
			
			//String correct = csv.get(20);
			String correct = csv.get(5);
			helper.normalizeInputVector(line,input.getData(),false);
			MLData output = bestMethod.compute(input);
			String closeChosen = helper.denormalizeOutputVectorToString(output)[0];
			
			result.append(Arrays.toString(line));
			result.append(" -> predicted: ");
			result.append(closeChosen);
			result.append("(correct: ");
			result.append(correct);
			result.append(")");
			
			System.out.println(result.toString());
//			if(Double.parseDouble(closeChosen) > 4 || Double.parseDouble(correct) > 4 || Double.parseDouble(closeChosen) < -4 || Double.parseDouble(correct) < -4) {
//				System.out.println("Predicted: " + closeChosen);
//				System.out.println("Correct: " + correct);
//				System.out.println();
//			}
		}
		
		file.delete();
		Encog.getInstance().shutdown();
	}
	
}
