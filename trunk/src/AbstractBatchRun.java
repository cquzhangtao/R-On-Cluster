import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

public abstract class AbstractBatchRun {
	private String appPath;
	private String installedLibFolder;
	private int wayToIntegerateJava;

	public AbstractBatchRun() {

		try {

			String path = AbstractBatchRun.class.getProtectionDomain()
					.getCodeSource().getLocation().toURI().getPath();

			if (path.contains("jar")) {
				if (path.contains(":/")) {
					path = path.substring(1, path.lastIndexOf("/")).replace(
							"/", File.separator);
				} else {
					path = path.substring(0, path.lastIndexOf("/")).replace(
							"/", File.separator);
				}
			} else {
				path = path.substring(1, path.lastIndexOf("/bin/")).replace(
						"/", File.separator);
				if (!path.startsWith("/")) {
					path = "/" + path;
				}
			}
			setAppPath(path);
		}

		catch (URISyntaxException e) {

			error("Exception on File path.");
		}

	}

	public abstract void start();

	public void printTitle() {
		System.out.println();
		System.out.println("R Experiment Tool @Unibw Munich 11-09-2017");
		System.out.println();
	}

	public void init() {
		installedLibFolder = getAppPath() + File.separator + "installed_libs";

		if (!new File(installedLibFolder).exists()) {
			new File(installedLibFolder).mkdirs();
		}
		installedLibFolder = installedLibFolder.replace("\\", "/");
		run(".libPaths(\"" + installedLibFolder + "\")");

	}

	public void afterScenarioRuns() {
		info("All scenarios are executed!");
		info("Aggregating the results...");
		// agregateResults(path,scenarios);
		info("Aggregation is ignored.");
		info("Batch execution is done!");
	}

	public void runScenarios() {
		List<List<String>> scenarios = readScenarios();

		for (int i = 1; i < scenarios.size(); i++) {
			List<String> scenario = scenarios.get(i);
			if (isScenarioDisable(scenarios.get(0), scenario)) {
				continue;
			}
			String scenarioName = getScenarioName(scenarios.get(0), scenario);
			String scriptPath = getAppPath() + File.separator + scenarioName
					+ ".script.txt";
			runScenario(scenario, scenarios.get(0), scriptPath);

		}

	}

