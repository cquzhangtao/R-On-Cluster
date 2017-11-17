import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class BatchRunOnServer extends AbstractBatchRun{
	
	private RConnection connection;
	
	public BatchRunOnServer(){
		super();
		System.out.println("Connectting to Rserve server...");
        try {
			connection = new RConnection();
		} catch (RserveException e) {			
			//e.printStackTrace();
			error("Rserve server is not started. Please install the server if necessary and start it in your R console and run this again."
        	 		+ "-- install.packages(\"Rserve\")-> library(Rserve)-> Rserve();");

		}
	}

	@Override
	public void run(String loadScript) {
		 try {
				connection.eval(loadScript);
				System.out.println("Executed!");
			} catch (RserveException e) {
				//e.printStackTrace();
				error("Script error");
			}
	}
	@Override
	public void close(){
		if(connection!=null){
			connection.close();
		}
	}

}
