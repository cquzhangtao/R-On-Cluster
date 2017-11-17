import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class BatchRun {

	public static void main(String[] args) {
		System.out.println();
		System.out.println("OSRAM R Client @Unibw Munich 11-09-2017");
		
		RConnection connection = null;
		 
        try {
        	
        	String path=BatchRun.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        	
        	if(path.contains("jar")){
        		if(path.contains(":/")){
        			path=path.substring(1, path.lastIndexOf("/")).replace("/", File.separator);
        		}else{
        			path=path.substring(0, path.lastIndexOf("/")).replace("/", File.separator);
        		}
        	} 
        	else{
        		path=path.substring(1, path.lastIndexOf("/bin/")).replace("/", File.separator);
        	}
        	System.out.println(path);


        	System.out.println("Connectting to Rserve server...");
            connection = new RConnection();
            
            runScenarios(path,connection);
           
            
        } catch (RserveException e) {
           // e.printStackTrace();
        	error("Rserve server is not started. Please install the server if necessary and start it in your R console and run this again."
        	 		+ "-- install.packages(\"Rserve\")-> library(Rserve)-> Rserve();");

        } catch (URISyntaxException e) {
			
			//e.printStackTrace();
			error("Exception on File path.");
		}finally{
            connection.close();
        }
        exit();

	}
	
	private static void runScenarios(String path,RConnection connection){
		List<List<String>> scenarios=readScenarios(path);
		String scriptPath=path+File.separator+"script.txt";
		for(int i=1;i<scenarios.size();i++){
			List<String> scenario=scenarios.get(i);
			if(isScenarioDisable(scenarios.get(0),scenario)){
				continue;
			}
			runScenario(scenario,scenarios.get(0),scriptPath,connection);
			
		}
		System.out.println("All scenarios are executed!");
		System.out.println("Aggregating the results...");
		//agregateResults(path,scenarios);
		System.out.println("Aggregating is ignored.");
		System.out.println("Batch execution is done!");
	}
	
	private static void agregateResults(String path,List<List<String>> scenarios){
		PrintWriter writer=null;
		try {
			writer = new PrintWriter( path+File.separator+"result.txt") ;
		} catch (FileNotFoundException e1) {
			//e1.printStackTrace();
			error("Result file error");
		}
		
		for(int i=1;i<scenarios.size();i++){
			List<String> scenario=scenarios.get(i);
			if(isScenarioDisable(scenarios.get(0),scenario)){
				continue;
			}
			String outputFile=getOutputFile(scenarios.get(0),scenario);
			String name=getScenarioName(scenarios.get(0),scenario);
			try {
				String result= new String(Files.readAllBytes(Paths.get(outputFile)));
				result=name+","+result;
				writer.println(result);
			} catch (IOException e) {
				//e.printStackTrace();
				error("Agreating results error "+outputFile);
			}
			
		}
		writer.close();
	}
	
	private static void runScenario(List<String>scenario,List<String>header,String path,RConnection connection){
		generateScriptForScenario(header,scenario,path);
		String scenarioName=getScenarioName(header,scenario);
		 System.out.println("Executing the script of Scenario "+scenarioName+" on Rserve server...");
         String loadScript="source('"+path+"')";
         loadScript=loadScript.replace("\\", "/");
         try {
			connection.eval(loadScript);
			System.out.println("Executed!");
		} catch (RserveException e) {
			//e.printStackTrace();
			error("Script error");
		}

        
	}
	
	private static List<List<String>> readScenarios(String path){
		try{
		 	System.out.println("Reading scenarios...");
		 	List<List<String>> scenarios=new ArrayList<List<String>>();
			List<String> configs=Files.readAllLines(Paths.get(path+File.separator+"scenarios.txt"));
			for(String config:configs){
				if(config.isEmpty()){
					continue;
				}
				scenarios.add(Arrays.asList(config.split("\t")));
			}
			return scenarios;
		} catch (IOException e) {
			e.printStackTrace();
			error("Config file or template file is broken.");
		}
		return null;
	}
	

	
	private static String getValueOfHeader(String name,List<String> headers,List<String> values){
		for(int i=0;i<headers.size();i++){
			if(headers.get(i).equalsIgnoreCase(name)){
				return values.get(i);
			}
		}
		error("Parameter: "+name+" is not found.");
		return null;
	}
	
	private static String getTemplatePath(List<String> configNames,List<String> configValues){
		return getValueOfHeader("Template",configNames,configValues);
	}
	
	private static String getScenarioName(List<String> configNames,List<String> configValues){
		return getValueOfHeader("Name",configNames,configValues);
	}
	private static boolean isScenarioDisable(List<String> headers,List<String> values){
		return getValueOfHeader("Include",headers,values).isEmpty();
	}
	private static String getOutputFile(List<String> configNames,List<String> configValues){
		return getValueOfHeader("Output",configNames,configValues);
	}
	
	private static void generateScriptForScenario(List<String> names,List<String> values,String scriptPath){
		
		String tempFile=getTemplatePath(names,values);
		String scenarioName=getScenarioName(names,values);
		System.out.println("Reading template for Scenario "+ scenarioName+"...");
		try {						
			String template= new String(Files.readAllBytes(Paths.get(tempFile)));
			System.out.println("Generating script for Scenario "+ scenarioName+"...");
			
			for(int i=0;i<names.size();i++){
				String regex="(?i)"+Pattern.quote("[["+names.get(i).trim()+"]]");
				template=template.replaceAll(regex, values.get(i));
			}
			new File(scriptPath).delete();
			 PrintWriter out = null;
			try{  out = new PrintWriter( scriptPath); 
			    out.println( template );
			    
			}catch (IOException e) {
				error("Script file "+scriptPath+" error.");
			}
			finally{
				out.close();
			}
			if(template.contains("[[") ||template.contains("]]")){
				error("One or more parameters are not defined in the config file.");
			}
	
			System.out.println("Script is generated.");
			
		} catch (IOException e) {
			error("Template file "+tempFile+" error.");
		}
	
		
	}

	
	private static void syntaxError(int lineNum, String content){
		error("Syntax error in the config file:", "Line "+lineNum+" :"+content);
	}
	
	private static void error(String... errors){
		for(String error: errors){
			System.err.println("ERROR: "+error);
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
		System.out.println("The client is terminated.");
		exit();
	}
	
	private static void exit(){
		try {
			System.out.println("Please press any key to quit ......");
			System.in.read();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.exit(-1);
	}

}
