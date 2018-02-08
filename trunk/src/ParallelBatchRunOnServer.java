import java.util.ArrayList;
import java.util.List;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;

public class ParallelBatchRunOnServer extends BatchRunOnServer{
	
	private List<ServerThread> threads=new ArrayList<ServerThread>();
	private Process process=null;
	
	
	public ParallelBatchRunOnServer(){

		super(0);
		 
		int port=Utilities.getAvailablePort();
		this.setPort(port);
		setConfigs(new Configuration(getAppPath()));
		process=ServerThread.startServer(getConfigs().getRPath(),getConfigs().getRServePath(),port,"Main");
	}
	
	
	
	@Override
	public void onScenariosStart(){
		List<ServerThread> failedThreads=new ArrayList<ServerThread>();
		for(ServerThread thread:threads){
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String str="";
		
		for(ServerThread thread:threads){
			if(!thread.isSucceed()){
				str+=thread.getServer().getScenarioName()+" ";
			}
		}
		
		info();
		info();
		
		if(!str.isEmpty()){
			info("Scenarios " +str+" failed");
		}else{
			info("All Scenarios are executed successfully");
		}
		
		info("Batch execution is done!");
		close();
		Utilities.exit();
		
		//killProcesses();
		//onScenariosDone();
	
	}
	
	
	private void killProcesses(){
		//for(ServerThread thread:threads){
		//	if(thread.getProcess()!=null){
		//		thread.getProcess().destroyForcibly();
		//	}
		//}
		close();
		if(process!=null){
			process.destroyForcibly();
		}
		
//		for(ServerThread thread:threads){
//		//thread.close();
//		try {
//			int rServerPid=thread.getServer().getConnection().eval("Sys.getpid()").asInteger();
//			this.getConnection().eval("tools::pskill("+ rServerPid + ")");
//			this.getConnection().eval("tools::pskill("+ rServerPid + ", tools::SIGKILL)");
//		} catch (RserveException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (REXPMismatchException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	BatchRunStandAlone run=new BatchRunStandAlone();
//	run.start();
//	try {
//		int rServerPid=getConnection().eval("Sys.getpid()").asInteger();
//		run.run("tools::pskill("+ rServerPid + ")");
//		run.run("tools::pskill("+ rServerPid + ", tools::SIGKILL)");
//	} catch (RserveException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	} catch (REXPMismatchException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	run.close();
		
	}
		



//
//	public void runScenario(List<String>scenario,List<String>header,String path){
//		info();
//		generateScriptForScenario(header,scenario,path);
//		String scenarioName=getScenarioName(header,scenario);
//		
//         String loadScript="source('"+path+"')";
//         loadScript=loadScript.replace("\\", "/");
//         runScriptFile(loadScript,scenarioName);
//         //info("Scenario "+scenarioName+" is executed!");        
//	}
	
	@Override
	
	public boolean runScriptFile(String scenarioName,String script){

		ServerThread thread=new ServerThread(getConfigs().getRPath(),getConfigs().getRServePath(),scenarioName);
		threads.add(thread);
		//thread.setScenarioName(scenarioName);
		thread.setScript(script);
		thread.getServer().setScenarioName(scenarioName);
		info("Executing the script of Scenario "+scenarioName+" ...");
		thread.start();
		return true;

		
	
	}
	@Override
	public void onOneScenarioDone(String scenarioName) {
		// keep empty
		
	}
	
	
	
	

	
	

}
