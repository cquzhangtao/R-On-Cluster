import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParallelBatchRunOnServer extends BatchRunOnServer{
	
	private static int port=6311;
	private List<ServerThread> threads=new ArrayList<ServerThread>();
	private Map<String,String> configs=new HashMap<String,String>();
	public ParallelBatchRunOnServer(){
		super(port);
		readConfigs(getAppPath());
		
	}
	
	public void start(){
		ServerThread.startServer(getRPath(),getRServePath(),port);
		
		super.start();
	}
	
	public void runScenarios(){
		info();
		info("Starting to run scenarios");
		List<List<String>> scenarios=readScenarios();
		
		String scriptTempPath=getAppPath()+File.separator+"generated_scripts";
		new File(scriptTempPath).mkdirs();
		
		for(int i=1;i<scenarios.size();i++){
			List<String> scenario=scenarios.get(i);
			if(isScenarioDisable(scenarios.get(0),scenario)){
				continue;
			}
			String scenarioName=getScenarioName(scenarios.get(0),scenario);
			String scriptPath=scriptTempPath+File.separator+scenarioName+".script.txt";
			runScenario(scenario,scenarios.get(0),scriptPath);
			
		}
		
		for(ServerThread thread:threads){
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Utilities.killAllRserver();
		afterScenarioRuns();
		
//		for(ServerThread thread:threads){
//			//thread.close();
//			try {
//				int rServerPid=thread.getServer().getConnection().eval("Sys.getpid()").asInteger();
//				this.getConnection().eval("tools::pskill("+ rServerPid + ")");
//				this.getConnection().eval("tools::pskill("+ rServerPid + ", tools::SIGKILL)");
//			} catch (RserveException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (REXPMismatchException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		BatchRunStandAlone run=new BatchRunStandAlone();
//		run.start();
//		try {
//			int rServerPid=getConnection().eval("Sys.getpid()").asInteger();
//			run.run("tools::pskill("+ rServerPid + ")");
//			run.run("tools::pskill("+ rServerPid + ", tools::SIGKILL)");
//		} catch (RserveException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (REXPMismatchException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		run.close();

	}

	public void runScenario(List<String>scenario,List<String>header,String path){
		info();
		generateScriptForScenario(header,scenario,path);
		String scenarioName=getScenarioName(header,scenario);
		
         String loadScript="source('"+path+"')";
         loadScript=loadScript.replace("\\", "/");
         runScriptFile(loadScript,scenarioName);
         //info("Scenario "+scenarioName+" is executed!");        
	}
	
	private void runScriptFile(String script,String scenarioName){

		ServerThread thread=new ServerThread(getRPath(),getRServePath());
		threads.add(thread);
		//thread.setScenarioName(scenarioName);
		thread.setScript(script);
		thread.getServer().setScenarioName(scenarioName);
		info("Executing the script of Scenario "+scenarioName+" ...");
		thread.start();

		
	
	}
	
	public String getRPath(){
		return getParameter("R_PATH");
	}
	public String getRServePath(){
		return getParameter("RSERVE_PATH");
	}
	
	private String getParameter(String name){
		String value=configs.get(name);
		if(value==null){
			error("Parameter:"+name+" is not defined in the config file.");
		}
		return value;
	}
	
	private  void readConfigs(String path){
	
			 info("Reading configuration...");
			List<String> configsList = null;
			try{
				configsList=Files.readAllLines(Paths.get(path+File.separator+"config.txt"));
			}catch(IOException e){
				try{
					Charset charset = Charset.forName("Cp1252");
					configsList=Files.readAllLines(Paths.get(path+File.separator+"config.txt"),charset);
				}catch(IOException e1){
					e1.printStackTrace();
					error("Config file does not exist or error!");
				}
			}
			 
			 int lineNum=0;
			for(String config:configsList){
				lineNum++;
				config=config.trim();
				if(config.isEmpty()||config.startsWith("//")){
					continue;
				}
				if(!config.contains("::=")){
					syntaxError(lineNum,config);
				}
				if(config.contains("//")&&config.indexOf("::=")>config.indexOf("//")){
					syntaxError(lineNum,config);
				}
				config=config.substring(0, config.indexOf("//")>0?config.indexOf("//"):config.length());
								
				String str[]=config.split("::=");
				String paraName=str[0].trim().toUpperCase();
				String paraValue=str[1].trim();
				if(paraName.isEmpty()||paraValue.isEmpty()){
					syntaxError(lineNum,config);
				}
				configs.put(paraName, paraValue);
			}
			
	}
	

	
	

}
