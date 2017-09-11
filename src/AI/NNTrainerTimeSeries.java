package AI;

import java.io.File;
import java.util.Arrays;

import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.mathutil.error.ErrorCalculation;
import org.encog.mathutil.error.ErrorCalculationMode;
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
import org.encog.util.arrayutil.VectorWindow;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import org.encog.util.simple.EncogUtility;

import Engine.Pluton;

public class NNTrainerTimeSeries {
	
	Pluton parent;
	File file_validate;
	
	public static final int WINDOW_SIZE = 4;
	
	public NNTrainerTimeSeries(Pluton parent) {
		
		this.parent = parent;
	}
	
	public NNTrainerTimeSeries(Pluton parent, File file_validate) {
		
		this.parent = parent;
		this.file_validate = file_validate;
	}
	
	public void trainNetwork(File file) {
		
		ErrorCalculation.setMode(ErrorCalculationMode.ESS);
		
		CSVFormat format = new CSVFormat('.', ','); 
		
		VersatileDataSource dataSource = new CSVDataSource(file, true, format);
		VersatileMLDataSet data = new VersatileMLDataSet(dataSource);
		data.getNormHelper().setFormat(format);

		ColumnDefinition columnGain = data.defineSourceColumn("gain", ColumnType.continuous);
		ColumnDefinition columnVolume = data.defineSourceColumn("volume", ColumnType.continuous);
//		ColumnDefinition columnGain2 = data.defineSourceColumn("gain2", ColumnType.continuous);
//		ColumnDefinition columnGain3 = data.defineSourceColumn("gain3", ColumnType.continuous);
//		ColumnDefinition columnGain4 = data.defineSourceColumn("gain4", ColumnType.continuous);
//		ColumnDefinition columnGain5 = data.defineSourceColumn("gain5", ColumnType.continuous);
//		
//		ColumnDefinition nextClose = data.defineSourceColumn("nextGain", ColumnType.continuous);
		
		data.analyze();
		data.defineInput(columnGain);
		data.defineInput(columnVolume);
//		data.defineInput(columnGain2);
//		data.defineInput(columnGain3);
//		data.defineInput(columnGain4);
//		data.defineInput(columnGain5);
		
		data.defineOutput(columnGain);
		
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
		
		model.setReport(new ConsoleStatusReportable());
		data.normalize();
		
		data.setLeadWindowSize(1);
		data.setLagWindowSize(WINDOW_SIZE);
		
		model.holdBackValidation(0.3, false, 1001);
		
		model.selectTrainingType(data);
		
		MLRegression bestMethod = (MLRegression) model.crossvalidate(5,	false);
		
		System.out.println("Training error: "
				+ model.calculateError(bestMethod, model.getTrainingDataset()));
		
		System.out.println("Validation error: "
				+ model.calculateError(bestMethod, model.getValidationDataset()));
		
		// Display our normalization parameters.
		NormalizationHelper helper = data.getNormHelper();
		System.out.println(helper.toString());

		// Display the final model.
		System.out.println("Final model: " + bestMethod);
		
		ReadCSV csv = new ReadCSV(file, true, format);
		String[] line = new String[2];
		
		// Create a vector to hold each time-slice, as we build them.
		// These will be grouped together into windows.
		double[] slice = new double[2];
		VectorWindow window = new VectorWindow(WINDOW_SIZE + 1);
		MLData input = helper.allocateInputVector(WINDOW_SIZE + 1);
		
		// Only display the first 100
		int stopAfter = 100;
		
		while (csv.next() && stopAfter > 0) {
			StringBuilder result = new StringBuilder();

			line[0] = csv.get(1);
			line[1] = csv.get(2);
//			line[2] = csv.get(2);
//			line[3] = csv.get(3);
//			line[4] = csv.get(4);
			helper.normalizeInputVector(line, slice, false);

			// enough data to build a full window?
			if (window.isReady()) {
				window.copyWindow(input.getData(), 0);
				String correct = csv.get(1); // trying to predict SSN.
				MLData output = bestMethod.compute(input);
				String predicted = helper
						.denormalizeOutputVectorToString(output)[0];

				result.append(Arrays.toString(line));
				result.append(" -> predicted: ");
				result.append(predicted);
				result.append("(correct: ");
				result.append(correct);
				result.append(")");

				System.out.println(result.toString());
			}
		
			// Add the normalized slice to the window. We do this just after
			// the after checking to see if the window is ready so that the
			// window is always one behind the current row. This is because
			// we are trying to predict next row.
			window.add(slice);

			stopAfter--;
		}
		
		
		file.delete();
		Encog.getInstance().shutdown();
	}
	
}
