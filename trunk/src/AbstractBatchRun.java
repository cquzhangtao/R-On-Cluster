import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

public abstract class AbstractBatchRun extends BasicLogger {
	private String appPath;
	private String installedLibFolder;
	private Configuration configs;

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

	protected abstract void connect();

	public void start() {
		connect();
		init();
	}



	public void init() {
		installedLibFolder = getAppPath() + File.separator + "installed_libs";

		if (!new File(installedLibFolder).exists()) {
			new File(installedLibFolder).mkdirs();
		}
		installedLibFolder = installedLibFolder.replace("\\", "/");
		run(".libPaths(\"" + installedLibFolder + "\")");

	}

	public void onScenariosDone() {
		info();
		info("All scenarios are executed!");
		info("Aggregating the results...");
		// agregateResults(path,scenarios);
		info("Aggregation is ignored.");
		info("Batch execution is done!");
		close();
		Utilities.exit();
	}

	public void onScenariosStart() {

	}

	public void runScenarios() {

		info();
		info("Starting to run scenarios");

		List<List<String>> scenarios = ScenarioUtilities.readScenarios(configs
				.getWorkingPath());

		for (int i = 1; i < scenarios.size(); i++) {
			List<String> scenario = scenarios.get(i);
			if (isScenarioDisable(scenarios.get(0), scenario)) {
				continue;
			}
			// String scenarioName = getScenarioName(scenarios.get(0),
			// scenario);
			// String scriptPath = getAppPath() + File.separator + scenarioName
			// + ".script.txt";
			runScenario(scenario, scenarios.get(0), configs.getWorkingPath());

		}
		onScenariosStart();

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

	public abstract void onOneScenarioDone(String scenarioName);

	public void runScenario(List<String> scenario, List<String> header,
			String workingPath) {
		info();
		String script = generateScriptForScenario(header, scenario, workingPath);
		String scenarioName = getScenarioName(header, scenario);
		info("Executing the script of Scenario " + scenarioName + " ...");
		String loadScript = "source('" + script + "')";
		loadScript = loadScript.replace("\\", "/");
		runScriptFile(scenarioName, loadScript);
		onOneScenarioDone(scenarioName);
	}

	public abstract REXPAdapter run(String loadScript);

	public abstract boolean runScriptFile(String name, String file);

	// protected List<List<String>> readScenarios() {
	// // try{
	// info("Reading scenarios...");
	// List<List<String>> scenarios = new ArrayList<List<String>>();
	// List<String> configs = null;
	// try {
	// configs = Files.readAllLines(Paths.get(getAppPath()
	// + File.separator + "scenarios.txt"));
	// } catch (IOException e) {
	// try {
	// Charset charset = Charset.forName("Cp1252");
	// configs = Files.readAllLines(
	// Paths.get(getAppPath() + File.separator
	// + "scenarios.txt"), charset);
	// } catch (IOException ie) {
	// e.printStackTrace();
	// error("Scenario file is broken.");
	// }
	// }
	// if (configs == null) {
	// return null;
	// }
	// for (String config : configs) {
	// if (config.isEmpty()) {
	// continue;
	// }
	// scenarios.add(Arrays.asList(config.split("\t")));
	// }
	// return scenarios;
	// // } catch (IOException e) {
	// // e.printStackTrace();
	// // error("Config file or template file is broken.");
	// // }
	// // return null;
	// }

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
			List<String> configValues, String workingPath) {
		String tempPath = getValueOfHeaderNoExit("Template", configNames,
				configValues);
		if (tempPath != null && !tempPath.trim().isEmpty()) {
			return tempPath;
		}
		String scenarioName = getScenarioName(configNames, configValues);
		info("No template specified for sceanrio " + scenarioName
				+ ", try to use the template.txt under the working path");
		return workingPath + File.separator + "template.txt";
	}

	protected String getScenarioName(List<String> configNames,
			List<String> configValues) {
		return getValueOfHeader("Name", configNames, configValues);
	}

	protected boolean isScenarioDisable(List<String> headers,
			List<String> values) {
		return !getValueOfHeader("Include", headers, values).equalsIgnoreCase("y");
	}

	private String getOutputFile(List<String> configNames,
			List<String> configValues) {
		return getValueOfHeader("Output", configNames, configValues);
	}

