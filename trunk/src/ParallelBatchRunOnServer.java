import java.util.ArrayList;
import java.util.List;

public class ParallelBatchRunOnServer extends BatchRunOnServer{
	
	private static int port=6311;
	private List<ServerThread> threads=new ArrayList<ServerThread>();
	
	public ParallelBatchRunOnServer(){
		super(port);
		setConfigs(new Configuration(getAppPath()));
		ServerThread.startServer(getConfigs().getRPath(),getConfigs().getRServePath(),port);
	}
	
	
	
	@Override
	public void onScenariosStart(){
		for(ServerThread thread:threads){
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		onScenariosDone();
	}
	
	
		
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
	
	public void runScriptFile(String scenarioName,String script){

		ServerThread thread=new ServerThread(getConfigs().getRPath(),getConfigs().getRServePath());
		threads.add(thread);
		//thread.setScenarioName(scenarioName);
		thread.setScript(script);
		thread.getServer().setScenarioName(scenarioName);
		info("Executing the script of Scenario "+scenarioName+" ...");
		thread.start();

		
	
	}
	@Override
	public void onOneScenarioDone(String scenarioName) {
		// keep empty
		
	}
	
	
	
	

	
	

}
