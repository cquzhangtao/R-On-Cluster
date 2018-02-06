import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScenarioSplitter {

	public static  Set<String> split(String inputfile) {

		Map<String, List<String>> splittedDataset = new HashMap<String, List<String>>();

		try {
			List<String> dataset = Files.readAllLines(Paths.get(inputfile));
			String header=dataset.remove(0);

			for (String data : dataset) {
				if (data.isEmpty() || !data.contains("\t")) {
					Utilities.printErrorAndExit("Input data file error");
				}
				String station = data.substring(0, data.indexOf("\t"));
				if (!splittedDataset.containsKey(station)) {
					splittedDataset.put(station, new ArrayList<String>());

				}
				splittedDataset.get(station).add(data);

			}

			String path=inputfile.substring(0,inputfile.lastIndexOf(File.separator))+File.separator+"splitteddataset";
			new File(path).mkdirs();
			String filename=inputfile.substring(inputfile.lastIndexOf(File.separator));
			

			for (String station : splittedDataset.keySet()) {
				PrintWriter writer = new PrintWriter(path+filename + "." + station);
				writer.println(header);
				for (String line : splittedDataset.get(station)) {
					writer.println(line);
				}
				writer.close();

			}
		} catch (IOException e) {
			e.printStackTrace();
			Utilities.printErrorAndExit("Input data file error");
		}

		return splittedDataset.keySet();

	}

	public static void createScenarios(String path) {
		Utilities.printInfo("Splitting the scnarios...");
		try {
			List<List<String>> scenarios = ScenarioUtilities
					.readScenarios(path);
			ScenarioUtilities.scenarioFileName="SplittedScenarios.txt";
			new File(path + File.separator
					+ ScenarioUtilities.scenarioFileName).delete();
			PrintWriter writer = new PrintWriter(path + File.separator
					+ ScenarioUtilities.scenarioFileName);
			String header = getNewHeader(scenarios.get(0));
			writer.println(header);

			for (int i = 1; i < scenarios.size(); i++) {
				List<String> newScenarios = splitScenario(scenarios.get(0),
						scenarios.get(i), path);
				for (String sce : newScenarios) {
					writer.println(sce);
				}
				

			}
			writer.close();
			
			Utilities.printInfo("Splitting the scnarios is done");

		} catch (IOException e) {
			e.printStackTrace();
			Utilities.printErrorAndExit("Split scenario error");
		}

	}

	private static  String getNewHeader(List<String> header) {
		String inputHeader = "";
		String newHeader = "";
		for (int i = 0; i < header.size(); i++) {
			if (header.get(i).startsWith("*")) {
				inputHeader = header.get(i);
			} else if(header.get(i).equalsIgnoreCase("name")){
				
			}
			else {
				newHeader += header.get(i) + "\t";
			}
		}
		newHeader += inputHeader+"\tName";
		return newHeader;
	}

	public static  List<String> splitScenario(List<String> header, List<String> values,
			String workingPath) {

		Set<String> stations = null;
		int inputIdx = 0;
		String file = "";
		String name="";
		for (int i = 0; i < header.size(); i++) {
			if (header.get(i).startsWith("*")) {
				file = values.get(i);
				if (!new File(file).exists()) {
					//Utilities
						//	.printInfo("File specified in the scenarios does not exit. Try to find the data file in the user working folder");
					if (!new File(workingPath + File.separator + file).exists()) {
						Utilities
								.printErrorAndExit("File specified in the scenarios does not exit in the working folder too. "
										+ file);
					} else {
						//Utilities
							//	.printInfo("Found the data file in the user working folder");
						file = workingPath + File.separator + file;
					}
				}
				stations = split(file);
				inputIdx = i;
				//break;

			}
			
			if(header.get(i).equalsIgnoreCase("name")){
				name=values.get(i);
			}
		}

		String str = "";
		List<String> newScenarios = new ArrayList<String>();

		for (int i = 0; i < values.size(); i++) {
			if (inputIdx == i) {
				continue;
			}
			if(header.get(i).equalsIgnoreCase("name")){
				continue;
			}
			// if(i<values.size()-1){
			str += values.get(i) + "\t";
			// }else{
			// str+=values.get(i);
			// }

		}
		
		String path=file.substring(0,file.lastIndexOf(File.separator))+File.separator+"splitteddataset";
		
		String filename=file.substring(file.lastIndexOf(File.separator));

		for (String station : stations) {
			newScenarios.add(str + path+filename + "." + station+"\t"+name+"_"+station);
		}
		
		Utilities.printInfo(newScenarios.size()+" dispatching stations found in Scenario "+name);

		return newScenarios;

	}

	private  static String getValueOfHeader(String name, List<String> headers,
			List<String> values) {
		String value = getValueOfHeaderNoExit(name, headers, values);
		if (value != null) {
			return value;
		}
		Utilities.printErrorAndExit("Parameter: " + name
				+ " is not found in scenario.txt.");
		return null;
	}

	private  static String getValueOfHeaderNoExit(String name, List<String> headers,
			List<String> values) {
		if (headers.size() != values.size()) {
			Utilities
					.printErrorAndExit("Scenario file error: number of parameters are not equal to number of values.");
		}
		for (int i = 0; i < headers.size(); i++) {
			if (headers.get(i).equalsIgnoreCase(name)) {
				return values.get(i);
			}
		}
		return null;
	}

	protected static String getScenarioName(List<String> configNames,
			List<String> configValues) {
		return getValueOfHeader("Name", configNames, configValues);
	}

	protected  static boolean isScenarioDisable(List<String> headers,
			List<String> values) {
		return !getValueOfHeader("Include", headers, values).equalsIgnoreCase(
				"y");
	}

}
