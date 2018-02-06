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
		ServerThread.startServer(getConfigs().getRPath(),getConfigs().getRServePath(),port);
	}

	@Override
	public REXPAdapter run(String loadScript) {
		REXP result = null;

			try {
				result = connection.eval(loadScript);
			} catch (RserveException e) {
				error("Script error:" + loadScript);
			}
			if(result==null){
				error("Script error:" + loadScript);
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

		try {
			//if (OSValidator.isWindows()) {
				info("Connectting to Rserve server at port " + port + "...");
				connection = new RConnection("localhost", port);
			//} else {
			//	info("Connectting to Rserve server...");
			//	connection = new RConnection();
			//}
		} catch (RserveException e) {
			// e.printStackTrace();
			error("Rserve server is not started. Please install the server if necessary and start it in your R console and run this again."
					+ "-- install.packages(\"Rserve\")-> library(Rserve)-> Rserve();");

		}
		info("Connected");

	}

	@Override
	public boolean runScriptFile(String name, String loadScript) {
		REXP result = null;
		try {
			result = connection.parseAndEval("try(eval(parse(text=" + loadScript + ")),silent=TRUE)");
		} catch (REngineException | REXPMismatchException e) {

			errorNoExit("Script error in Scenario " + scenarioName + ".script.txt", loadScript);
			return false;

		}
		if (result.inherits("try-error")){
			try {
				errorNoExit("Script error in Scenario " + scenarioName + ".script.txt", loadScript);
				errorNoExit(result.asString().split("\n"));
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
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
