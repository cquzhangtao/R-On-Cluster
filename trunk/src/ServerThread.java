import java.io.IOException;
import java.net.ServerSocket;


public class ServerThread extends Thread{
	//private static int port=6311;
	private String script;
	private BatchRunOnServer server;
	private Process process=null;
	

	public ServerThread(String rPath,String rServePath){
		//port++;
		int port;
		port = Utilities.getAvailablePort();
		//if(OSValidator.isWindows()){
			setProcess(startServer(rPath,rServePath,port));
		//}
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
		
		server.runScriptFile(server.getScenarioName(),script);

		Utilities.printInfo("*******************************************","Scenario "+ server.getScenarioName()+" is executed!","*******************************************");
		if(process!=null){
			//process.destroyForcibly();
			//System.out.println(process.getClass().getName());
			server.close();
			//process.destroy();
	
			//Utilities.killProcess(process);
			//Utilities.printInfo("Process for "+server.getScenarioName()+" is killed.");
		}
		
		
	}
	public void close(){
		server.close();	
	}
	public static Process startServer(String rPath,String rServePath,int port){
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
		
		return StartRserve.launchRserve(cmd,rServePath.replace("\\", "/"),port);

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
