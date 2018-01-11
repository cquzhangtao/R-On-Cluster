
public class Start {

	public static void main(String[] args) {
		
	
		
		
		//AbstractBatchRun tool=new BatchRunOnServer();
		AbstractBatchRun tool=new BatchRunStandAlone();
		tool.runScenarios();
		tool.exit();
	}

}
