import java.io.IOException;
import java.net.ServerSocket;


public class ServerThread extends Thread{
	//private static int port=6311;
	private String script;
	private BatchRunOnServer server;
	private Process process=null;
	private boolean succeed=false;
	private ParallelBatchRunOnServer mainServer;
	

	public boolean isSucceed() {
		return succeed;
	}

	public void setSucceed(boolean succeed) {
		this.succeed = succeed;
	}

	public ServerThread(String rPath,String rServePath,String name,ParallelBatchRunOnServer mainServer){
		//port++;
		int port;
		port = Utilities.getAvailablePort();
		//if(OSValidator.isWindows()){
			setProcess(startServer(rPath,rServePath,port,name));
		//}
		this.mainServer=mainServer;
		server=new BatchRunOnServer(port);
		server.start();
	}

	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
		
	}
	public void run(){
		
		succeed=server.runScriptFile(server.getScenarioName(),script);
		
		if(succeed){

			Utilities.printInfo("***************************************************************",
					"***************************************************************",
					"**********      Scenario "+ server.getScenarioName()+" is done!",
					"***************************************************************",
					"***************************************************************");
		}else{
			Utilities.printInfo("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!",
					"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!",
					"!!!!!!!!!!      Scenario "+ server.getScenarioName()+" failed",
					"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!",
					"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		server.close();
		if(process!=null){
			//process.destroyForcibly();
			//System.out.println(process.getClass().getName());
			
			process.destroy();
	
			//Utilities.killProcess(process);
			//Utilities.printInfo("Process for "+server.getScenarioName()+" is killed.");
		}
		
		mainServer.onOneScenarioDoneForLimitedBatchSize();
		
	
		
		
	}
	public void close(){
		server.close();	
	}
	public static Process startServer(String rPath,String rServePath,int port,String name){
//		BatchRunStandAlone run=new BatchRunStandAlone();
//		run.start();
//		run.run(".libPaths(\"C:/Users/shufang xie/Documents/R/win-library/3.4\")"+System.lineSeparator()+"library('Rserve')"+System.lineSeparator()+"Rserve(port="+ port+")");
//		run.close();
//		C:\Users\shufang xie\Documents\R\win-library\3.4\Rserve\libs\x64\Rserve.exe 
		String cmd = null;
		if(OSValidator.isWindows()){
			cmd=rPath+"\\R.exe";
		}else{
			cmd=rPath+"/R";
		}
		
		int repeat=0;
		Process process=null;
		while(repeat<50){
			process=StartRserve.launchRserve(cmd,rServePath.replace("\\", "/"),port,name);
			if(process!=null){
				break;
			}
			repeat++;
		}
		
		return process;

	}
	public BatchRunOnServer getServer() {
		return server;
	}
	public void setServer(BatchRunOnServer server) {
		this.server = server;
	}
	public Process getProcess() {
		return process;
	}
	public void setProcess(Process process) {
		this.process = process;
	}
	
}