	private void agregateResults(List<List<String>> scenarios) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(getAppPath() + File.separator
					+ "result.txt");
		} catch (FileNotFoundException e1) {
			// e1.printStackTrace();
			error("Result file error");
		}

		for (int i = 1; i < scenarios.size(); i++) {
			List<String> scenario = scenarios.get(i);
			if (isScenarioDisable(scenarios.get(0), scenario)) {
				continue;
			}
			String outputFile = getOutputFile(scenarios.get(0), scenario);
			String name = getScenarioName(scenarios.get(0), scenario);
			try {
				String result = new String(Files.readAllBytes(Paths
						.get(outputFile)));
				result = name + "," + result;
				writer.println(result);
			} catch (IOException e) {
				// e.printStackTrace();
				error("Agreating results error " + outputFile);
			}

		}
		writer.close();
	}

	public void runScenario(List<String> scenario, List<String> header,
			String path) {
		generateScriptForScenario(header, scenario, path);
		String scenarioName = getScenarioName(header, scenario);
		info("Executing the script of Scenario " + scenarioName + " ...");
		String loadScript = "source('" + path + "')";
		loadScript = loadScript.replace("\\", "/");
		run(loadScript);
		info("Executed!");
	}

	public abstract REXP run(String loadScript);

	protected List<List<String>> readScenarios() {
		// try{
		info("Reading scenarios...");
		List<List<String>> scenarios = new ArrayList<List<String>>();
		List<String> configs = null;
		try {
			configs = Files.readAllLines(Paths.get(getAppPath()
					+ File.separator + "scenarios.txt"));
		} catch (IOException e) {
			try {
				Charset charset = Charset.forName("Cp1252");
				configs = Files.readAllLines(
						Paths.get(getAppPath() + File.separator
								+ "scenarios.txt"), charset);
			} catch (IOException ie) {
				e.printStackTrace();
				error("Scenario file is broken.");
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

	private String getValueOfHeader(String name, List<String> headers,
			List<String> values) {
		String value = getValueOfHeaderNoExit(name, headers, values);
		if (value != null) {
			return value;
		}
		error("Parameter: " + name + " is not found in scenario.txt.");
		return null;
	}

	private String getValueOfHeaderNoExit(String name, List<String> headers,
			List<String> values) {
		if (headers.size() != values.size()) {
			error("Scenario file error: number of parameters are not equal to number of values.");
		}
		for (int i = 0; i < headers.size(); i++) {
			if (headers.get(i).equalsIgnoreCase(name)) {
				return values.get(i);
			}
		}
		return null;
	}

	private String getTemplatePath(List<String> configNames,
			List<String> configValues) {
		String tempPath = getValueOfHeaderNoExit("Template", configNames,
				configValues);
		if (tempPath != null && !tempPath.trim().isEmpty()) {
			return tempPath;
		}
		String scenarioName = getScenarioName(configNames, configValues);
		info("No template specified for sceanrio " + scenarioName
				+ ", try to use the template.txt under tool folder");
		return getAppPath() + File.separator + "template.txt";
	}

	protected String getScenarioName(List<String> configNames,
			List<String> configValues) {
		return getValueOfHeader("Name", configNames, configValues);
	}

	protected boolean isScenarioDisable(List<String> headers,
			List<String> values) {
		return getValueOfHeader("Include", headers, values).isEmpty();
	}

	private String getOutputFile(List<String> configNames,
			List<String> configValues) {
		return getValueOfHeader("Output", configNames, configValues);
	}

	protected void generateScriptForScenario(List<String> names,
			List<String> values, String scriptPath) {

		String tempFile = getTemplatePath(names, values);
		String scenarioName = getScenarioName(names, values);
		info("Reading template for Scenario " + scenarioName + "...");
		try {
			String template = new String(
					Files.readAllBytes(Paths.get(tempFile)));
			info("Generating script for Scenario " + scenarioName + "...");

			for (int i = 0; i < names.size(); i++) {
				String regex = "(?i)"
						+ Pattern.quote("[[" + names.get(i).trim() + "]]");
				template = template.replaceAll(regex, values.get(i));
			}
			String regex = "(?i)" + Pattern.quote("[[ScenarioName]]");
			template = template.replaceAll(regex, scenarioName);

			template = ".libPaths(\"" + installedLibFolder + "\")"
					+ System.lineSeparator() + template;

			new File(scriptPath).delete();
			PrintWriter out = null;
			try {
				out = new PrintWriter(scriptPath);
				out.println(template);

			} catch (IOException e) {
				error("Script file " + scriptPath + " error.");
			} finally {
				out.close();
			}
			if (template.contains("[[") || template.contains("]]")) {
				error("One or more parameters are not defined in the config file.");
			}

			info("Script is generated.");

		} catch (IOException e) {
			error("Template file " + tempFile + " error.");
		}

	}

	protected void syntaxError(int lineNum, String content) {
		error("Syntax error in the config file:", "Line " + lineNum + " :"
				+ content);
	}

	protected void info(String... errors) {
		if (errors.length == 0) {
			Utilities.printInfo();
		} else {
			Utilities.printInfo(errors);
		}
	}

	protected void error(String... errors) {
		errorNoExit(errors);
		info("The tool is terminated.");
		exit();
	}

	protected void errorNoExit(String... errors) {
		for (String error : errors) {
			System.err.println("ERROR: " + error);
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// e.printStackTrace();
		}

	}

	void exit() {
		// close();
		try {
			if (OSValidator.isWindows()) {
				info("Please press any key to quit ......");
				System.in.read();
			} else {

			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Utilities.killAllRserver();
		System.exit(-1);
	}

	protected abstract void close();

	public String getAppPath() {
		return appPath;
	}

	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}

	public void installLocalLibs(String rPath) {
		String libFolder = getAppPath() + File.separator + "lib";

		if (!new File(libFolder).exists()) {
			error("Required librares are not found.");
		}
		// run("Sys.setenv(\"R_LIBS_USER\"=\""+installedLibFolder+"\")");

		// REXP ret =engine.eval("installed.packages()[,c(1,3)]");
		// info(ret.asStringArray().toString());
		// REXP ret =run(".libPaths(\""+installedLibFolder+"\")");
		REXP yret = run(".libPaths()");
		// info(ret..toString());

		File[] files = new File(libFolder).listFiles();
		if (files == null && files.length == 0) {
			error("Required librares are not found.");
		}

		boolean someUninstalled = true;

		while (someUninstalled) {
			someUninstalled = false;
			for (File file : files) {
				if (file.isDirectory()) {
					continue;
				}

				String ext;
				if (OSValidator.isWindows()) {
					ext = ".zip";
				} else {
					ext = ".tar.gz";
				}
				if (file.getName().toLowerCase().contains(ext)) {
					String name = file.getAbsolutePath().replace("\\", "/");
					boolean installed = false;
					if (name.lastIndexOf("_") > -1
							&& name.lastIndexOf("/") + 1 < name.length()) {
						String shortName = name.substring(
								name.lastIndexOf("/") + 1,
								name.lastIndexOf("_"));
						// info("Loading libaray "+shortName+" ...");
						REXP re = run("'" + shortName
								+ "' %in% installed.packages()[,c(1,3)]");
						try {
							if (re != null && re.asString() != null
									&& re.asString().equalsIgnoreCase("true")) {
								installed = true;
								
								continue;
							}
						} catch (REXPMismatchException e) {

							e.printStackTrace();
							error("Cannot install library:" + name);
						}
						info("Installing libaray " + shortName + " ...");
					}
					someUninstalled = true;
					if (OSValidator.isWindows()) {
						// REXP re
						// =run("install.packages(\""+name+"\",\""+installedLibFolder+"\",repos = NULL, type=\"source\")");
						REXP re = run("install.packages(\"" + name + "\",\""
								+ installedLibFolder
								+ "\",repos = NULL, type=\"source\")");

						if (re == null) {
							error("Cannot install library:" + name);
						}

					} else {
						String str = rPath + "/R CMD INSTALL --library="
								+ installedLibFolder + " " + name;
						try {
							info(str);
							Process p = Runtime.getRuntime().exec(str);
							//StreamHog errorHog = new
							// StreamHog(p.getErrorStream(), false);
							// StreamHog outputHog = new
							// StreamHog(p.getInputStream(), false);
							p.waitFor();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
							error("Cannot install library:" + name, str);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
							error("Cannot install library:" + name, str);
						}
					}
				}

			}
		}
	}

	public int getWayToIntegerateJava() {
		return wayToIntegerateJava;
	}

	public void setWayToIntegerateJava(int wayToIntegerateJava) {
		this.wayToIntegerateJava = wayToIntegerateJava;
	}

	public String getInstalledLibFolder() {
		return installedLibFolder;
	}

	public void setInstalledLibFolder(String installedLibFolder) {
		this.installedLibFolder = installedLibFolder;
	}

}
