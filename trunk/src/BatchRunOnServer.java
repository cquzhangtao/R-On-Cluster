import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class BatchRunOnServer extends AbstractBatchRun {

	private RConnection connection;
	private String scenarioName=null;

	
	private int port=6311;

	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public BatchRunOnServer(int port) {
		super();
		this.port = port;

	}
	public BatchRunOnServer() {
		super();
		setConfigs(new Configuration(getAppPath()));
		port = Utilities.getAvailablePort();
		ServerThread.startServer(getConfigs().getRPath(),getConfigs().getRServePath(),port,"Main");
	}

	@Override
	public REXPAdapter run(String loadScript) {
		if(connection==null){
			return null;
		}
		REXP result = null;

			try {
				result = connection.eval(loadScript);
			} catch (RserveException e) {
				errorNoExit("Script error:" + loadScript);
			}
			if(result==null){
				errorNoExit("Script error:" + loadScript);
			}


		return new REXPAdapter(result);
	}

	@Override
	public void close() {
		if (connection != null) {
			info("Shutdown the server at port " + port);
			
			try {
				connection.shutdown();
			} catch (RserveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			connection.close();

		}
	}

	@Override
	public void connect() {
//
//		try {
//			//if (OSValidator.isWindows()) {
//				info("Connectting to Rserve server at port " + port + "...");
//				connection = new RConnection("localhost", port);
//			//} else {
//			//	info("Connectting to Rserve server...");
//			//	connection = new RConnection();
//			//}
//		} catch (RserveException e) {
//			// e.printStackTrace();
//			error("Rserve server is not started. Please install the server if necessary and start it in your R console and run this again."
//					+ "-- install.packages(\"Rserve\")-> library(Rserve)-> Rserve();");
//
//		}
		
		
		
		int attempts = 15; /* try up to 5 times before giving up. We can be conservative here, because at this point the process execution itself was successful and the start up is usually asynchronous */
		while (attempts > 0) {
			try {
				//if (OSValidator.isWindows()) {
					info("Connectting to Rserve server at port " + port + "...");
					connection = new RConnection("localhost", port);
				//}else{
				//	info("Connectting to Rserve server...");
					//connection = new RConnection();
				//}
				info("Connected");
				return;
			} catch (Exception e2) {
				//e2.printStackTrace();
				info("failed to connect to Rserve, try again now...");
			}
			/* a safety sleep just in case the start up is delayed or asynchronous */
			try { Thread.sleep(500); } catch (InterruptedException ix) { };
			attempts--;
		}
		errorNoExit("Cannot connect to the Rserve");

	}
	
	


//	@Override
//	public boolean runScriptFile(String name, String loadScript) {
//		if(connection==null){
//			return false;
//		}
//		REXP result = null;
//		try {
//			result = connection.parseAndEval("try(eval(parse(text=" + loadScript + ")),silent=TRUE)");
//		} catch (REngineException | REXPMismatchException e) {
//
//			errorNoExit("Script error in Scenario " + scenarioName + ".script.txt", loadScript);
//			return false;
//
//		}
//		if (result.inherits("try-error")){
//			try {
//				errorNoExit("Script error in Scenario " + scenarioName + ".script.txt", loadScript);
//				
//				errorNoExit(result.asString().split("\n"));
//				if(result.asString().contains("ignoring SIGPIPE signal")){
//					System.exit(1);
//				}
//			} catch (REXPMismatchException e) {
//				// TODO Auto-generated catch block
//				// e.printStackTrace();
//			}
//			return false;
//		}
//		return true;
//		
//	}
//	
	public boolean runScriptFile(String name, String loadScript) {
		if(connection==null){
			return false;
		}
		REXP result = null;
		try {
			//String nloadScript="invisible(capture.output("+loadScript+"))";
			String nloadScript="suppressMessages("+loadScript+")";
			result = connection.eval(nloadScript );
		} catch (REngineException e) {

			errorNoExit("Script error in Scenario " + scenarioName + ".script.txt", loadScript);
			return false;

		}
		
		return true;
		
	}
	
	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public RConnection getConnection() {
		return connection;
	}

	public void setConnection(RConnection connection) {
		this.connection = connection;
	}
	
	@Override
	public void onOneScenarioDone(String scenarioName) {
		info(scenarioName+" is done!");
		
	}


}
