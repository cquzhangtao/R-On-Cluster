import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ScenarioUtilities {
	public static String scenarioFileName="scenarios.txt";
	public static List<List<String>> readScenarios(String path) {
		// try{
		Utilities.printInfo("Reading scenarios...");
		List<List<String>> scenarios = new ArrayList<List<String>>();
		List<String> configs = null;
		try {
			configs = Files.readAllLines(Paths.get(path
					+ File.separator + scenarioFileName));
		} catch (IOException e) {
			try {
				Charset charset = Charset.forName("Cp1252");
				configs = Files.readAllLines(
						Paths.get(path + File.separator
								+ scenarioFileName), charset);
			} catch (IOException ie) {
				e.printStackTrace();
				Utilities.printErrorAndExit("Scenario file is missng or broken.");

			}
		}
		if (configs == null) {
			return null;
		}
		for (String config : configs) {
			if (config.isEmpty()) {
				continue;
			}
			scenarios.add(Arrays.asList(config.split("\t")));
		}
		return scenarios;
		// } catch (IOException e) {
		// e.printStackTrace();
		// error("Config file or template file is broken.");
		// }
		// return null;
	}
}
