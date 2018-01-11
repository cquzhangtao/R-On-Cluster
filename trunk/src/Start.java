

public class Start {

	public static void main(String[] args) {
		
		//System.out.println("Default Charset=" + Charset.defaultCharset());

		//AbstractBatchRun tool=new BatchRunOnServer();
		AbstractBatchRun tool=new BatchRunStandAlone();
		tool.runScenarios();
		tool.exit();
	}

}
