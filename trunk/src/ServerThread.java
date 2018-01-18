
public class ServerThread extends Thread{
	private static int port=6311;
	private String script;
	private BatchRunOnServer server;
	

	public ServerThread(String rPath,String rServePath){
		port++;
		//if(OSValidator.isWindows()){
			startServer(rPath,rServePath,port);
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

		
		
	}
	public void close(){
		server.close();	
	}
	public static void startServer(String rPath,String rServePath,int port){
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
		
		StartRserve.launchRserve(cmd,rServePath.replace("\\", "/"),port);

	}
	public BatchRunOnServer getServer() {
		return server;
	}
	public void setServer(BatchRunOnServer server) {
		this.server = server;
	}
	
}
