import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class BatchRunOnServer extends AbstractBatchRun {

	private RConnection connection;
	private String scenarioName=null;

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

	private int port;

	public BatchRunOnServer(int port) {
		super();
		setWayToIntegerateJava(0);
		this.port = port;

	}

	@Override
	public REXP run(String loadScript) {
		REXP result = null;
		if (scenarioName == null) {
			try {
				result = connection.eval(loadScript);
			} catch (RserveException e) {
				error("Script error:" + loadScript);
			}
		} else {
			try {
				result = connection.parseAndEval("try(eval(parse(text=" + loadScript + ")),silent=TRUE)");
			} catch (REngineException | REXPMismatchException e) {

				error("Script error in Scenario " + scenarioName + ".script.txt", loadScript);

			}
			if (result.inherits("try-error"))
				try {
					errorNoExit("Script error in Scenario " + scenarioName + ".script.txt", loadScript);
					error(result.asString().split("\n"));
				} catch (REXPMismatchException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
		}

		return result;
	}

	@Override
	public void close() {
		if (connection != null) {
			info("Shutdown the server at port " + port);
			// connection.close();
			try {
				connection.serverShutdown();
			} catch (RserveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void start() {

		try {
			if (OSValidator.isWindows()) {
				info("Connectting to Rserve server at port " + port + "...");
				connection = new RConnection("localhost", port);
			} else {
				info("Connectting to Rserve server...");
				connection = new RConnection();
			}
		} catch (RserveException e) {
			// e.printStackTrace();
			error("Rserve server is not started. Please install the server if necessary and start it in your R console and run this again."
					+ "-- install.packages(\"Rserve\")-> library(Rserve)-> Rserve();");

		}
		info("Connected");

	}

}
