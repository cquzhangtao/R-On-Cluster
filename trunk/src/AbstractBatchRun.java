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

public abstract class AbstractBatchRun {
	private String appPath;

	public AbstractBatchRun() {
		System.out.println();
		System.out.println("R Experiment Tool @Unibw Munich 11-09-2017");
		 
        try {
        	
        	String path=AbstractBatchRun.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        	
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
        	setAppPath(path);

           // runScenarios();
        }
           
       catch (URISyntaxException e) {
			
			//e.printStackTrace();
			error("Exception on File path.");
		}
       // exit();

	}
	
	public void runScenarios(){
		List<List<String>> scenarios=readScenarios();
		String scriptPath=getAppPath()+File.separator+"script.txt";
		for(int i=1;i<scenarios.size();i++){
			List<String> scenario=scenarios.get(i);
			if(isScenarioDisable(scenarios.get(0),scenario)){
				continue;
			}
			runScenario(scenario,scenarios.get(0),scriptPath);
			
		}
		System.out.println("All scenarios are executed!");
		System.out.println("Aggregating the results...");
		//agregateResults(path,scenarios);
		System.out.println("Aggregating is ignored.");
		System.out.println("Batch execution is done!");
	}
	
	private  void agregateResults(List<List<String>> scenarios){
		PrintWriter writer=null;
		try {
			writer = new PrintWriter( getAppPath()+File.separator+"result.txt") ;
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
	
	
	
	private void runScenario(List<String>scenario,List<String>header,String path){
		generateScriptForScenario(header,scenario,path);
		String scenarioName=getScenarioName(header,scenario);
		 System.out.println("Executing the script of Scenario "+scenarioName+" ...");
         String loadScript="source('"+path+"')";
         loadScript=loadScript.replace("\\", "/");
         run(loadScript);
         System.out.println("Executed!");
		

        
	}
	
	public abstract void run(String loadScript) ;

	private List<List<String>> readScenarios(){
		try{
		 	System.out.println("Reading scenarios...");
		 	List<List<String>> scenarios=new ArrayList<List<String>>();
			List<String> configs=Files.readAllLines(Paths.get(getAppPath()+File.separator+"scenarios.txt"));
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
	

	
	private  String getValueOfHeader(String name,List<String> headers,List<String> values){
		for(int i=0;i<headers.size();i++){
			if(headers.get(i).equalsIgnoreCase(name)){
				return values.get(i);
			}
		}
		error("Parameter: "+name+" is not found.");
		return null;
	}
	
	private  String getTemplatePath(List<String> configNames,List<String> configValues){
		return getValueOfHeader("Template",configNames,configValues);
	}
	
	private  String getScenarioName(List<String> configNames,List<String> configValues){
		return getValueOfHeader("Name",configNames,configValues);
	}
	private  boolean isScenarioDisable(List<String> headers,List<String> values){
		return getValueOfHeader("Include",headers,values).isEmpty();
	}
	private  String getOutputFile(List<String> configNames,List<String> configValues){
		return getValueOfHeader("Output",configNames,configValues);
	}
	
	private  void generateScriptForScenario(List<String> names,List<String> values,String scriptPath){
		
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

	
	private  void syntaxError(int lineNum, String content){
		error("Syntax error in the config file:", "Line "+lineNum+" :"+content);
	}
	
	protected  void error(String... errors){
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
	
	void exit(){
		close();
		try {
			System.out.println("Please press any key to quit ......");
			System.in.read();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.exit(-1);
	}

	protected abstract void close() ;

	public String getAppPath() {
		return appPath;
	}

	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}

}