	protected String generateScriptForScenario(List<String> names,
			List<String> values, String workingPath) {

		String tempFile = getTemplatePath(names, values, workingPath);
		String scenarioName = getScenarioName(names, values);
		String scenarioPath = workingPath + File.separator + scenarioName;
		new File(scenarioPath).mkdirs();

		String scriptPath = scenarioPath + File.separator + scenarioName
				+ ".script";

		info("Reading template for Scenario " + scenarioName + "...");
		String template=null;
		try {
			template = new String(
					Files.readAllBytes(Paths.get(tempFile)));
		} catch (IOException e) {
			error("Read template file " + tempFile + " error. It may not exsit or be broken.");
		}
		info("Generating script for Scenario " + scenarioName + "...");
	

			for (int i = 0; i < names.size(); i++) {

				String name = names.get(i).trim();
				String value = values.get(i);

				if (name.startsWith("*")) {

					String file = values.get(i);
					if (!new File(file).exists()) {
						info("File specified in the scenarios does not exit. Try to find the data file in the user working folder");
						if(!new File(workingPath+File.separator+file).exists()){
							error("File specified in the scenarios does not exit in the working folder too. "
								+ file);
						}else{
							info("Found the data file in the user working folder");
							file=workingPath+File.separator+file;
						}
					}

					String desfile = scenarioPath + File.separator
							+ new File(file).getName();

					try {
						Files.copy(Paths.get(file), Paths.get(desfile),
								StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						//e.printStackTrace();
						error("Copy input file to "+desfile+" error.",e.getClass().getName());
					}

					name = name.substring(1);
					value = desfile;
				}

				String regex = "(?i)" + Pattern.quote("[[" + name + "]]");
				template = template.replaceAll(regex, value);
			}
			String regex = "(?i)" + Pattern.quote("[[ScenarioPath]]");
			template = template.replaceAll(regex, scenarioPath);

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
			if (template.contains("[[") && template.contains("]]")) {
				String str = template.substring(template.indexOf("[["),
						template.indexOf("]]") + 2);
				error("One or more parameters in the template are not defined in the scenario file.",
						" If it is a tool-reserved parameter, please spell the name correctly.",
						str);
			}

			info("Script is generated.");
			return scriptPath;

		
		

	}

	protected abstract void close();

	public String getAppPath() {
		return appPath;
	}

	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}

	public void installLocalLibs() {

		String libFolder = getAppPath() + File.separator + "lib";

		if (!new File(libFolder).exists()) {
			error("Required librares are not found.");
		}
		// run("Sys.setenv(\"R_LIBS_USER\"=\""+installedLibFolder+"\")");

		// REXP ret =engine.eval("installed.packages()[,c(1,3)]");
		// info(ret.asStringArray().toString());
		// REXP ret =run(".libPaths(\""+installedLibFolder+"\")");
		run(".libPaths()");
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
						REXPAdapter re = run("'" + shortName
								+ "' %in% installed.packages()[,c(1,3)]");

						if (re != null && re.isBoolean()&& re.isTrue()) {
							installed = true;

							continue;
						}

						info("Installing libaray " + shortName + " ...");
					}
					someUninstalled = true;
					if (OSValidator.isWindows()
							|| this instanceof BatchRunStandAlone) {
						// REXP re
						// =run("install.packages(\""+name+"\",\""+installedLibFolder+"\",repos = NULL, type=\"source\")");
						REXPAdapter re = run("install.packages(\"" + name
								+ "\",\"" + installedLibFolder
								+ "\",repos = NULL, type=\"source\")");

						if (re == null) {
							error("Cannot install library:" + name);
						}

					} else {
						String rPath = configs.getRPath();
						String str = rPath + "/R CMD INSTALL --library="
								+ installedLibFolder + " " + name;
						try {
							info(str);
							Process p = Runtime.getRuntime().exec(str);
							StreamHog errorHog = new StreamHog(
									p.getErrorStream(), false);
							StreamHog outputHog = new StreamHog(
									p.getInputStream(), false);
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

	public String getInstalledLibFolder() {
		return installedLibFolder;
	}

	public void setInstalledLibFolder(String installedLibFolder) {
		this.installedLibFolder = installedLibFolder;
	}

	public Configuration getConfigs() {
		return configs;
	}

	public void setConfigs(Configuration configs) {
		this.configs = configs;
	}

}
